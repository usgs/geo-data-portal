package gov.usgs.cida.gdp.wps.util;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 *
 * @author tkunicki
 */
public class XPathWrapper {
    public static final String DEFAULT_REGEX = "\\s+";
    private final XPath xpath;
    private final Document document;

    public XPathWrapper(XPath xpath, Document document) {
        this.xpath = xpath;
        this.document = document;
    }

    public String textAsString(String expression) throws XPathExpressionException {
        return xpath.evaluate(expression, document);
    }

    public String[] textAsStringArray(String expression) throws XPathExpressionException {
        return textAsStringArray(expression, DEFAULT_REGEX);
    }

    public String[] textAsStringArray(String expression, String regex) throws XPathExpressionException {
        String string = xpath.evaluate(expression, document);
        if (string != null && string.length() > 0) {
            return string.split(regex);
        } else {
            return new String[0];
        }
    }

    public double[] textAsDoubleArray(String expression) throws XPathExpressionException {
        return textAsDoubleArray(expression, DEFAULT_REGEX);
    }

    public double[] textAsDoubleArray(String expression, String regex) throws XPathExpressionException {
        String[] split = textAsStringArray(expression, regex);
        double[] doubles = new double[split.length];
        for (int i = 0; i < split.length; ++i) {
            doubles[i] = Double.parseDouble(split[i]);
        }
        return doubles;
    }

    public String[] nodeListTextContentAsStringArray(String expression) throws XPathExpressionException {
        Object object = xpath.evaluate(expression, document, XPathConstants.NODESET);
        if (object instanceof NodeList) {
            NodeList nodeList = (NodeList) object;
            String[] strings = new String[nodeList.getLength()];
            for (int i = 0; i < nodeList.getLength(); ++i) {
                strings[i] = nodeList.item(i).getTextContent();
            }
            return strings;
        } else {
            return new String[0];
        }
    }

}
