package gov.usgs.cida.gdp.filemanagement.bean;

import gov.usgs.cida.gdp.utilities.FileHelper;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.slf4j.LoggerFactory;
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

private static org.slf4j.Logger log = LoggerFactory.getLogger(SummaryBeanTest.class);
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
            Summary result = null;
            assertNull(result);

            result = new Summary();
            assertNotNull(result);
            assertEquals("", result.getFileName());
            assertNotNull(result.getFileSummary());
            assertTrue(result.getFileSummary().isEmpty());

            result = new Summary("Test");
            assertNotNull(result);
            assertEquals("Test", result.getFileName());
            assertNull(result.getFileSummary());

            result = new Summary("Test", new ArrayList<String>());
            assertNotNull(result);
            assertEquals("Test", result.getFileName());
            assertNotNull(result.getFileSummary());
            assertTrue(result.getFileSummary().isEmpty());
        }

    /**
     * Test of getFileName method, of class Summary.
     */
    @Test
    public void testGetFileName() {
        System.out.println("getFileName");
        Summary instance = new Summary();
        String expResult = "";
        String result = instance.getFileName();
        assertEquals(expResult, result);        
    }

    /**
     * Test of setFileName method, of class Summary.
     */
    @Test
    public void testSetFileName() {
        System.out.println("setFileName");
        String fileName = "";
        Summary instance = new Summary();
        instance.setFileName(fileName);
        String result = instance.getFileName();
        assertEquals("", result);
    }

    /**
     * Test of getFileSummary method, of class Summary.
     */
    @Test
    public void testGetFileSummary() {
        System.out.println("getFileSummary");
        Summary instance = new Summary();
        instance.setFileSummary(new ArrayList<String>());
        List<String> expResult = new ArrayList<String>();
        List<String> result = instance.getFileSummary();
        assertEquals(expResult, result);
        
    }

    /**
     * Test of setFileSummary method, of class Summary.
     */
    @Test
    public void testSetFileSummary() {
        System.out.println("getFileSummary");
        Summary instance = new Summary();
        instance.setFileSummary(new ArrayList<String>());
        List<String> expResult = new ArrayList<String>();
        List<String> result = instance.getFileSummary();
        assertEquals(expResult, result);
    }

}