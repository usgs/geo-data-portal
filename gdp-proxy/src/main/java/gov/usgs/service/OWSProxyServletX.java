package gov.usgs.service;

import gov.usgs.cida.blacklist.BlacklistFactory;
import gov.usgs.cida.blacklist.BlacklistInterface;
import static gov.usgs.cida.blacklist.BlacklistFactory.BlacklistType.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.cache.CacheResponseStatus;
import org.apache.http.client.cache.HttpCacheEntry;
import org.apache.http.client.cache.HttpCacheStorage;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpTrace;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.cache.BasicHttpCacheStorage;
import org.apache.http.impl.client.cache.CacheConfig;
import org.apache.http.impl.client.cache.CachingHttpClient;
import org.apache.http.impl.conn.SchemeRegistryFactory;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpParams;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 *
 * @author jwalker
 */
public class OWSProxyServletX extends HttpServlet {
    
    private static Logger LOGGER = LoggerFactory.getLogger(OWSProxyServletX.class);
    
    // Cache setup
    private final static boolean CACHING_ENABLED = true;
    private final static int CACHING_MAX_ENTRIES = 2048;
    private final static int CACHING_MAX_RESPONSE_SIZE = 32767;
    private final static boolean CACHING_HEURISTIC_ENABLED = true; // behaves per RFC 2616
    private final static long CACHIN_HEURITIC_DEFAULT_LIFETIME_SECONDS = 300;  // 5 minutes
    
    // Connection pool setup
    private final static int CONNECTION_TTL = 15 * 60 * 1000;       // 15 minutes, default is infinte
    private final static int CONNECTIONS_MAX_TOTAL = 256;
    private final static int CONNECTIONS_MAX_ROUTE = 32;
    
    // Connection timeouts
    private final static int CLIENT_SOCKET_TIMEOUT = 5 * 60 * 1000; // 5 minutes, default is infinite
    private final static int CLIENT_CONNECTION_TIMEOUT = 15 * 1000; // 15 seconds, default is infinte
    
	public static final String INVALID_ENDPOINT = 
            "Service you are requesting is not a valid OWS service." +
			"  If this is incorrect, register this url by submitting a GetCapabilities request.";
    public static final String ENDPOINT_CONFIG = "endpoints.xml";

	private Set<Endpoint> verifiedEndpointCache = Collections.synchronizedSet(new HashSet<Endpoint>());
	private BlacklistInterface blacklist = BlacklistFactory.setActiveBlacklist(DELAY_ONLY);
	
    private ThreadSafeClientConnManager clientConnectionManager;
    
    private HttpCacheStorage cacheStorage;
    private CacheConfig cacheConfig;
    
    private Set<String> ignoredClientRequestHeaderSet;
    private Set<String> ignoredServerResponseHeaderSet;
    
    // don't use directly, use getHTTPClient()
    private HttpClient proxyHttpClient;
    
	@Override
	public void init(ServletConfig config) throws ServletException {
        super.init(config);
        
        // Initialize connection manager, this is thread-safe.  if we use this
        // with any HttpClient instance it becomes thread-safe.
        clientConnectionManager = new ThreadSafeClientConnManager(SchemeRegistryFactory.createDefault(), CONNECTION_TTL, TimeUnit.MILLISECONDS);
        clientConnectionManager.setMaxTotal(CONNECTIONS_MAX_TOTAL);
        clientConnectionManager.setDefaultMaxPerRoute(CONNECTIONS_MAX_ROUTE);
        LOGGER.info("Created HTTP client connection manager: maximum connections total = {}, maximum connections per route = {}",
                clientConnectionManager.getMaxTotal(), clientConnectionManager.getDefaultMaxPerRoute());
        
        // Ignored headers relating to proxing requests
        ignoredClientRequestHeaderSet = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
		ignoredClientRequestHeaderSet.add("host");          // don't parameterize, need to swtich host from proxy to server
		ignoredClientRequestHeaderSet.add("connection");    // don't parameterize, let proxy to server call do it's own handling
		ignoredClientRequestHeaderSet.add("cookie");        // parameterize (cookies passthru?)
		ignoredClientRequestHeaderSet.add("authorization"); // parameterize (authorization passthru?)
		ignoredClientRequestHeaderSet.add("content-length");// ignore header in request, this is set in client call.
        
        // Ignored headers relating to proxing reponses.
        ignoredServerResponseHeaderSet = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
		ignoredServerResponseHeaderSet.add("transfer-encoding");// don't parameterize
		ignoredServerResponseHeaderSet.add("keep-alive");       // don't parameterize
		ignoredServerResponseHeaderSet.add("set-cookie");       // parameterize (cookies passthru?)
		ignoredServerResponseHeaderSet.add("authorization");    // parameterize (authorization passthru?)
//		ignoredServerResponseHeaderSet.add("content-length");   // allow for now, NOTE: are you doing response body content rewrite?\
        
        // Endpoints we know we are going to be used can just be fed into the verified endpoint cache
        InputStream configStream = OWSProxyServletX.class.getClassLoader().getResourceAsStream(ENDPOINT_CONFIG);
        if (null != configStream) {
            readValidEndpointsFromConfig(configStream);
        }
        
        // If enabled setup a memory/heap cache for server responses.  If we
        // want we could setup a layered L1/L2/... cache scheme by wrapping
        // CachingHttpClient instances with appropriate storage (i.e. small cache
        // in heap storage, larger with file storage, etc..) 
        if (CACHING_ENABLED) {
            cacheConfig = new CacheConfig();  
            cacheConfig.setMaxCacheEntries(CACHING_MAX_ENTRIES);
            cacheConfig.setMaxObjectSizeBytes(CACHING_MAX_RESPONSE_SIZE);
            cacheConfig.setHeuristicCachingEnabled(CACHING_HEURISTIC_ENABLED);
            cacheConfig.setHeuristicDefaultLifetime(CACHIN_HEURITIC_DEFAULT_LIFETIME_SECONDS);
            cacheConfig.setSharedCache(true);  // won't cache authorized responses
            cacheStorage = new ProxyCacheStorage(cacheConfig);
            LOGGER.info("HTTP Response caching enabled: maximum cache entries = {}, maximum response size = {} bytes, heuristic caching enabled = {}, heuristic default lifetime = {} s",
                    new Object[] {
                        cacheConfig.getMaxCacheEntries(),
                        cacheConfig.getMaxObjectSizeBytes(),
                        cacheConfig.isHeuristicCachingEnabled(),
                        cacheConfig.getHeuristicDefaultLifetime(),
                    });
        } else {
            LOGGER.info("HTTP Response caching disabled");
        }
        
        HttpParams httpParams = new BasicHttpParams();
        HttpConnectionParams.setSoTimeout(httpParams, CLIENT_SOCKET_TIMEOUT);
        HttpConnectionParams.setConnectionTimeout(httpParams, CLIENT_CONNECTION_TIMEOUT);
        DefaultHttpClient defaultClient = new DefaultHttpClient(clientConnectionManager, httpParams);
        this.proxyHttpClient = CACHING_ENABLED ? new CachingHttpClient(defaultClient, cacheStorage, cacheConfig) : defaultClient;
	}

    @Override
    public void destroy() {
        clientConnectionManager.shutdown();
    }

    @Override
    protected void service(HttpServletRequest clientRequest, HttpServletResponse clientResponse) throws ServletException, IOException {
        try {
            if (isValidRequest(clientRequest)) {
                proxyRequest(clientRequest, clientResponse);
            } else {
                proxyReject(clientRequest, clientResponse);
            }
        } catch (Exception e) {
            // Important!  With Apache Tomcat 6 container uncaught exceptions will result in
            // container returning HTTP status code 200 (OK) w/ empty response body.
            proxyError(clientRequest, clientResponse, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
            // log a little more information to help with debugging as this is a bas state...
            StringBuilder logMessageBuilder = new StringBuilder();
            logMessageBuilder.append("Uncaught exception handling proxy request from ").
                    append(getClientRequestURIAsString(clientRequest)).append(" to ").
                    append(getServerRequestURIAsString(clientRequest));
            LOGGER.error(logMessageBuilder.toString(), e);
        } finally {
            clientResponse.flushBuffer();
        }   
    }
    
    protected void proxyRequest(HttpServletRequest clientRequest, HttpServletResponse clientResponse) {
        try {
            HttpUriRequest serverRequest = generateServerRequest(clientRequest);
            handleServerRequest(clientRequest, clientResponse, serverRequest);
        } catch (ProxyException e) {
            proxyError(clientRequest, clientResponse, e);
        }
    }
    
    protected void proxyReject(HttpServletRequest clientRequest, HttpServletResponse clientResponse) {
        proxyError(clientRequest, clientResponse, HttpServletResponse.SC_FORBIDDEN, INVALID_ENDPOINT);
    }
    
    protected void proxyError(HttpServletRequest clientRequest, HttpServletResponse clientResponse, ProxyException exception) {
        StringBuilder errorBuilder = new StringBuilder(exception.getMessage());
        Throwable cause = exception.getRootCause();
        if (cause != null) {
            errorBuilder.append(", exception message: ").append(cause.getMessage());
        }
        proxyError(clientRequest, clientResponse, HttpServletResponse.SC_BAD_REQUEST, errorBuilder.toString());
    }
    
    protected void proxyError(HttpServletRequest clientRequest, HttpServletResponse clientResponse, int errorCode, String errorMessage) {
        PrintWriter writer = null;
        try {
            LOGGER.error("Error proxying request from {} to {}. {}",
                    new Object[] {
                        getClientRequestURIAsString(clientRequest),
                        getServerRequestURIAsString(clientRequest),
                        errorMessage});
            clientResponse.setStatus(errorCode);
            clientResponse.setCharacterEncoding("UTF-8");
            writer = clientResponse.getWriter();
            writer.println(errorMessage);
        } catch (IOException e) {
            LOGGER.error("Error writing error message to client: {} {}, exception message: {} ", new Object[] {
                errorCode,
                errorMessage,
                e.getMessage()});
        } finally {
            IOUtils.closeQuietly(writer);
        }
    }
    
    protected HttpUriRequest generateServerRequest(HttpServletRequest clientRequest) throws ProxyException {
        HttpUriRequest serverRequest = null;
        try {
            
            // 1) Generate Server URI
            String serverRequestURIAsString = getServerRequestURIAsString(clientRequest);
            // instantiating to URL then calling toURI gives us some error
            // checking as URI(String) appears too forgiving.
            URI serverRequestURI = (new URL(serverRequestURIAsString)).toURI();
            
            // 2 ) Create request base on client request method
            String clientRequestMethod = clientRequest.getMethod();
            if ("HEAD".equals(clientRequestMethod)) {
                serverRequest = new HttpHead(serverRequestURI);
            } else if ("GET".equals(clientRequestMethod)) {
                serverRequest = new HttpGet(serverRequestURI);
            } else if ("POST".equals(clientRequestMethod)) {
                serverRequest = new HttpPost(serverRequestURI);
            } else if ("PUT".equals(clientRequestMethod)) {
                serverRequest = new HttpPut(serverRequestURI);
            } else if ("DELETE".equals(clientRequestMethod)) {
                serverRequest = new HttpDelete(serverRequestURI);
            } else if ("TRACE".equals(clientRequestMethod)) {
                serverRequest = new HttpTrace(serverRequestURI);
            } else if ("OPTIONS".equals(clientRequestMethod)) {
                serverRequest = new HttpOptions(serverRequestURI);
            } else {
               throw new ProxyException("Unsupported request method, " + clientRequestMethod);
            }

            // 3) Map client request headers to server request
            generateServerRequestHeaders(clientRequest, serverRequest);

            // 4) Copy client request body to server request
            int contentLength = clientRequest.getContentLength();
            if (contentLength > 0) {
                if (serverRequest instanceof HttpEntityEnclosingRequest) {
                    try {
                        // !!! Are you here to edit this to enable request body content rewrite?
                        //     You may want to remove or edit the "Content-Length" header !!!
                        InputStreamEntity serverRequestEntity = new InputStreamEntity(
                                clientRequest.getInputStream(),
                                contentLength);
                        serverRequestEntity.setContentType(clientRequest.getContentType());
                        ((HttpEntityEnclosingRequest)serverRequest).setEntity(serverRequestEntity);
                    } catch (IOException e) {
                        throw new ProxyException("Error reading client request body", e);
                    }
                } else {
                    throw new ProxyException("Content in request body unsupported for client request method " + serverRequest.getMethod());
                }
            }
            
        } catch (MalformedURLException e) {
            throw new ProxyException( "Syntax error parsing server URL", e);
        } catch (URISyntaxException e) {
            throw new ProxyException( "Syntax error parsing server URI", e);
        }
        
        return serverRequest;
    }
    
    protected void generateServerRequestHeaders(HttpServletRequest clientRequest, HttpUriRequest serverRequest) {
        
        Enumeration<String> headerNameEnumeration = clientRequest.getHeaderNames();
        while (headerNameEnumeration.hasMoreElements()) {
            String requestHeaderName = headerNameEnumeration.nextElement();
            Enumeration<String> headerValueEnumeration = clientRequest.getHeaders(requestHeaderName);
            while (headerValueEnumeration.hasMoreElements()) {
                String requestHeaderValue = headerValueEnumeration.nextElement();
                if (!ignoredClientRequestHeaderSet.contains(requestHeaderName)) {
                    serverRequest.addHeader(requestHeaderName, requestHeaderValue);
                    LOGGER.debug("Mapped client request header \"{}: {}\"", requestHeaderName, requestHeaderValue);
                } else {
                    LOGGER.debug("Ignored client request header \"{}: {}\"", requestHeaderName, requestHeaderValue);
                }
            }
            
        }
        
        URI serverURI = serverRequest.getURI();
        StringBuilder serverHostBuilder = new StringBuilder(serverURI.getHost());
        if (serverURI.getPort() > -1) {
            serverHostBuilder.append(':').append(serverURI.getPort());
        } 
        String requestHost = serverHostBuilder.toString();
        serverRequest.addHeader("Host", serverHostBuilder.toString());
        LOGGER.debug("Added server request header \"Host: {}\"", requestHost);
    }
    
    protected void generateClientResponseHeaders(HttpServletResponse clientResponse, HttpResponse serverResponse) {
        Header[] proxyResponseHeaders = serverResponse.getAllHeaders();
        for (Header header : proxyResponseHeaders) {
            String responseHeaderName = header.getName();
            String responseHeaderValue = header.getValue();
            if (!ignoredServerResponseHeaderSet.contains(responseHeaderName)) {
                clientResponse.addHeader(responseHeaderName, responseHeaderValue);
                LOGGER.debug("Mapped server response header \"{}: {}\"", responseHeaderName, responseHeaderValue);
            } else {
                LOGGER.debug("Ignored server response header \"{}: {}\"", responseHeaderName, responseHeaderValue);
            }
        }
    }
    
    protected void handleServerRequest(HttpServletRequest clientRequest, HttpServletResponse clientResponse, HttpUriRequest serverRequest) throws ProxyException {
        HttpClient serverClient = getHttpClient(clientRequest);
        try {
            HttpContext localContext = new BasicHttpContext();
            HttpResponse methodReponse = serverClient.execute(serverRequest, localContext);
            if (CACHING_ENABLED && LOGGER.isDebugEnabled()) {
                CacheResponseStatus responseStatus = (CacheResponseStatus)
                        localContext.getAttribute(CachingHttpClient.CACHE_RESPONSE_STATUS);
                if (responseStatus != null) {
                    LOGGER.debug("CacheResponseStatus={} for request to {}",
                            responseStatus.toString(),
                            serverRequest.getURI().toString());
                }
            }
            handleServerResponse(clientRequest, clientResponse, methodReponse);
        } catch (ClientProtocolException e) {
            throw new ProxyException("Client protocol error", e);
        } catch (IOException e) {
            throw new ProxyException("I/O error on server request", e);
        }
        
    }
    
    protected void handleServerResponse(HttpServletRequest clientRequest, HttpServletResponse clientResponse, HttpResponse serverResponse) throws ProxyException {

        String clientRequestURLAsString = getClientRequestURIAsString(clientRequest);
        String serverRequestURLAsString = getServerRequestURIAsString(clientRequest);
        
        // 1) Map server response status to client response
        // NOTE: There's no clear way to map status message, HttpServletResponse.sendError(int, String)
        // will display some custom html (we don't want that here).  HttpServletResponse.setStatus(int, String)
        // is deprecated and i'm not certain there is (will be) an functional implementation behind it.
        StatusLine serverStatusLine = serverResponse.getStatusLine();
        int statusCode = serverStatusLine.getStatusCode();
        clientResponse.setStatus(statusCode);
        LOGGER.debug("Mapped server status code {}", statusCode);

        // 2) Map server response headers to client response
        generateClientResponseHeaders(clientResponse, serverResponse);
        
        
        // 3) Copy server response body to client response
        HttpEntity methodEntity = serverResponse.getEntity();
        if (methodEntity != null) {
            
            InputStream is = null;
            OutputStream os = null;
            
            long responseBytes = 0;
            
            try {
                
                // !!! Are you here to edit this to enable response body content rewrite?
                //     You may want to remove or edit the "Content-Length" header !!!
                try {
                    is = methodEntity.getContent();
                } catch (IOException e) {
                    throw new ProxyException("Error obtaining input stream for server response", e);
                }

                try {
                    os = clientResponse.getOutputStream();
                } catch (IOException e) {
                    throw new ProxyException("Error obtaining output stream for client response", e);
                }
                
                try {
                    responseBytes = IOUtils.copyLarge(is, os);
                } catch (IOException e) {
                    throw new ProxyException("Error copying server response to client", e);
                }

            } finally {
                LOGGER.debug("Copied {} bytes from server response for proxy from {} to {}",
                        new Object[] {
                            responseBytes,
                            clientRequestURLAsString,
                            serverRequestURLAsString});
                try {
                    // This is important to guarantee connection release back into
                    // connection pool for future reuse!
                    EntityUtils.consume(methodEntity);
                } catch (IOException e) {
                    LOGGER.error("Error consuming remaining bytes in server response entity for proxy reponse from {} to {}",
                            clientRequestURLAsString, serverRequestURLAsString);
                }
                IOUtils.closeQuietly(is);
                IOUtils.closeQuietly(os);
            }
        } else {
            LOGGER.warn("Server response was empty for proxy response from {} to {}",
                    clientRequestURLAsString, serverRequestURLAsString);
        }
    }
    
    protected HttpClient getHttpClient(HttpServletRequest clientRequest) {
        // this could be extended to return client specific HttpClients in
        // this future.  An example use case is if authorization were enabled
        // and we wanted to provide each client endpoint with it's own 
        // CachedHttpClient with a private cache to store authorized response 
        // content with its own cache storage.  In the absense of this
        // we can just return a singleton client.
        return proxyHttpClient;
    }
    
    protected String getClientRequestURIAsString(HttpServletRequest clientRequest) {
        return clientRequest.getRequestURL().toString();
    }
    
    protected String getServerRequestURIAsString(HttpServletRequest clientrequest) {
        String proxyPath = new StringBuilder(clientrequest.getContextPath()).
                append(clientrequest.getServletPath()).
                append('/').
                toString();
        
        StringBuilder requestBuffer = new StringBuilder(clientrequest.getRequestURI());
        // request query string is *not* URL decoded
        String requestQueryString = clientrequest.getQueryString();
        if (requestQueryString != null) {
            requestBuffer.append('?').append(requestQueryString);
        }
        return requestBuffer.substring(proxyPath.length());
    }
    
	private boolean isValidRequest(HttpServletRequest request) {
		String remoteAddr = request.getRemoteAddr();
		if (blacklist.isBlacklisted(remoteAddr)) {
			// fall through to false
		}
		else {
			String serverURIAsString = getServerRequestURIAsString(request);

			LOGGER.debug("Looking up URI for endpoint validation: {}", serverURIAsString);
			Endpoint owsEndpoint = new Endpoint(serverURIAsString);

			if (verifiedEndpointCache.contains(owsEndpoint)) {
				LOGGER.debug("URI in cache, proxying");
				return true;
			}

			if (OGCCommons.isOWSEndpoint(owsEndpoint)) {
				verifiedEndpointCache.add(owsEndpoint);
				Document capabilitiesDocument = OGCCommons.getCapabilitiesDocument(owsEndpoint);
				verifiedEndpointCache.addAll(OGCCommons.getOperationEndpoints(capabilitiesDocument));
				return true;
			}
			else {
				long timeToSleep = blacklist.determineSleepTime(remoteAddr);

				try {
					Thread.sleep(timeToSleep);
				}
				catch (InterruptedException ex) {
					LOGGER.debug("Sleep interrupted for 403 error wait");
				}
			}
		}
		return false;
	}
    
    private void readValidEndpointsFromConfig(InputStream in) {
        Properties props = null;
        try {
            props = new Properties();
            props.loadFromXML(in);
        } catch (IOException ex) {
            LOGGER.error("Could not load properties from file", ex);
        } finally {
            IOUtils.closeQuietly(in);
        }
        for (String key : props.stringPropertyNames()) {
            String value = props.getProperty(key);
            Endpoint endpt = new Endpoint(value);
            if (endpt.getType() != Endpoint.EndpointType.UNKNOWN) {
                LOGGER.debug("Adding " + value + " to verifiedCache");
                verifiedEndpointCache.add(endpt);
            }
            else {
                LOGGER.debug("Unable to determine endpoint type");
            }
        }
        
    }
    
    public static class ProxyException extends Exception {
        public ProxyException(String message) {
            super(message);
        }
        public ProxyException(String message, Throwable cause) {
            super(message, cause);
        }
        public Throwable getRootCause() {
            return getRootCause(getCause());
        }
        private Throwable getRootCause(Throwable t) {
            return t.getCause() == null ? t : getRootCause(t.getCause());
        }
    }
    
    public static class ProxyCacheStorage extends BasicHttpCacheStorage {
        public ProxyCacheStorage(CacheConfig cacheConfig) {
            super(cacheConfig);
        }

        @Override
        public synchronized void putEntry(String url, HttpCacheEntry entry) throws IOException {
            super.putEntry(url, checkEntry(entry));
        }

        @Override
        public synchronized HttpCacheEntry getEntry(String url) throws IOException {
            return checkEntry(super.getEntry(url));
        }
        
        private HttpCacheEntry checkEntry(HttpCacheEntry entry) {
            if (entry != null && entry.getFirstHeader(HTTP.CONTENT_LEN) == null) {
                Header[] originalHeaders = entry.getAllHeaders();
                int originalHeaderCount = originalHeaders.length;
                Header[] fixedHeaders = Arrays.copyOf(originalHeaders, originalHeaderCount + 1);
                fixedHeaders[originalHeaderCount] = new BasicHeader(HTTP.CONTENT_LEN, Long.toString(entry.getResource().length()));
                return new HttpCacheEntry(
                        entry.getRequestDate(),
                        entry.getResponseDate(),
                        entry.getStatusLine(),
                        fixedHeaders,
                        entry.getResource(),
                        entry.getVariantMap());
            } else {
                return entry;
            }
        }
        
    }
}
