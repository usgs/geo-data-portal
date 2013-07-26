package gov.usgs.cida.gdp.utilities;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
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

    public static String transformXML(final InputStream xml, final InputStream xslt) throws TransformerConfigurationException, TransformerException {
        TransformerFactory tFact = TransformerFactory.newInstance();
        Transformer transformer = tFact.newTransformer(new StreamSource(xslt));
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        transformer.transform(
                new StreamSource(xml),
                new StreamResult(bos)
            );
        String transformedXML = bos.toString();
        return transformedXML;
    }
    
    public static String createPrettyXML(Document document) throws IOException {
            String rawXML;
            OutputFormat format = new OutputFormat(document);
            format.setLineWidth(65);
            format.setIndenting(true);
            format.setIndent(2);
            Writer out = new StringWriter();
            XMLSerializer serializer = new XMLSerializer(out, format);
            serializer.serialize(document);
            rawXML = out.toString();
            return rawXML;
    }
    
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
