package gov.usgs.cida.jmx.log4j;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.ReflectionException;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggerRepository;

/**
 *
 * @author tkunicki
 */
public class JMXLog4JLogger implements DynamicMBean {
    
    private final static Logger LOGGER = Logger.getLogger(JMXLog4JLogger.class);

    @Override
    public Object getAttribute(String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException {
        Logger logger = convertToLogger(attribute);
        if (logger == null) {
            throw new AttributeNotFoundException("\"" + attribute + "\" is not a known logger");
        }
        return logger.getEffectiveLevel().toString();
    }

    @Override
    public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
        String attributeName = attribute.getName();
        Logger logger = convertToLogger(attributeName);
        if (logger == null) {
            throw new AttributeNotFoundException("Attribute name \"" +attributeName + "\" is not a known logger");
        }
        Object attributeValue = attribute.getValue();
        if (!(attributeValue instanceof String)) {
            throw new InvalidAttributeValueException("Attribute value must a String");
        }
        String attributeLevelAsString = (String)attributeValue;
        Level level = convertToLevel(attributeLevelAsString);
        if (level == null) {
            throw new InvalidAttributeValueException("Attribute value \"" + attributeLevelAsString + "\" not a known Log4J log level");
        }
        logger.setLevel(level);
        LOGGER.info("Set log4j log level for " + logger.getName() + " to " + level);
    }

    @Override
    public AttributeList getAttributes(String[] attributes) {
        AttributeList attributeList = new AttributeList(attributes.length);
        LoggerRepository loggerRepository = LogManager.getLoggerRepository();
        for (String attribute : attributes) {
            Logger logger = convertToLogger(attribute);
            attributeList.add(new Attribute(attribute, logger == null ? null : logger.getEffectiveLevel().toString()));
        }
        return attributeList;
    }

    @Override
    public AttributeList setAttributes(AttributeList attributes) {
        AttributeList attributeList = new AttributeList();
        for (Attribute attribute : attributes.asList()) {
            Logger logger = convertToLogger(attribute.getName());
            if (logger != null) {
                Object attributeValue = attribute.getValue();
                if (attributeValue instanceof String) {
                    Level level = convertToLevel((String)attributeValue);
                    if (!logger.getEffectiveLevel().equals(level)) {
                        logger.setLevel(level);
                        LOGGER.info("Set log4j log level for " + logger.getName() + " to " + level);
                        attributeList.add(new Attribute(attribute.getName(), level.toString()));
                    } else {
                        LOGGER.debug("Did not set log4j log level for " + logger.getName() + " to " + level + ", level matched existing effective level");
                    }
                }
            }
        }
        return attributeList;
    }

    @Override
    public Object invoke(String actionName, Object[] params, String[] signature) throws MBeanException, ReflectionException {
        throw new UnsupportedOperationException("Unsupported, use attribute(s) interface");
    }

    @Override
    public MBeanInfo getMBeanInfo() {
        return new MBeanInfo(
                getClass().getCanonicalName(),
                "Set/Get Log4J logger levels as MBean attributes",
                null, /* MBeanAttributeInfo[] */
                null, /* MBeanConstructorInfo[] */
                null, /* MBeanOperationInfo[] */
                null  /* MBeanNotificationInfo */);
    }
    
    private Level convertToLevel(String attributeValue) {
        if (attributeValue instanceof String) {
            String attributeValueAsString = ((String)attributeValue).toUpperCase();
            Level level = Level.toLevel(attributeValueAsString);
            if (attributeValueAsString.equals(level.toString())) {
                return level;
            }
        }
        return null;
    }
    
    private Logger convertToLogger(String attributeName) {
        return attributeName == null || attributeName.length() == 0 || ".".equals(attributeName) ?
            LogManager.getRootLogger() :
            LogManager.getLoggerRepository().getLogger(attributeName);
    }
}
