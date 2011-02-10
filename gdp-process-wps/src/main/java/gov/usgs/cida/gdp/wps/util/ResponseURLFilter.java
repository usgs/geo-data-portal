package gov.usgs.cida.gdp.wps.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.util.Enumeration;
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

    private final static Logger LOGGER = LoggerFactory.getLogger(ResponseURLFilter.class);

    private String configURLString;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        configURLString =  "http://" +
            WPSConfig.getInstance().getWPSConfig().getServer().getHostname() + ":" +
            WPSConfig.getInstance().getWPSConfig().getServer().getHostport() +
            filterConfig.getServletContext().getContextPath();
        LOGGER.info("Response URL filter will replace {} with request URL base", configURLString);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest requestHTTP = (request instanceof HttpServletRequest) ? (HttpServletRequest)request : null;
        HttpServletResponse responseHTTP = (response instanceof HttpServletResponse) ? (HttpServletResponse)response : null;
        if (requestHTTP != null && responseHTTP != null) {
            String requestURLString = extractRequestURLString(requestHTTP);
            String baseURLString = requestURLString.replaceAll("/[^/]*$", "");
            LOGGER.info("Response URL filtering enabled for request to {}, all instances of {} will be replaced with {}",
                    new Object [] { requestURLString, configURLString, baseURLString });
            chain.doFilter(request, new BaseURLFilterHttpServletResponse(responseHTTP, configURLString, baseURLString));
        } else {
            LOGGER.warn("Unable to configure response URL filtering, unable to cast request and/or response instances");
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {

    }

    private String extractRequestURLString(HttpServletRequest request) {
//        String requestServerName = request.getHeader("X-SERVER-NAME");
//        String requestServerPort = request.getHeader("X-SERVER-PORT");
//        String requestPath = request.getHeader("X-REQUEST-PATH");
//        String requestURI = request.getHeader("X-REQUEST-URI");
        StringBuilder sb = new StringBuilder("Request Header:\n");
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            sb.append("  ").append(headerName).append(" : ").append(request.getHeader(headerName)).append("\n");
        }
        LOGGER.info(sb.toString());
        return request.getRequestURL().toString();
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
            return new OutputStreamWrapper(getResponse().getOutputStream(), configURLString, requestURLString);
        }

        @Override
        public PrintWriter getWriter() throws IOException {
            return new PrintWriter(getOutputStream()); 
        }
    }

    private static class OutputStreamWrapper extends ServletOutputStream {

        private final ServletOutputStream outputStream;

        private ByteBuffer find;
        private ByteBuffer replace;
        private boolean match;

        public OutputStreamWrapper(ServletOutputStream outputStream, String find, String replace) {
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
                    } else {
                        // POTENTIAL MATCH ongoing, write deferred
                    }
                } else {
                    // FAILED MATCH
                    // 1) write out portion of 'find' buffer that matched
                    // 2) write out the current byte that cause mismatch
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
