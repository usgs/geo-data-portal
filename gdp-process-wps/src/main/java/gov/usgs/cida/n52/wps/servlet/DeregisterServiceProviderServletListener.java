package gov.usgs.cida.n52.wps.servlet;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ServiceRegistry;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Web application lifecycle listener.
 *
 * @author tkunicki
 */
public class DeregisterServiceProviderServletListener implements ServletContextListener {

    private final static Logger LOGGER = LoggerFactory.getLogger(DeregisterServiceProviderServletListener.class);
    
    public DeregisterServiceProviderServletListener() {
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        final ClassLoader classLoader = getClass().getClassLoader();
        final ServiceRegistry serviceRegistry = IIORegistry.getDefaultInstance();
        Iterator<Class<?>> categories = IIORegistry.getDefaultInstance().getCategories();
        if (categories != null) {
            while (categories.hasNext()) {
                Class<?> category = categories.next();
                Iterator<?> providers = serviceRegistry.getServiceProviders(category, false);
                if (providers != null) {
                    List providersToRemove = new ArrayList();
                    while (providers.hasNext()) {
                        Object provider = providers.next();
                        if (provider != null && classLoader.equals(provider.getClass().getClassLoader())) {
                            providersToRemove.add(provider);
                        }
                    }
                    for (Object provider : providersToRemove) {
                        serviceRegistry.deregisterServiceProvider(provider);
                        LOGGER.info("deregistered service provider {} from category {}", provider.getClass().getName(), category.getName());
                    }
                }
            }
        }
    }
}
