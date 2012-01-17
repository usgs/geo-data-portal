package ucar.nc2.iosp.geotiff.epsg;

import java.util.Iterator;
import javax.imageio.spi.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.nc2.iosp.geotiff.epsg.csv.CSVEPSGFactoryProvider;

/**
 *
 * @author tkunicki
 */
public class EPSGFactoryManager {
    
    public final static Logger LOGGER = LoggerFactory.getLogger(EPSGFactoryManager.class);
    
    public static EPSGFactoryProvider defaultFactoryProvider;
    
    private static EPSGFactoryManager instance;
    
    public synchronized static EPSGFactoryManager getInstance() {
        if (instance == null) {
            instance = new EPSGFactoryManager();
        }
        return instance;
    }
    
    private boolean multipleProvidersWarning;
    
    private EPSGFactoryManager() {
        multipleProvidersWarning = false;
    }
    
    public synchronized EPSGFactory getEPSGFactory() {
        EPSGFactoryProvider provider = null;
        
        Iterator<EPSGFactoryProvider> iterator =
                ServiceRegistry.lookupProviders(EPSGFactoryProvider.class);
        
        if (iterator.hasNext()) {
            provider = iterator.next();
            if (iterator.hasNext()) {
                if (!multipleProvidersWarning) {
                    LOGGER.warn("Multiple EPSGFactoryProvider services registered. Service ordering is not guaranteed between JVM runtimes!");
                    multipleProvidersWarning = true;
                }
            }
            LOGGER.debug("Using EPSGFactoryProvider instance of type: {}", provider.getClass().getName());
        }
        
        if (provider == null) {
            provider = getDefaultFactoryProvider();
            LOGGER.debug("No EPSGFactoryProvider instances registered using default instance of type: {}", provider.getClass().getName());
        }
        return provider.getEPSGFactory();
    }
    
    protected synchronized EPSGFactoryProvider getDefaultFactoryProvider() {
        if (defaultFactoryProvider == null) {
            defaultFactoryProvider = new CSVEPSGFactoryProvider();
        }
        return defaultFactoryProvider;
    }
}
