package gov.usgs.cida.gdp.wps.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author jwalker
 */
public class ProcessStatusTest {

	@Test
	public void testIsSuccess() throws XPathExpressionException, AddressException, MessagingException, FileNotFoundException, IOException, SAXException, ParserConfigurationException {
		FileInputStream fis = new FileInputStream(getResourceDir() + "/WPS_Success.xml");
		Document document = CheckProcessCompletion.parseDocument(fis);
		ProcessStatus procStat = new ProcessStatus(document);
		assertTrue("Successfully parsed as succeeded", procStat.isSuccess());
	}

	@Test
	public void testIsFailed() throws XPathExpressionException, AddressException, MessagingException, FileNotFoundException, IOException, SAXException, ParserConfigurationException {
		FileInputStream fis = new FileInputStream(getResourceDir() + "/WPS_Failure.xml");
		Document document = CheckProcessCompletion.parseDocument(fis);
		ProcessStatus procStat = new ProcessStatus(document);
		assertTrue("Successfully parsed as failed", procStat.isFailed());
	}

	@Test
	public void testGetOutputReference() throws XPathExpressionException, AddressException, MessagingException, FileNotFoundException, IOException, SAXException, ParserConfigurationException {
		FileInputStream fis = new FileInputStream(getResourceDir() + "/WPS_Success.xml");
		Document document = CheckProcessCompletion.parseDocument(fis);
		ProcessStatus procStat = new ProcessStatus(document);
		assertEquals("Successfully parsed as succeeded", "http://gdpurl.gov/success", procStat.getOutputReference());
	}

	@Test
	public void testIsStarted() throws XPathExpressionException, AddressException, MessagingException, FileNotFoundException, IOException, SAXException, ParserConfigurationException {
		FileInputStream fis = new FileInputStream(getResourceDir() + "/WPS_Started.xml");
		Document document = CheckProcessCompletion.parseDocument(fis);
		ProcessStatus procStat = new ProcessStatus(document);
		assertTrue("Successfully parsed as started", procStat.isStarted());
	}

	@Test
	public void testGetPercentComplete() throws XPathExpressionException, AddressException, MessagingException, FileNotFoundException, IOException, SAXException, ParserConfigurationException {
		FileInputStream fis = new FileInputStream(getResourceDir() + "/WPS_Started.xml");
		Document document = CheckProcessCompletion.parseDocument(fis);
		ProcessStatus procStat = new ProcessStatus(document);
		assertEquals("Successfully parsed as started", "36", procStat.getPercentComplete());
	}

	@Test
	@Ignore
	/* Need to figure out the ExceptionReportParsing */
	public void testGetFailureMessage() throws FileNotFoundException, IOException, SAXException, ParserConfigurationException, XPathExpressionException {
		FileInputStream fis = new FileInputStream(getResourceDir() + "/WPS_Failure.xml");
		Document document = CheckProcessCompletion.parseDocument(fis);
		ProcessStatus procStat = new ProcessStatus(document);
		assertEquals("Successfully parsed as failed", "BigError", procStat.getFailureMessage());
	}

	private synchronized static String getResourceDir() {
		String RESOURCE_PATH = null;
        ClassLoader cl = ProcessStatusTest.class.getClassLoader();
            URL sampleFileLocation = cl.getResource("Sample_Responses" + File.separator);
            try {
                RESOURCE_PATH = new File(sampleFileLocation.toURI()).getPath();
            } catch (URISyntaxException e) {
                RESOURCE_PATH = new File(sampleFileLocation.getPath()).getPath();
            }
        return RESOURCE_PATH;
    }
}
