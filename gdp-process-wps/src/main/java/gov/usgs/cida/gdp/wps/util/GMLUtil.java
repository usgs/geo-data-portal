package gov.usgs.cida.gdp.wps.util;

import com.ctc.wstx.stax.WstxInputFactory;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.geotools.geometry.jts.ReferencedEnvelope;
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
	 private static final Logger LOGGER = LoggerFactory.getLogger(GMLUtil.class);
	 private static final WstxInputFactory INPUTFACTORY_XML;
	 private static final String MSG_ERR_DETERMINE_ENVELOPE = "Error determining envelope";
	 private static final String MSG_ERR_DETERMINE_CRS = "Error determining CRS";
	static {
		INPUTFACTORY_XML = new WstxInputFactory();
		INPUTFACTORY_XML.configureForSpeed();
	}
	
	/**
	 * In GML where there is a single boundedBy node that sets the envelope and
	 * CRS for the entire collection, extract the Envelope and CRS for that envelope by streaming
	 * the GML
	 * 
	 * @param file
	 * @return
	 * @throws IOException 
	 */
	public static ReferencedEnvelope determineCollectionEnvelope(File file) throws IOException {
		CoordinateReferenceSystem crs = determineCollectionCRS(file);
		return determineCollectionEnvelope(file, crs);
	}
	
	/**
	 * Determine envelope of GML feature collection using given CRS
	 * 
	 * @see GMLUtil#determineCollectionEnvelope(java.io.File) 
	 * @param file
	 * @param crs
	 * @return
	 * @throws IOException 
	 */
	public static ReferencedEnvelope determineCollectionEnvelope(File file, CoordinateReferenceSystem crs) throws IOException {
		InputStream inputStream = null;
		ReferencedEnvelope envelope = null;
		try {
			inputStream = new BufferedInputStream(new FileInputStream(file));
			envelope = determineCollectionEnvelope(inputStream, crs);
		} catch (FileNotFoundException ex) {
			throw new IOException(MSG_ERR_DETERMINE_ENVELOPE, ex);
		} finally {
			IOUtils.closeQuietly(inputStream);
		}
		return envelope;
	}
	
	/**
	 * @see GMLUtil#determineCollectionEnvelope(java.io.File, org.opengis.referencing.crs.CoordinateReferenceSystem) 
	 * @param inputStream
	 * @param crs
	 * @return
	 * @throws IOException 
	 */
	public static ReferencedEnvelope determineCollectionEnvelope(InputStream inputStream, CoordinateReferenceSystem crs) throws IOException {
		XMLStreamReader xmlStreamReader;
		ReferencedEnvelope envelope = null;
		CoordinateReferenceSystem envelopeCrs = null;
		Double[] upperCorner = new Double[2];
		Double[] lowerCorner = new Double[2];
		
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
						// We're int he pipe, five by five
						xmlStreamReader.nextTag();
						
						// We are now in the Envelope tag. We safeguard ourselves 
						// from a null incoming crs by re-doing the work of the 
						// determineCollectionCRS function. 
						if (crs != null) {
							envelopeCrs = crs;
						} else {
							String crsText = xmlStreamReader.getAttributeValue(null, "srsName");
							if (StringUtils.isNotBlank(crsText)) {
								try {
									envelopeCrs = CRS.decode(crsText);
								} catch (NoSuchAuthorityCodeException ex) {
									throw new IOException(MSG_ERR_DETERMINE_ENVELOPE, ex);
								} catch (FactoryException ex) {
									throw new IOException(MSG_ERR_DETERMINE_ENVELOPE, ex);
								}
							} else {
								throw new IOException(MSG_ERR_DETERMINE_ENVELOPE);
							}
						}
						
						
						
						// Moving on, we go on to the upper/lower corner nodes
						while(xmlStreamReader.nextTag() != XMLStreamReader.END_ELEMENT) {
							tagLN = xmlStreamReader.getLocalName();
							if (tagLN.equalsIgnoreCase("lowercorner")) {
								String[] dblStringArr = xmlStreamReader.getElementText().split(" ");
								lowerCorner[0] = Double.parseDouble(dblStringArr[0]);
								lowerCorner[1] = Double.parseDouble(dblStringArr[1]);
							} else if (tagLN.equalsIgnoreCase("uppercorner")) {
								String[] dblStringArr = xmlStreamReader.getElementText().split(" ");
								upperCorner[0] = Double.parseDouble(dblStringArr[0]);
								upperCorner[1] = Double.parseDouble(dblStringArr[1]);
							}
						}
						if (lowerCorner[0] + lowerCorner[1] != 0d && upperCorner[0] + upperCorner[1] != 0d) {
							envelope = new ReferencedEnvelope(lowerCorner[0], upperCorner[0], lowerCorner[1], upperCorner[1], envelopeCrs);
						}
					}
				}
			}

		} catch (XMLStreamException ex) {
			throw new IOException(MSG_ERR_DETERMINE_ENVELOPE, ex);
		}

		return envelope;
	}

	/**
	 * In GML where there is a single boundedBy node that sets the envelope and
	 * CRS for the entire collection, extract the CRS for that envelope by streaming
	 * the GML
	 * 
	 * @param file
	 * @return Returns a CoordinateReferenceSystem object or null if CRS could not be 
	 * parsed
	 * @throws IOException 
	 */
	public static CoordinateReferenceSystem determineCollectionCRS(File file) throws IOException {
		InputStream inputStream = null;
		CoordinateReferenceSystem crs = null;
		try {
			inputStream = new BufferedInputStream(new FileInputStream(file));
			crs = determineCollectionCRS(inputStream);
		} catch (FileNotFoundException ex) {
			throw new IOException(MSG_ERR_DETERMINE_CRS, ex);
		} finally {
			IOUtils.closeQuietly(inputStream);
		}
		return crs;
	}

	/**
	 * @see GMLUtil#determineCollectionCRS(java.io.File) 
	 * @param inputStream
	 * @return
	 * @throws IOException 
	 */
	public static CoordinateReferenceSystem determineCollectionCRS(InputStream inputStream) throws IOException {
		XMLStreamReader xmlStreamReader = null;
		CoordinateReferenceSystem crs = null;
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
			throw new IOException(MSG_ERR_DETERMINE_CRS, ex);
		} catch (NoSuchAuthorityCodeException ex) {
			throw new IOException(MSG_ERR_DETERMINE_CRS, ex);
		} catch (FactoryException ex) {
			throw new IOException(MSG_ERR_DETERMINE_CRS, ex);
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

		Configuration configuration;
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
