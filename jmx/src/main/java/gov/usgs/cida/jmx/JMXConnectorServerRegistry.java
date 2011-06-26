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
import javax.servlet.http.HttpServletRequest;
import mx4j.tools.remote.http.HTTPConnectorServer;

/**
 *
 * @author tkunicki
 */
public class JMXConnectorServerRegistry {
    
    private final static Logger LOGGER = Logger.getLogger(JMXConnectorServerRegistry.class.getName());
    
    private final String jmxProtocol;
    private final Map<JMXServiceURL, JMXConnectorServer> jmxConnectorServerMap;
    
    
    JMXConnectorServerRegistry(String jmxProtocol) {
        this.jmxProtocol = jmxProtocol;
        jmxConnectorServerMap =  new HashMap<JMXServiceURL, JMXConnectorServer>();
    }
    
    public synchronized void initializeJMXConnectorServer(HttpServletRequest request) throws IOException {
        
        String contextPath = request.getContextPath();
        String serviceURLPath = contextPath.length() == 1 ?
                request.getServletPath() :
                contextPath + request.getServletPath();
        
        String serviceHostName = request.getServerName();
        int servicePort = request.getLocalPort();
        
        try {
            JMXServiceURL serviceURL =
                    new JMXServiceURL(
                        jmxProtocol,     /* protocol */
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
                
                LOGGER.log(Level.INFO, "Started JMX Connector Server for Service URL {0}", serviceURL.getURLPath());
                
                jmxConnectorServerMap.put(serviceURL, jmxConnectorServer);
            }

        } catch (MalformedURLException ex) {
            LOGGER.log(Level.SEVERE, "Exception thrown generating JMX Connector Server: {0}", ex);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Exception thrown generating JMX Connector Server: {0}", ex);
        }
    }
    
    public void destory() {
        Iterator<JMXConnectorServer> serverIterator = jmxConnectorServerMap.values().iterator();
        while(serverIterator.hasNext()) {
            JMXConnectorServer jmxConnectorServer = serverIterator.next();
            try {
                jmxConnectorServer.stop();
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "Exception thrown stopping JMX Connector Server: {0}", ex);
            }
            serverIterator.remove();
        }
    }
}
