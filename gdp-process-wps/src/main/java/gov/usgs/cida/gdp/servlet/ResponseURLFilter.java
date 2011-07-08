package gov.usgs.cida.gdp.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import org.n52.wps.commons.WPSConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tkunicki
 */
public class ResponseURLFilter implements Filter {
    
    private final static String HEADER_CONTENT_LENGTH = "Content-Length";

    private final static Logger LOGGER = LoggerFactory.getLogger(ResponseURLFilter.class);

    private String configURLString;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        configURLString =  "http://" +
            WPSConfig.getInstance().getWPSConfig().getServer().getHostname() + ":" +
            WPSConfig.getInstance().getWPSConfig().getServer().getHostport() + "/" +
            WPSConfig.getInstance().getWPSConfig().getServer().getWebappPath();
        LOGGER.info("Response URL filtering enabled using base URL of {}", configURLString);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest requestHTTP = (request instanceof HttpServletRequest) ? (HttpServletRequest)request : null;
        HttpServletResponse responseHTTP = (response instanceof HttpServletResponse) ? (HttpServletResponse)response : null;
        if (requestHTTP != null && responseHTTP != null) {
            String requestURLString = extractRequestURLString(requestHTTP);
            String baseURLString = requestURLString.replaceAll("/[^/]*$", "");
            LOGGER.info("Wrapping response for URL filtering");
            chain.doFilter(request, new BaseURLFilterHttpServletResponse(responseHTTP, configURLString, baseURLString));
        } else {
            LOGGER.warn("Unable to to wrap response for URL filtering");
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {

    }

    private static String extractRequestURLString(HttpServletRequest request) {
        // The "X-REQUEST-URL" HTTP Request Header is CIDA specific, added at
        // the externally facing Apache HTTPd proxy if present.
        String requestURLString = request.getHeader("X-REQUEST-URL");
        if (requestURLString == null || requestURLString.length() == 0) {
            requestURLString = request.getRequestURL().toString();
            LOGGER.warn("HTTP request header \"X-REQUEST-URL\" is missing");
        } else {
            LOGGER.info("HTTP request header \"X-REQUEST-URL\" is {}", requestURLString);
        }
        return requestURLString;
    }

    private static class BaseURLFilterHttpServletResponse extends HttpServletResponseWrapper {

        private final String configURLString;
        public final String requestURLString;

        public BaseURLFilterHttpServletResponse(HttpServletResponse response, String configURLString, String requestURLString) {
            super(response);
            this.configURLString = configURLString;
            this.requestURLString = requestURLString;
        }

        @Override
        public ServletOutputStream getOutputStream() throws IOException {
            String contentType = getContentType();
            if (enableFiltering(contentType)) {
                LOGGER.info("Content-type: {}, response URL filtering enabled for response to {}", contentType, requestURLString);
                return new ServletOutputStreamWrapper(
                        getResponse().getOutputStream(),
                        configURLString,
                        requestURLString);
            } else {
                LOGGER.info("Content-type: {}, response URL filtering disabled for response to {}", contentType, requestURLString);
                return getResponse().getOutputStream();
            }
        }

        @Override
        public void setContentLength(int len) {
            if (allowHeader(HEADER_CONTENT_LENGTH, len)) {
                super.setContentLength(len);
            }
        }
        
        @Override
        public void addHeader(String name, String value) {
            if (allowHeader(name, value)) {
                super.addHeader(name, value);
            }
        }

        @Override
        public void setHeader(String name, String value) {
            if (allowHeader(name, value)) {
                super.addHeader(name, value);
            }
        }
        
        @Override
        public void addIntHeader(String name, int value) {
            if (allowHeader(name, value)) {
                super.addIntHeader(name, value);
            }
        }
        
        @Override
        public void setIntHeader(String name, int value) {
            if (allowHeader(name, value)) {
                super.setIntHeader(name, value);
            }
        }

        
        private boolean allowHeader(String name, String value) {
            if (HEADER_CONTENT_LENGTH.equalsIgnoreCase(name) && enableFiltering(getContentType())) {
                LOGGER.info("Refusing to set \"Content-Length\" response header, response filtering is enabled and reponse length could change.");
                return false;
            } else {
                return true;
            }
        }
        
        private boolean allowHeader(String name, int value) {
            if (HEADER_CONTENT_LENGTH.equalsIgnoreCase(name) && enableFiltering(getContentType())) {
                LOGGER.info("Refusing to set \"Content-Length\" response header, response filtering is enabled and reponse length could change.");
                return false;
            } else {
                return true;
            }
        }

        @Override
        public PrintWriter getWriter() throws IOException {
            return new PrintWriter(getOutputStream()); 
        }
        
        private boolean enableFiltering(String contentType) {
            return contentType == null || contentType.endsWith("xml");
        }
    }

    private static class ServletOutputStreamWrapper extends ServletOutputStream {

        private final ServletOutputStream outputStream;

        private ByteBuffer find;
        private ByteBuffer replace;
        private boolean match;

        public ServletOutputStreamWrapper(ServletOutputStream outputStream, String find, String replace) {
            this.outputStream = outputStream;
            this.find = ByteBuffer.wrap(find.getBytes());
            this.replace = ByteBuffer.wrap(replace.getBytes());
        }

        @Override
        public void write(int i) throws IOException {
            byte b = (byte)(i & 0xff);
            if (match) {
                if(find.get() == b) {
                    if (!find.hasRemaining()) {
                        // COMPLETE MATCH
                        // 1) write out replacement buffer
                        // 2) unset 'match' flag
                        outputStream.write(replace.array());
                        match = false;
                    } // else { /* POTENTIAL MATCH ongoing, writes deferred */ } 
                } else {
                    // FAILED MATCH
                    // 1) write out portion of 'find' buffer that matched
                    // 2) write out the current byte that caused mismatch
                    // 3) unset 'match' flag
                    outputStream.write(find.array(), 0, find.position() - 1);
                    outputStream.write(b);
                    match = false;
                }
            } else {
                if (b == find.get(0)) {
                    // POTENTIAL MATCH started, write deferred
                    // - set 'match' flag to true for next write call
                    // - position 'find' buffer at next byte for next check
                    match = true;
                    find.position(1);
                } else {
                    // NO MATCH, just pass byte through to underlying outputstream
                    outputStream.write(b);
                }
            }
        }

        @Override
        public void write(byte[] bytes) throws IOException {
            write(bytes, 0, bytes.length);
        }

        @Override
        public void write(byte[] b, int o, int l) throws IOException {
            for (int i = 0; i < l; ++i) { write(b[o + i]); }
        }

        @Override
        public void close() throws IOException {
            if (match) {
                // FAILED MATCH, complete deferred writes
                outputStream.write(find.array(), 0, find.position());
                match = false;
            }
            super.close();
            outputStream.close();
        }

        @Override
        public void flush() throws IOException {
            super.flush();
            outputStream.flush();
        }
    }


}
