package gov.usgs.cida.gdp.utilities;

import java.io.InputStream;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Properties;
import java.util.ResourceBundle; 

public abstract class PropertyLoader
{
    /**
     * Looks up a resource named 'name' in the classpath. The resource must map
     * to a file with .properties extention. The name is assumed to be absolute
     * and can use either "/" or "." for package segment separation with an
     * optional leading "/" and optional ".properties" suffix. Thus, the
     * following names refer to the same resource:
     * <pre>
     * some.pkg.Resource
     * some.pkg.Resource.properties
     * some/pkg/Resource
     * some/pkg/Resource.properties
     * /some/pkg/Resource
     * /some/pkg/Resource.properties
     * </pre>
     * 
     * @param name classpath resource name [may not be null]
     * @param loader classloader through which to load the resource [null
     * is equivalent to the application loader]
     * 
     * @return resource converted to java.util.Properties [may be null if the
     * resource was not found and THROW_ON_LOAD_FAILURE is false]
     * @throws IllegalArgumentException if the resource was not found and
     * THROW_ON_LOAD_FAILURE is true
     */
    public static Properties loadProperties (String name, ClassLoader loader)
    {
    	ClassLoader localLoader = loader;
    	String localName = name;
        if (localName == null)
            throw new IllegalArgumentException ("null input: name");
        
        if (localName.startsWith ("/"))
            localName = localName.substring (1);
            
        if (localName.endsWith (SUFFIX))
        	localName = localName.substring (0, localName.length () - SUFFIX.length ());
        
        Properties result = null;
        
        InputStream in = null;
        try
        {
            if (localLoader == null) localLoader = ClassLoader.getSystemClassLoader ();
            
            if (LOAD_AS_RESOURCE_BUNDLE)
            {    
            	localName = localName.replace ('/', '.');
                // Throws MissingResourceException on lookup failures:
                final ResourceBundle rb = ResourceBundle.getBundle (localName,
                    Locale.getDefault (), localLoader);
                
                result = new Properties ();
                for (Enumeration<?> keys = rb.getKeys (); keys.hasMoreElements ();)
                {
                    final String key = (String) keys.nextElement ();
                    final String value = rb.getString (key);
                    
                    result.put (key, value);
                } 
            }
            else
            {
            	localName = localName.replace ('.', '/');
                
                if (! localName.endsWith (SUFFIX))
                	localName = localName.concat (SUFFIX);
                                
                // Returns null on lookup failures:
                in = localLoader.getResourceAsStream (localName);
                if (in != null)
                {
                    result = new Properties ();
                    result.load (in); // Can throw IOException
                }
            }
        }
        catch (Exception e)
        {
            result = null;
        }
        finally
        {
            if (in != null) try { in.close (); } catch (Throwable ignore) { /* Do nothing */ }
        }
        
        if (THROW_ON_LOAD_FAILURE && (result == null))
        {
        	String outString =  "a resource bundle";
            throw new IllegalArgumentException ("could not load [" + localName + "]"+
                " as " + outString);
        }
        
        return result;
    }
    
    /**
     * A convenience overload of {@link #loadProperties(String, ClassLoader)}
     * that uses the current thread's context classloader.
     */
    public static Properties loadProperties (final String name)
    {
        return loadProperties (name, Thread.currentThread().getContextClassLoader());
    }
        
    private static final boolean THROW_ON_LOAD_FAILURE = true;
    private static final boolean LOAD_AS_RESOURCE_BUNDLE = false;
    private static final String SUFFIX = ".properties";
}