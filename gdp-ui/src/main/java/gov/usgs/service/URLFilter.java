package gov.usgs.service;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 *
 * @author jwalker
 */
public class URLFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        if (request instanceof HttpServletRequest) {
            final HttpServletRequest httpReq = (HttpServletRequest)request;
            HttpServletRequestWrapper wrapper = new HttpServletRequestWrapper(httpReq) {
                
                @Override
                public StringBuffer getRequestURL() {
                    String xRequestURL = httpReq.getHeader("X-REQUEST-URL");
                    if (xRequestURL != null) {
                        return new StringBuffer(xRequestURL);
                    }
                    return new StringBuffer(super.getRequestURL());
                }
            };
            chain.doFilter(wrapper, response);
        }
    }

    @Override
    public void destroy() {
    }
    
}
