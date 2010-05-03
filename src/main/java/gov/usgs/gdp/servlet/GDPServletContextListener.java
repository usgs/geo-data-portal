package gov.usgs.gdp.servlet;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;

/**
 * Application Lifecycle Listener implementation class GSPServletContextListener
 * 
 */
public class GDPServletContextListener implements ServletContextListener {

    /**
     * Default constructor.
     */
    public GDPServletContextListener() {
        // TODO Auto-generated constructor stub
    }

    /**
     * @see ServletContextListener#contextInitialized(ServletContextEvent)
     */
    @Override
    public void contextInitialized(ServletContextEvent sre) {
        // TODO Clean up temp files from previous application instance
        // (unexpected JVM exit)
    }

    /**
     * @see ServletContextListener#contextDestroyed(ServletContextEvent)
     */
    @Override
    public void contextDestroyed(ServletContextEvent sre) {
        // TODO Clean up temp files from this application instance (unexpected
        // JVM exit)

        // Clean up after MultiThreadedHttpConnectionManager
        // NOTE: Check class loaders to guarantee
        // MultiThreadedHttpConnectionManager is associated with this
        // Servlet Context's ClassLoader
        if (MultiThreadedHttpConnectionManager.class.getClassLoader() == 
                Thread.currentThread().getContextClassLoader()) {
            MultiThreadedHttpConnectionManager.shutdownAll();
        }
    }
}
