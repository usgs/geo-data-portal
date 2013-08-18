package gov.usgs.cida.gdp.wps.util;

import com.ctc.wstx.stax.WstxInputFactory;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.geotools.gml3.ApplicationSchemaConfiguration;
import org.geotools.gml3.GMLConfiguration;
import org.geotools.referencing.CRS;
import org.geotools.xml.Configuration;
import org.geotools.xml.Parser;
import org.n52.wps.io.SchemaRepository;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tkunicki
 */
public class GMLUtil {
	private final static Logger LOGGER = LoggerFactory.getLogger(GMLUtil.class);
	private static final WstxInputFactory INPUTFACTORY_XML;

	static {
		INPUTFACTORY_XML = new WstxInputFactory();
		INPUTFACTORY_XML.configureForSpeed();
	}

	public static CoordinateReferenceSystem extractCRS(File file) {
		InputStream inputStream = null;
		CoordinateReferenceSystem crs = null;
		try {
			inputStream = new BufferedInputStream(new FileInputStream(file));
			crs = extractCRS(inputStream);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(inputStream);
		}
		return crs;
	}

	public static CoordinateReferenceSystem extractCRS(InputStream inputStream) {
		CoordinateReferenceSystem crs = null;
		XMLStreamReader xmlStreamReader = null;
		try {
			xmlStreamReader = INPUTFACTORY_XML.createXMLStreamReader(inputStream);
			int eventType = xmlStreamReader.nextTag();
			
			while (eventType == XMLStreamReader.START_ELEMENT
					&& "xml-fragment".equals(xmlStreamReader.getLocalName())) {
				eventType = xmlStreamReader.nextTag();
			}
			
			if (eventType == XMLStreamReader.START_ELEMENT) {
				// We are now within the XML document and need to get to the first 
				// node that we expect to be a boundedBy node. If it's not a 
				// boundedBy node, we give up
				eventType = xmlStreamReader.nextTag();
				if (eventType == XMLStreamReader.START_ELEMENT) {
					String tagLN = xmlStreamReader.getLocalName();
					if (tagLN.equalsIgnoreCase("boundedby")) {
						// The first node is, in fact, a boundedBy node. The next
						// node should be an Envelope node with srsName info.
						xmlStreamReader.nextTag();
						String crsText = xmlStreamReader.getAttributeValue(null, "srsName");
						if (StringUtils.isNotBlank(crsText)) {
							crs = CRS.decode(crsText);
						}
					}
				}
			}
			
		} catch (XMLStreamException ex) {
			throw new RuntimeException("Error determining CRS", ex);
		} catch (NoSuchAuthorityCodeException ex) {
			throw new RuntimeException("Error determining CRS", ex);
		} catch (FactoryException ex) {
			throw new RuntimeException("Error determining CRS", ex);
		} finally {
			if (xmlStreamReader != null) {
				try {
					xmlStreamReader.close();
				} catch (XMLStreamException ex) {
					LOGGER.debug(ex.getMessage());
				}
			}
		}
		return crs;
	}

	public static QName extractFeatureTypeSchema(File file) {
		QName qName = null;
		InputStream inputStream = null;
		try{
			inputStream = new BufferedInputStream(new FileInputStream(file), 16 << 10);
			qName = extractFeatureTypeSchema(inputStream);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException ex) {
					LOGGER.debug(ex.getMessage());
				}
			}
		}
		return qName;
	}

	public static QName extractFeatureTypeSchema(InputStream inputStream) {
		QName qName = null;
		XMLStreamReader xmlStreamReader = null;
		try {
			xmlStreamReader = INPUTFACTORY_XML.createXMLStreamReader(inputStream);
			int eventType = xmlStreamReader.nextTag();
			// 1) find START_ELEMENT event for root element, may have to
			//    jump past "xml-fragment" if needed
			while (eventType == XMLStreamReader.START_ELEMENT &&
					"xml-fragment".equals(xmlStreamReader.getLocalName()) ) {
				eventType = xmlStreamReader.nextTag();
			}
			// 2) parse root element for
			if (eventType == XMLStreamReader.START_ELEMENT) {
				String schemaLocation = xmlStreamReader.getAttributeValue(
						XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI,
						"schemaLocation");
				if (schemaLocation != null && schemaLocation.length() > 0) {
					String[] split = schemaLocation.split("\\s+");
					if (split.length > 0 && split.length % 2 == 0) {
                        for (int i = 0 ; i < split.length && qName == null; i += 2) {
                            if (split[i].length() > 0 &&
                                    !"http://www.opengis.net/wfs".equals(split[i]) &&
                                    !"http://www.opengis.net/gml".equals(split[i])) {
                                qName = new QName(split[i], split[i+1]);
							}
						}
					}
				}
			}
		} catch (XMLStreamException e) {
			throw new RuntimeException("Error determining feature type schema location", e);
		} finally {
			if (xmlStreamReader != null) {
				try {
					xmlStreamReader.close();
				} catch (XMLStreamException e) {
					LOGGER.debug(e.getMessage());
				}
			}
		}
		return qName;
	}

	public static Configuration generateGMLConfiguration(File file) {
		Configuration configuration = null;
		InputStream inputStream = null;
		try{
			inputStream = new BufferedInputStream(new FileInputStream(file), 16 << 10);
			configuration = generateGMLConfiguration(inputStream);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (inputStream != null) try { inputStream.close(); } catch (IOException e) { }
				}
		return configuration;
	}

	public static Configuration generateGMLConfiguration(InputStream inputStream) {
		QName featureTypeSchema = GMLUtil.extractFeatureTypeSchema(inputStream);
		if (featureTypeSchema == null) {
			throw new RuntimeException("featureTypeSchema null for inputStream");
		}

		String schemaLocation = featureTypeSchema.getLocalPart();

		Configuration configuration = null;
		if (schemaLocation != null && featureTypeSchema.getNamespaceURI() != null) {
			SchemaRepository.registerSchemaLocation(featureTypeSchema.getNamespaceURI(), schemaLocation);
			configuration = new ApplicationSchemaConfiguration(featureTypeSchema.getNamespaceURI(), schemaLocation);
		} else {
			configuration = new GMLConfiguration();
			configuration.getProperties().add(Parser.Properties.IGNORE_SCHEMA_LOCATION);
			configuration.getProperties().add(Parser.Properties.PARSE_UNKNOWN_ELEMENTS);
		}
		return configuration;
	}
}
