package gov.usgs.cida.jmx.log4j;

import java.lang.management.ManagementFactory;
import javax.management.DynamicMBean;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;


public class JMXLog4JLoggerServletContextListener implements ServletContextListener {

	private static final Logger LOGGER = Logger.getLogger(JMXLog4JLoggerServletContextListener.class);

    @Override
	public void contextInitialized(ServletContextEvent sce) {

        Logger rootLogger = LogManager.getRootLogger();
        DynamicMBean mbean = new JMXLog4JLogger();
        MBeanServer server  = ManagementFactory.getPlatformMBeanServer();

        
        try {
            ObjectName objectName = getObjectName(sce);
            server.registerMBean(mbean, getObjectName(sce));
            LOGGER.info("Registered Log4J JMX Logger MBean " + objectName.getCanonicalName());
        } catch (Exception e) {
            LOGGER.error("Problem registering Log4J Logger MBean", e);
        }
	}
	
    @Override
	public void contextDestroyed(ServletContextEvent sce) {
		try {
            MBeanServer server  = ManagementFactory.getPlatformMBeanServer();
            ObjectName objectName = getObjectName(sce);
			if (server.isRegistered(getObjectName(sce))) {
				server.unregisterMBean(getObjectName(sce));
                LOGGER.info("Unregistered Log4J JMX Logger MBean " + objectName.getCanonicalName());
			} else {
                LOGGER.warn("Did not unregistered Log4J JMX Logger MBean " + objectName.getCanonicalName() + ", was not registered");
            }
		} catch (Exception e) {
			LOGGER.error("Problem unregistering Log4J Logger MBean", e);
		} 
	}
	
	private ObjectName getObjectName(ServletContextEvent sce) throws MalformedObjectNameException {
        ServletContext context = sce.getServletContext();
        String webappName = context.getContextPath().substring(1);
        return new ObjectName(webappName, "logger", "level");
            
	}
}
