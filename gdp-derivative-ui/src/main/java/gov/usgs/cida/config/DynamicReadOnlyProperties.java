package gov.usgs.cida.config;

import java.io.*;
import java.util.Map.Entry;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.naming.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements <i>read-only</i> properties with ${} expansion. Override order is
 * specified by order of arguments. The earlier property sets have higher
 * priority and override latter, with automatic inclusion of environment
 * properties (including command line arguments) as second lowest priority and
 * System properties as lowest priority always included by default. To change
 * the priority of System.properties or environment properties, explicitly
 * include it. The properties are read-only because expanded/substituted
 * properties should not be saved back to file.
 *
 * @author ikuoikuo
 *
 */
public class DynamicReadOnlyProperties extends Properties {

    private final static Logger log = LoggerFactory.getLogger(DynamicReadOnlyProperties.class);
    private static final long serialVersionUID = 5079727277068716415L;
    // ==============
    // STATIC METHODS
    // ==============
    public static final int MAX_SUBSTITUTION_PASSES = 10;
    public static final String[] DEFAULT_JNDI_CONTEXTS = {"java:", "java:comp/env"};

    public static enum NullKeyHandlingOption {

        DO_NOTHING, RETURN_EMPTY_STRING
    };
    public static final NullKeyHandlingOption DEFAULT_HANDLING_OPTION = NullKeyHandlingOption.DO_NOTHING;
    public static Set<String> baseTypes = new HashSet<String>();

    static {
        baseTypes.add(String.class.getName());
        baseTypes.add(Integer.class.getName());
        baseTypes.add(Double.class.getName());
        baseTypes.add(Long.class.getName());
        baseTypes.add(Float.class.getName());
        baseTypes.add(Boolean.class.getName());
        baseTypes.add(Short.class.getName());
        baseTypes.add(Character.class.getName());
        baseTypes.add(Byte.class.getName());
    }
    public static final int JNDI_ERROR_MESSAGE_LIMIT = 1;

    public static DynamicReadOnlyProperties initProps() {
        try {
            return new DynamicReadOnlyProperties().addJNDIContexts((String[]) null);
        } catch (NamingException e) {
            log.warn("Error occured during initProps()", e);
        }
        return null;
    }

    /**
     * Converts a set of properties to a Map<String, String>
     *
     * @param props
     * @return
     */
    public static Map<String, String> asMap(Properties props) {
        Map<String, String> result = new HashMap<String, String>();
        for (Map.Entry<Object, Object> entry : props.entrySet()) {
            Object value = entry.getValue();
            String valueString = (value == null) ? null : value.toString();
            result.put(entry.getKey().toString(), valueString);
        }
        return result;
    }

    public static List<Map<String, String>> asMaps(Properties... props) {
        List<Map<String, String>> result = new ArrayList<Map<String, String>>();
        for (Properties prop : props) {
            if (prop != null) {
                result.add(asMap(prop));
            }
        }
        return result;
    }

    public static StringBuilder readStream2StringBuilder(final InputStream in)
            throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in), 1024 * 1024);
        StringBuilder sb = new StringBuilder();
        String line = reader.readLine();
        while (line != null) {
            sb.append(line);
            line = reader.readLine();
            if (line != null) {
                sb.append("\n"); // normalizes line endings but I don't care
            }
        }
        return sb;
    }
    public static final Pattern SUBST_REGEXP = Pattern.compile("\\$\\{([^}]+)\\}");
    public final static String VALUE_PROPERTY_REGEXREPLACE = "\\$\\{([\\w\\.]+)\\}";

    /**
     * Performs substitution expansion on a master list of Map/properties. Note
     * that this method DOES NOT include System.properties by default and does
     * not expand JNDI properties
     *
     * @param props
     * @param opts optional single parameter for null handling
     * @return
     */
    public static Map<String, String> expand(Map<String, String> props) {
        Map<String, String> result = new HashMap<String, String>();
        Set<String> entriesNeedingSubstitution = new HashSet<String>();
        // First pass: put all properties into result and identify properties which need expanding
        for (Entry<String, String> entry : props.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            Matcher m = SUBST_REGEXP.matcher(value);
            if (m.find()) {
                entriesNeedingSubstitution.add(key);
            }
            result.put(key, value);
        }

        // Substitution passes
        for (int nestingDepth = 1; nestingDepth < MAX_SUBSTITUTION_PASSES && entriesNeedingSubstitution.size() > 0; nestingDepth++) {
            //System.out.println("Starting substitution pass #" + nestingDepth);
            Iterator<String> iter = entriesNeedingSubstitution.iterator();
            while (iter.hasNext()) {
                String expKey = iter.next();
                String value = result.get(expKey);
                value = expand(value, result);
                result.put(expKey, value);
                if (!SUBST_REGEXP.matcher(value).find()) {
                    iter.remove();
                }
            }
        }
        return result;
    }

    /**
     * Performs substitution expansion on a string using the provided list of
     * properties. Note that this method DOES NOT include System.properties by
     * default and does not expand JNDI properties.
     *
     * @param value
     * @param props
     * @return
     */
    public static String expand(String value, Map<String, String> props) {
        Matcher m = SUBST_REGEXP.matcher(value);
        int matchPos = 0;
        while (matchPos < value.length() && m.find(matchPos)) {
            String subKey = m.group(1);
            String subValue = props.get(subKey);
            int subPosStart = m.start();
            matchPos = subPosStart; // need to keep it here in case there is nested replacement

            if (subValue == null) {
                // We cannot complete expansion as this property is not defined. 
                // SKIP this replacement and continue making best effort attempt.
                matchPos++; // need to move the matching past the first match after the matchPos
            } else {
                // Do replacement
                int subPosEnd = m.end();
                value = value.substring(0, subPosStart) + subValue + value.substring(subPosEnd);
                m = SUBST_REGEXP.matcher(value); // matcher must be reset
            }
        }
        return value;
    }

//	/**
//	 * Returns the full jndi name for lookup
//	 * @param jndiContextName
//	 * @param name
//	 * @return
//	 */
//	public static String makeFullJNDIName(String jndiContextName, String name) {
//		return (jndiContextName == null || jndiContextName.isEmpty())? name: jndiContextName + "/" + name;
//	}
    // =======================================================================
    // CONSTRUCTORS
    // =======================================================================
    private DynamicReadOnlyProperties(List<Map<String, String>> properties) {
        // First add System.properties and System.getEnv(). Since this is a private
        // constructor called by everyone else, this is the only place we can do this.
        Map<String, String> map = new HashMap<String, String>();
        map.putAll(asMap(System.getProperties()));
        map.putAll(System.getenv());

        // Load all the properties to create a master list of properties
        for (Map<String, String> props : properties) {
            if (props != null) {
                map.putAll(props);
            }
        }
        // environment properties, including command line

        map = expand(map);
        putAll(map);
    }

    @SuppressWarnings("unchecked")
    public DynamicReadOnlyProperties() {
        this((Map<String, String>) null);
    }

    public DynamicReadOnlyProperties(Map<String, String>... properties) {
        this(Arrays.asList(properties));
    }

    public DynamicReadOnlyProperties(Properties... properties) {
        this(asMaps(properties));
    }

    public DynamicReadOnlyProperties(File propsFile) throws FileNotFoundException, IOException {
        this(loadFileAsProperty(propsFile));
    }

    public DynamicReadOnlyProperties(File propsFile, Properties... properties) throws FileNotFoundException, IOException {
        this(toList(loadFileAsMap(propsFile), asMaps(properties)));
    }

    public DynamicReadOnlyProperties(File propsFile, Map<String, String>... properties) throws FileNotFoundException, IOException {
        this(toList(loadFileAsMap(propsFile), Arrays.asList(properties)));
    }

    public DynamicReadOnlyProperties(Properties commandLineProps, File propsFile, Properties... properties) throws FileNotFoundException, IOException {
        this(toList(asMap(commandLineProps), toList(loadFileAsMap(propsFile), asMaps(properties))));
    }

    public DynamicReadOnlyProperties(Map<String, String> commandLineProps, File propsFile, Map<String, String>... properties) throws FileNotFoundException, IOException {
        this(toList(commandLineProps, toList(loadFileAsMap(propsFile), Arrays.asList(properties))));
    }
    // ================
    // Instance Members
    // ================
    protected boolean isUsingJNDI;
    protected String[] jndiContexts;
    protected int jndiErrorMessageCount;

    /**
     * Replaces and expands property values in the string
     *
     * @param value
     * @return
     */
    public String expand(String value) {
        return expand(value, this.asMap());
    }

    public InputStream expand(final InputStream in) throws IOException {
        // Assuming default encoding for InputStream, not necessarily a good assumption
        StringBuilder input = readStream2StringBuilder(in);
        String result = this.expand(input.toString());
        ByteArrayInputStream byteStream = new ByteArrayInputStream(result.getBytes());
        return byteStream;
    }

    /**
     * Adds JNDI lookups to the properties.
     *
     * @param contexts default = STANDARD_JNDI_CONTEXTS
     * @throws NamingException
     */
    public DynamicReadOnlyProperties addJNDIContexts(String... contexts) throws NamingException {
        isUsingJNDI = true;
        if (contexts == null || contexts.length == 0 || (contexts.length == 1 && contexts[0] == null)) {
            contexts = DEFAULT_JNDI_CONTEXTS;
        }
        jndiContexts = contexts;
        //
        Map<String, String> map = this.asMap();
        Context iCtx = new InitialContext();
        // TODO Are these contexts really needed? Can I just look it up from the initial context?
        for (String context : contexts) {
            Object lookup = null;
            try {
                lookup = iCtx.lookup(context);
            } catch (NamingException e1) {
                log.warn("Error occured during addJNDIContexts()", e1);
            }

            if (lookup != null && lookup instanceof Context) {
                Context ctx = (Context) lookup;
                Map<String, String> jndiValues = extractContext(ctx, context, false, false);
                map.putAll(jndiValues);
            }
        }
        this.putAll(expand(map));
        return this;
    }

    public static String makeFullJNDIName(String jndiContextName, String name) {
        return (jndiContextName == null || jndiContextName.isEmpty()) ? name : jndiContextName + "/" + name;
    }

    /**
     * Extracts the JNDI value as String-String key-value pairs in a
     * java.util.Map
     *
     * @param context
     * @param fullContextName - the full name of the provided context
     * @param useShortNames - true if the abbreviated lookup value is to be used
     * as a key
     * @param addTypeToValue - true if Object values of non-primitive type
     * objects are to be prepended by a string indicating the class name
     * @return
     */
    public static Map<String, String> extractContext(Context context, String fullContextName, boolean useShortNames, boolean addTypeToValue) {
        Map<String, String> result = new HashMap<String, String>();
        try {
            NamingEnumeration<Binding> bindings = context.listBindings("");
            while (bindings.hasMore()) {
                Binding binding = bindings.next();
                String shortName = binding.getName();
                String fullName = makeFullJNDIName(fullContextName, shortName);
                Object boundObject = binding.getObject();
                if (boundObject != null) {
                    if (boundObject instanceof Context) {
                        result.putAll(extractContext((Context) boundObject, fullName, useShortNames, addTypeToValue));
                    } else {
                        String value = boundObject.toString();
                        if (addTypeToValue) {
                            String className = boundObject.getClass().getName();
                            if (!baseTypes.contains(className)) {
                                value = "[" + className + "] " + value;
                            }
                        }

                        result.put(fullName, value);
                        // Make the entry findable by both the full and the short name
                        // This runs the real danger of overwriting previous values, so it's off by default
                        if (useShortNames) {
                            result.put(shortName, value);
                        }
                    }
                }
            }
        } catch (NamingException e) {
            // This is be expected behavior for tomcat's jndi implementation, but it's not clear why
            // What about other jndi implementation?
            if (!"java:/comp".equals(fullContextName)) {
                log.error("[ERROR] unable to get bindings for " + fullContextName);
            }
        }
        return result;
    }

    public String getJNDIPropertyUsingContexts(String key) {
        if (isUsingJNDI && jndiContexts != null) {
            for (String context : jndiContexts) {
                String fullKey = makeFullJNDIName(context, key);
                String value = super.getProperty(fullKey);
                if (value != null) {
                    return value;
                }
            }
        }
        return null;
    }

    // ========================================
    // Overridden Methods from Properties class
    // ========================================
    @Override
    public String get(Object key) {
        if (key == null) {
            return null;
        }
        return getProperty(key.toString());
    }

    @Override
    public String getProperty(String key, String defaultValue) {
        String value = getProperty(key);
        return (value == null) ? defaultValue : value;
    }

    @Override
    public String getProperty(String key) {
        String value = super.getProperty(key);
        if (value == null && isUsingJNDI) {
            return getJNDIPropertyUsingContexts(key);
        }
        return value;
    }

    /**
     * @Deprecated because all the substitutions have been made
     * @see java.util.Properties#list(java.io.PrintStream)
     *
     */
    @Override
    public void list(PrintStream out) {
        super.list(out);
    }

    /**
     * @Deprecated because all the substitutions have been made
     * @see java.util.Properties#list(java.io.PrintWriter)
     */
    @Override
    public void list(PrintWriter out) {
        super.list(out);
    }

    @Override
    public synchronized void load(InputStream inStream) throws IOException {
        super.load(inStream);
        this.expand();
    }

    @Override
    public synchronized void load(Reader reader) throws IOException {
        super.load(reader);
        this.expand();
    }

    @Override
    public synchronized void loadFromXML(InputStream in) throws IOException,
            InvalidPropertiesFormatException {
        super.loadFromXML(in);
        this.expand();
    }

    /**
     * @Deprecated because all the substitutions have been made
     * @see java.util.Properties#save(java.io.OutputStream, java.lang.String)
     * @deprecated
     */
    @Deprecated
    @Override
    public synchronized void save(OutputStream out, String comments) {
        // TODO Auto-generated method stub
        super.save(out, comments);
    }

    /**
     * @Deprecated because all the substitutions have been made
     * @see java.util.Properties#store(java.io.OutputStream, java.lang.String)
     */
    @Override
    public void store(OutputStream out, String comments) throws IOException {
        super.store(out, comments);
    }

    /*
     * @Deprecated because all the substitutions have been made @see
     * java.util.Properties#store(java.io.Writer, java.lang.String)
     */
    @Override
    public void store(Writer writer, String comments) throws IOException {
        super.store(writer, comments);
    }

    /**
     * @Deprecated because all the substitutions have been made
     * @see java.util.Properties#storeToXML(java.io.OutputStream,
     * java.lang.String, java.lang.String)
     */
    @Override
    public synchronized void storeToXML(OutputStream os, String comment,
            String encoding) throws IOException {
        super.storeToXML(os, comment, encoding);
    }

    /**
     * @Deprecated because all the substitutions have been made
     * @see java.util.Properties#storeToXML(java.io.OutputStream,
     * java.lang.String)
     */
    @Override
    public synchronized void storeToXML(OutputStream os, String comment)
            throws IOException {
        super.storeToXML(os, comment);
    }

    public Map<String, String> asMap() {
        return asMap(this);
    }

    /**
     * This method doesn't normally need to be called.
     *
     * @return
     */
    public DynamicReadOnlyProperties expand() {
        Map<String, String> map = this.asMap();
        map = expand(map);
        this.putAll(map);
        return this;
    }

    // ===============
    // Utility Methods
    // ===============
    protected static Properties loadFileAsProperty(File file) throws FileNotFoundException, IOException {
        Properties result = new Properties();
        result.load(new FileReader(file));
        return result;
    }

    protected static Map<String, String> loadFileAsMap(File file) throws FileNotFoundException, IOException {
        return asMap(loadFileAsProperty(file));
    }

    protected static List<Map<String, String>> toList(Map<String, String> map, List<Map<String, String>> maps) {
        List<Map<String, String>> result = new ArrayList<Map<String, String>>();
        result.add(map);
        result.addAll(maps);
        return result;
    }
}
