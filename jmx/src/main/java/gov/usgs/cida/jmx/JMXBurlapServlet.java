package gov.usgs.cida.jmx;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import mx4j.tools.remote.caucho.burlap.BurlapServlet;
import mx4j.tools.remote.http.HTTPConnectorServer;

/**
 *
 * @author tkunicki
 */
public class JMXBurlapServlet extends BurlapServlet {
    
    private Map<JMXServiceURL, JMXConnectorServer> jmxConnectorServerMap;

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        initializeJMXConnectorServer(request);
        super.service(request, response);
    }

    protected synchronized void initializeJMXConnectorServer(HttpServletRequest request) throws IOException {
        
        String contextPath = request.getContextPath();
        String serviceURLPath = contextPath.length() == 1 ?
                request.getServletPath() :
                contextPath + request.getServletPath();
        
        String serviceHostName = request.getServerName();
        int servicePort = request.getLocalPort();
        
        try {
            JMXServiceURL serviceURL =
                    new JMXServiceURL(
                        "burlap",        /* protocol */
                        serviceHostName, /* host */
                        servicePort,     /* port */
                        serviceURLPath); /* URL path */
           
            if (!jmxConnectorServerMap.containsKey(serviceURL)) {                

                MBeanServer server = ManagementFactory.getPlatformMBeanServer();

                Map<String, Object> optionsMap = new HashMap<String, Object>();
                optionsMap.put(HTTPConnectorServer.USE_EXTERNAL_WEB_CONTAINER, Boolean.TRUE);

                JMXConnectorServer jmxConnectorServer =
                        JMXConnectorServerFactory.newJMXConnectorServer(
                            serviceURL, /* service URL */
                            optionsMap, /* options */
                            server);    /* server to bind to */

                jmxConnectorServer.start();
                
                jmxConnectorServerMap.put(serviceURL, jmxConnectorServer);
            }

        } catch (MalformedURLException ex) {
            Logger.getLogger(JMXBurlapServlet.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(JMXBurlapServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        jmxConnectorServerMap = new HashMap<JMXServiceURL, JMXConnectorServer>();
    }
    
    @Override
    public void destroy() {
        super.destroy();
        Iterator<JMXConnectorServer> serverIterator = jmxConnectorServerMap.values().iterator();
        while(serverIterator.hasNext()) {
            JMXConnectorServer jmxConnectorServer = serverIterator.next();
            try {
                jmxConnectorServer.stop();
            } catch (IOException ex) {
                Logger.getLogger(JMXBurlapServlet.class.getName()).log(Level.SEVERE, null, ex);
            }
            serverIterator.remove();
        }
    }
}
