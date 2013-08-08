package gov.usgs.cida.geonetwork;

import gov.usgs.cida.config.DynamicReadOnlyProperties;
import gov.usgs.cida.gdp.utilities.JNDISingleton;
import gov.usgs.service.OWSProxyServletX;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jordan Walker <jiwalker@usgs.gov>
 */
public class GeonetworkProxy extends OWSProxyServletX {

    private static Logger LOGGER = LoggerFactory.getLogger(GeonetworkProxy.class);
    private static CookieStore cookieJar;
    private static Date selfExpireCookieDate;
    private static final DynamicReadOnlyProperties props = JNDISingleton.getInstance();
    private static final String geonetworkAddr = props.getProperty("derivative/GEONETWORK_ADDR");
    private static final String GEONETWORK_CSW = geonetworkAddr + "/srv/en/csw";
    private static final String GEONETWORK_LOGIN = geonetworkAddr + "/srv/en/xml.user.login";
    private static final String GEONETWORK_LOGOUT = geonetworkAddr + "/srv/en/xml.user.logout";
    private static final String GEONETWORK_USER = props.getProperty("derivative/GEONETWORK_USER");
    private static final String GEONETWORK_PASS = props.getProperty("derivative/GEONETWORK_PASS");
	
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        if (null == geonetworkAddr || null == GEONETWORK_USER || null == GEONETWORK_PASS) {
            throw new RuntimeException("Geonetwork dependency not declared in JNDI context,"
                    + " please set GEONETWORK_ADDR, GEONETWORK_USER, and GEONETWORK_PASS");
        }
        cookieJar = new BasicCookieStore();
        selfExpireCookieDate = new Date();
        LOGGER.debug("Geonetwork proxy initialized");
    }

    @Override
    public void destroy() {
        try {
            logout();
        }
        catch (IOException ioe) {
            LOGGER.debug("Error logging out of geonetwork", ioe);
        }
        catch (URISyntaxException ex) {
            LOGGER.debug("Exception in logout URI", ex);
        }
        HttpClient httpClient = getHttpClient(null);
        if (null != httpClient) {
            ClientConnectionManager conMgr = httpClient.getConnectionManager();
            if (null != conMgr) {
                conMgr.shutdown();
            }
        }
        cookieJar.clear();
        cookieJar = null;
        LOGGER.debug("Servlet destroy complete");
    }

    /**
     * Calls the login for geonetwork, returns the cookie store
     * @throws URISyntaxException
     * @throws IOException 
     */
    private synchronized void login() throws URISyntaxException,
                                                   IOException {
        URI loginUri = new URI(GEONETWORK_LOGIN);
        HttpContext localContext = new BasicHttpContext();
        localContext.setAttribute(ClientContext.COOKIE_STORE, cookieJar);
        HttpUriRequest request = new HttpPost(loginUri);
        // username and password should be configured somewhere
        LOGGER.warn("username and password are still not parameterized");
        HttpEntity entity = new StringEntity(
                "username=" + GEONETWORK_USER + "&password=" + GEONETWORK_PASS,
                "application/x-www-form-urlencoded",
                "UTF-8");
        ((HttpEntityEnclosingRequest) request).setEntity(entity);
        HttpClient httpClient = getHttpClient(null);
        httpClient.execute(request, localContext);
        
        Calendar cal = new GregorianCalendar();
        cal.add(Calendar.HOUR_OF_DAY, 1);
        selfExpireCookieDate = cal.getTime();
    }

    /**
     * Calls the logout url for geonetwork, kills the cookie store
     * @throws URISyntaxException
     * @throws IOException 
     */
    private synchronized void logout() throws URISyntaxException,
                                                    IOException {
        URI logout = new URI(GEONETWORK_LOGOUT);
        HttpContext localContext = new BasicHttpContext();
        localContext.setAttribute(ClientContext.COOKIE_STORE, cookieJar);
        HttpUriRequest request = new HttpGet(logout);
        HttpClient httpClient = getHttpClient(null);
        httpClient.execute(request, localContext);
        cookieJar.clear();
    }
    
    private synchronized boolean isExistingCookie() throws URISyntaxException, IOException {
        if (cookieJar != null) {
            Date now = new Date();
            if (now.after(selfExpireCookieDate)) {
                cookieJar.clear();
                return false;
            }
            return !(cookieJar.getCookies().isEmpty());
        }
        return false;
    }

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void service(HttpServletRequest request,
                           HttpServletResponse response)
            throws ServletException, IOException {

        try {
            if(!isExistingCookie()) {
                login();
            }
            HttpUriRequest serverRequest = generateServerRequest(request);
            handleServerRequest(request, response, serverRequest);
        }
        catch (ProxyException ex) {
            LOGGER.debug("Parent proxy class threw exception", ex);
            // should probably throw something to front end
        }
        catch (URISyntaxException ex) {
            LOGGER.debug("FAIL!", ex);
        }
    }

    @Override
    protected void handleServerRequest(HttpServletRequest clientRequest,
                                       HttpServletResponse clientResponse,
                                       HttpUriRequest serverRequest) throws
            ProxyException {
        HttpClient serverClient = getHttpClient(clientRequest);
        try {
            HttpContext localContext = new BasicHttpContext();
            localContext.setAttribute(ClientContext.COOKIE_STORE, cookieJar);
            HttpResponse methodReponse = serverClient.execute(serverRequest,
                                                              localContext);
            handleServerResponse(clientRequest, clientResponse, methodReponse);
        }
        catch (ClientProtocolException e) {
            throw new ProxyException("Client protocol error", e);
        }
        catch (IOException e) {
            throw new ProxyException("I/O error on server request", e);
        }

    }

    @Override
    protected String getServerRequestURIAsString(
            HttpServletRequest clientrequest) {
        return GEONETWORK_CSW;
    }
}
