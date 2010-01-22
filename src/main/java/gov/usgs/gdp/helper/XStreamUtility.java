package gov.usgs.gdp.helper;

import com.thoughtworks.xstream.XStream;

public class XStreamUtility {
	public static void simpleAlias(XStream xstream, Class<?> clazz) {
		xstream.alias(clazz.getSimpleName(), clazz);
	}
}
