/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.usgs.gdp.bean;

import gov.usgs.gdp.helper.FileHelper;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author isuftin
 */
public class SummaryBeanTest {

    public SummaryBeanTest() {
    }
private static org.apache.log4j.Logger log = Logger.getLogger(SummaryBeanTest.class);
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		log.debug("Started testing class");
	}
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		log.debug("Ended testing class");
	}
	private String tempDir = "";

	private String seperator = "";

	@Before
	public void setUp() throws Exception {
		this.tempDir = System.getProperty("java.io.tmpdir");

		if ( !(this.tempDir.endsWith("/") || this.tempDir.endsWith("\\")) )
		   this.tempDir = this.tempDir + System.getProperty("file.separator");
		String systemTempDir = System.getProperty("java.io.tmpdir");
		this.seperator =  java.io.File.separator;
		String currentTime = Long.toString((new Date()).getTime());
		this.tempDir = systemTempDir + this.seperator + currentTime;
		(new File(this.tempDir)).mkdir();

		// Copy example files
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		URL sampleFileLocation = cl.getResource("Sample_Files/");
		if (sampleFileLocation != null) {
			File sampleFiles = null;
			try {
				sampleFiles = new File(sampleFileLocation.toURI());
			} catch (URISyntaxException e) {
				assertTrue("Exception encountered: " + e.getMessage(), false);
			}
			FileHelper.copyFileToFile(sampleFiles, this.tempDir + this.seperator);
		} else {
			assertTrue("Sample files could not be loaded for test", false);
		}
	}

	@After
	public void tearDown() throws Exception {
		FileUtils.deleteDirectory((new File(this.tempDir)));
	}

        @Test
        public void testConstructors() {
            System.out.println("testConstructors");
            SummaryBean result = null;
            assertNull(result);

            result = new SummaryBean();
            assertNotNull(result);
            assertEquals("", result.getFileName());
            assertNotNull(result.getFileSummary());
            assertTrue(result.getFileSummary().isEmpty());

            result = new SummaryBean("Test");
            assertNotNull(result);
            assertEquals("Test", result.getFileName());
            assertNull(result.getFileSummary());

            result = new SummaryBean("Test", new ArrayList<String>());
            assertNotNull(result);
            assertEquals("Test", result.getFileName());
            assertNotNull(result.getFileSummary());
            assertTrue(result.getFileSummary().isEmpty());
        }

    /**
     * Test of getFileName method, of class SummaryBean.
     */
    @Test
    public void testGetFileName() {
        System.out.println("getFileName");
        SummaryBean instance = new SummaryBean();
        String expResult = "";
        String result = instance.getFileName();
        assertEquals(expResult, result);        
    }

    /**
     * Test of setFileName method, of class SummaryBean.
     */
    @Test
    public void testSetFileName() {
        System.out.println("setFileName");
        String fileName = "";
        SummaryBean instance = new SummaryBean();
        instance.setFileName(fileName);
        String result = instance.getFileName();
        assertEquals("", result);
    }

    /**
     * Test of getFileSummary method, of class SummaryBean.
     */
    @Test
    public void testGetFileSummary() {
        System.out.println("getFileSummary");
        SummaryBean instance = new SummaryBean();
        instance.setFileSummary(new ArrayList<String>());
        List expResult = new ArrayList<String>();
        List result = instance.getFileSummary();
        assertEquals(expResult, result);
        
    }

    /**
     * Test of setFileSummary method, of class SummaryBean.
     */
    @Test
    public void testSetFileSummary() {
        System.out.println("getFileSummary");
        SummaryBean instance = new SummaryBean();
        instance.setFileSummary(new ArrayList<String>());
        List expResult = new ArrayList<String>();
        List result = instance.getFileSummary();
        assertEquals(expResult, result);
    }

}