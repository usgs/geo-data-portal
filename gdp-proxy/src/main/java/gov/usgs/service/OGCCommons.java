package gov.usgs.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author jwalker
 */
public class OGCCommons {

	protected static Logger ogclog = LoggerFactory.getLogger(OGCCommons.class);

	/**
	 * This extracts the Operational endpoints from a getCapabilities document
	 * Tested to support:
	 * - WPS 1.0.0
	 * - CSW 2.0.2
	 * - WCS 2.0.0
	 * - WCS 1.1
	 * - WFS 2.0.0
	 * - Others may work
	 *
	 * WMS 1.3.0 definitely does not work, do not proxy these
	 * @param capabiltiesRoot GetCapabilities document or root element node
	 * @return Set of endpoints found
	 * @throws XPathExpressionException
	 */
	public static Set<Endpoint> getOperationEndpoints(Node rootDocumentNode) {
		Set<Endpoint> endpointSet = new HashSet<Endpoint>();
		try {
			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();
			String bigLongXpath = "//*[local-name() = 'OperationsMetadata']/*[local-name() = 'Operation']/*[local-name() = 'DCP']/*[local-name() = 'HTTP']/*/@href";
			XPathExpression expression = xpath.compile(bigLongXpath);
			NodeList operationEndpoints = (NodeList) expression.evaluate(rootDocumentNode, XPathConstants.NODESET);
			for (int i = 0; i < operationEndpoints.getLength(); i++) {
				Node n = operationEndpoints.item(i);
				Endpoint end = new Endpoint(n.getNodeValue());
				endpointSet.add(end);
			}
			return endpointSet;
		}
		catch (XPathExpressionException xpee) {
		}
		finally {
			return endpointSet;
		}
	}

	public static boolean isOWSEndpoint(Endpoint owsEndpoint) {
		if (owsEndpoint.getType() != Endpoint.EndpointType.UNKNOWN) {
			Document doc = getCapabilitiesDocument(owsEndpoint);

			if (doc == null) {
				return false;
			}
			Node node = doc.getFirstChild();
			while (node != null && node.getNodeType() == Document.COMMENT_NODE) {
				 node = node.getNextSibling();
			}
                        if (node == null) {
                            return false;
                        }
                        
			String nodeName = node.getNodeName();
			if (nodeName == null) {
				return false;
			}
			
			Pattern pattern = Pattern.compile("(?:\\w+:)?(?:\\w{3}_)?Capabilities");
			Matcher matcher = pattern.matcher(nodeName);
			if (matcher.matches()) {
				ogclog.debug("Response contained capabilities element, adding to cache and proxying");
				return true;
			}

		}
		ogclog.debug("Does not look like an OWS endpoint");
		return false;
	}

	public static Document getCapabilitiesDocument(Endpoint endpoint) {
		Document doc = null;
        InputStream inputStream = null;
		try {
			URL getCapsUrl = endpoint.generateGetCapabilitiesURL();
			ogclog.debug("Sending getCapabilities to: " + getCapsUrl.toString());
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
            inputStream = getCapsUrl.openStream();
			doc = db.parse(inputStream);
		}
		catch (SAXException se) {
			ogclog.debug("SAX threw an exception", se);
		}
		catch (IOException ioe) {
			ogclog.debug("IOException in isOWSEndpoint", ioe);
		}
		catch (ParserConfigurationException pce) {
			ogclog.debug("Error with XML Document Parsing", pce);
		}
		finally {
            IOUtils.closeQuietly(inputStream);
			return doc;
		}
	}
}
