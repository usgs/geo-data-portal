package gov.usgs.service;

import javax.servlet.http.HttpServletRequest;

/**
 * Yanked from https://issues.apache.org/bugzilla/show_bug.cgi?id=28222#c16
 * The process is described a bit better @ http://www.caucho.com/resin-3.0/webapp/faq.xtp
 * Also http://tomcat.apache.org/tomcat-7.0-doc/servletapi/constant-values.html
 * 
 * Forwarded Request Parameters
 * 
 * Except for servlets obtained by using the getNamedDispatcher method, a servlet that has been invoked by another servlet using the forward method of RequestDispatcher has access to the path of the original request.
 * 
 * The following request attributes must be set:
 * 
 * javax.servlet.forward.request_uri
 * javax.servlet.forward.context_path
 * javax.servlet.forward.servlet_path
 * javax.servlet.forward.path_info
 * javax.servlet.forward.query_string
 * 
 * The values of these attributes must be equal to the return values of the HttpServletRequest methods getRequestURI, getContextPath, getServletPath, getPathInfo, getQueryString respectively, invoked on the request object passed to the first servlet object in the call chain that received the request from the client. These attributes are accessible from the forwarded servlet via the getAttribute method on the request object. Note that these attributes must always reflect the information in the original request even under the situation that multiple forwards and subsequent includes are called.
 * 
 * If the forwarded servlet was obtained by using the getNamedDispatcher method, these attributes must not be set. 
 * 
 * @author Ivan Suftin <isuftin@usgs.gov>
 */
public class URLUtility {
    private static final String REQUEST_URI_STRING = "javax.servlet.forward.request_uri";
    private static final String QUERY_STRING = "javax.servlet.forward.query_string";
    private URLUtility() {}
    
    /** 
     * Recreates the full URL that originally got the web client to the given 
     * request.  This takes into account changes to the request due to request 
     * dispatching.
     *
     * <p>Note that if the protocol is HTTP and the port number is 80 or if the
     * protocol is HTTPS and the port number is 443, then the port number is not 
     * added to the return string as a convenience.</p>
     * @param request
     * @return  
     */
    public static String getReturnURL(HttpServletRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Cannot take null parameters.");
        }

        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();

        // Try to get the forwarder value first, only if it's empty fall back to the
        // current value
        String requestUri = (String) request.getAttribute(REQUEST_URI_STRING);
        requestUri = (requestUri == null) ? request.getRequestURI() : requestUri;

        // Try to get the forwarder value first, only if it's empty fall back to the
        // current value 
        String queryString = (String) request.getAttribute(QUERY_STRING);
        queryString = (queryString == null) ? request.getQueryString() : queryString;

        StringBuilder buffer = new StringBuilder();
        buffer.append(scheme);
        buffer.append("://");
        buffer.append(serverName);

        //if not http:80 or https:443, then add the port number
        if (!(scheme.equalsIgnoreCase("http") && serverPort == 80) && !(scheme.equalsIgnoreCase("https") && serverPort == 443)) {
            buffer.append(":").append(String.valueOf(serverPort));
        }

        buffer.append(requestUri);

        if (queryString != null) {
            buffer.append("?");
            buffer.append(queryString);
        }

        return buffer.toString();
    }
}
