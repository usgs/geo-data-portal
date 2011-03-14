/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.usgs.cida.gdp.utilities;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
/**
 *
 * @author isuftin
 */
public class XMLUtils {
    private static org.slf4j.Logger log = LoggerFactory.getLogger(XMLUtils.class);

    private XMLUtils() {}

    public static XPathExpression createXPathExpression(final String expression) throws XPathExpressionException {
        XPathFactory xPathFactory = XPathFactory.newInstance();
        XPath xPath = xPathFactory.newXPath();
        XPathExpression xPathExpression = xPath.compile(expression);
        return xPathExpression;
    }

    public static Boolean createBooleanUsingXPathExpression(String expression, String xml) throws XPathExpressionException, UnsupportedEncodingException {
        return (Boolean) XMLUtils.createXPathExpression(expression).evaluate(new InputSource(new ByteArrayInputStream(xml.getBytes("utf-8"))), XPathConstants.BOOLEAN);
    }

    public static NodeList createNodeListUsingXPathExpression(String expression, String xml) throws XPathExpressionException, UnsupportedEncodingException {
        return (NodeList) XMLUtils.createXPathExpression(expression).evaluate(new InputSource(new ByteArrayInputStream(xml.getBytes("utf-8"))), XPathConstants.NODESET);
    }

    public static Node createNodeUsingXPathExpression(String expression, String xml) throws XPathExpressionException, UnsupportedEncodingException {
        return (Node) XMLUtils.createXPathExpression(expression).evaluate(new InputSource(new ByteArrayInputStream(xml.getBytes("utf-8"))), XPathConstants.NODE);
    }

}
