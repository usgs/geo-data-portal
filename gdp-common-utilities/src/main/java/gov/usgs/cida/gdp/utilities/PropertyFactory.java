package gov.usgs.cida.gdp.utilities;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import org.slf4j.LoggerFactory;
 
/**
 * Factory class for returning String values for given keys
 * 
 * @author isuftin
 *
 */
public class PropertyFactory {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(PropertyFactory.class);
    private static Properties properties = null;

    // Static class - can not be instantiated
    private PropertyFactory() {
        // Empty private constructor
    }

    // Returns all the keys available to the application
    static public Enumeration<Object> getKeys() {
        if (properties == null) {
            log.debug("Loading properties file");
            try {
                loadProperties();
            } catch (RuntimeException e) {
                log.error(e.getMessage());
            }
            log.debug("Loaded properties file");
        }
        return properties.keys();
    }

    /**
     * Get a property from the factory or JVM property file.
     *
     * @param key A key for the property
     * @return a property based on key given, "" if not found
     */
    static public String getProperty(String key) {
        if (key == null) return null;

        // No real way to test this chunk of code - would require
        // properties file to not exist during testing
        if (properties == null) {
            log.debug("Loading properties file");
            loadProperties();
            log.debug("Finished Loading properties file");
        }
        
        String result = null;

        // First check to see if the property exists in the JVM
        result = System.getProperty(key);

        // Check if we have a property value. If not, try getting it from the
        // properties file.
        if (result == null) result = (String) properties.get(key);

        // Check again if we have a property. If not, set the output to empty string.
        if (result == null) {
            // Log that the property could not be found.
            log.info("unable to find property for key: " + key);
            result = "";
        }
        
        return result;
    }

    /**
     * Overwrite a property during runtime.
     * @param key
     * @param property
     */
    static public void setProperty(String key, String property) {
        // Well, if there was anything there before, it's been changed.
        properties.setProperty(key, property);
    }

    /**
     * Uses the PropertyLoader class to cleverly load properties file
     *
     * @throws RuntimeException
     */
    private static void loadProperties() throws RuntimeException {
        properties = PropertyLoader.loadProperties("application.properties");
        if (properties == null) {
            throw new RuntimeException("Unable to load properties file");
        }
    }

    public static List<String> getValueList(String key) {
        List<String> result = new ArrayList<String>();
        int index = 0;
        String valueResult = PropertyFactory.getProperty(key + "." + index);

        while (!"".equals(valueResult)) {
            index++;
            result.add(valueResult);
            valueResult = PropertyFactory.getProperty(key + "." + index);
        }

        return result;
    }
}
