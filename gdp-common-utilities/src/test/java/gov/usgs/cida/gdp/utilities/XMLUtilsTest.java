/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.usgs.cida.gdp.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import static org.junit.Assert.*;

/**
 *
 * @author isuftin
 */
public class XMLUtilsTest {

    String testXml = new StringBuilder("<?xml version=\"1.0\" encoding=\"utf-8\"?>")
        .append("<parentNode>")
        .append("<childNode>").append("1").append("</childNode>")
        .append("<childNode>").append("2").append("</childNode>")
        .append("<childNode>").append("3").append("</childNode>")
        .append("<childNode>").append("true").append("</childNode>")
        .append("<childNode>").append("false").append("</childNode>")
        .append("</parentNode>").toString();

    public XMLUtilsTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void transformXMLTest() throws FileNotFoundException, TransformerConfigurationException, TransformerException {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        String fileSeparator = System.getProperty("file.separator");
        URL xmlFileLocation = cl.getResource("Sample_Files"+fileSeparator+"XSLT"+fileSeparator+"sample.xml");
        URL xsltFileLocation = cl.getResource("Sample_Files"+fileSeparator+"XSLT"+fileSeparator+"sample.xsl");
        File sampleXMLFile = null;
        File xsltFile = null;
        try {
            sampleXMLFile = new File(xmlFileLocation.toURI());
            xsltFile = new File(xsltFileLocation.toURI());
        } catch (URISyntaxException e) {
            assertTrue("Exception encountered: " + e.getMessage(), false);
        }
        
        assertTrue(sampleXMLFile.exists());
        assertTrue(xsltFile.exists());
        
        FileInputStream xmlFis = new FileInputStream(sampleXMLFile);
        FileInputStream xslFis = new FileInputStream(xsltFile);
        String result = XMLUtils.transformXML(xmlFis, xslFis);
        assertTrue(result != null);
        assertTrue(result.contains("Input: FEATURE_ATTRIBUTE_NAME"));
        assertTrue(result.contains("Data: 1999-12-30T00:00:00.000Z"));
        assertTrue(result.contains("Input: FEATURE_COLLECTION"));
        assertTrue(result.contains("< Complex Value Reference (not shown) >"));
        assertTrue(result.contains("Mime Type: text/csv"));
    }
    
    @Test
    public void createNodeUsingXPathExpressionTest() throws XPathExpressionException, UnsupportedEncodingException {
        Node result = XMLUtils.createNodeUsingXPathExpression("/parentNode/childNode[1]", testXml);
        assertThat(result.getTextContent(), equalTo("1"));

        result = XMLUtils.createNodeUsingXPathExpression("/parentNode/childNode[2]", testXml);
        assertThat(result.getTextContent(), equalTo("2"));
    }

    @Test
    public void createNodeListUsingXPathExpressionTest() throws XPathExpressionException, UnsupportedEncodingException {
        NodeList result = XMLUtils.createNodeListUsingXPathExpression("/parentNode/childNode", testXml);
        assertThat(result.getLength(), equalTo(5));
        assertThat(result.item(0).getTextContent(), equalTo("1"));
        assertThat(result.item(1).getTextContent(), equalTo("2"));
        assertThat(result.item(2).getTextContent(), equalTo("3"));
    }

    @Test
    public void createBooleanUsingXPathExpressionTest() throws XPathExpressionException, UnsupportedEncodingException {
        Boolean result = XMLUtils.createBooleanUsingXPathExpression("/parentNode/childNode[4]", testXml);
        assertThat(result, equalTo(Boolean.TRUE));

        result = XMLUtils.createBooleanUsingXPathExpression("/parentNode/childNode[6]", testXml);
        assertThat(result, equalTo(Boolean.FALSE));
    }
}