package gov.usgs.cida.gdp.dataaccess.helper;

import java.io.IOException;
import static org.junit.Assert.*;
import gov.usgs.cida.gdp.utilities.FileHelper;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.LoggerFactory;

/**
 *
 * @author isuftin
 */
public class WPSHelperTest {
    private static org.slf4j.Logger log = LoggerFactory.getLogger(WPSHelperTest.class);
    private static String sampleDir;
    private static String testFilePath;
    private static final String testFile = "test_zip";
    private static final String secondTestFile = "Yahara_River_HRUs_geo_WGS84";
    private String tempDir = "";
    private String seperator = "";

    public WPSHelperTest() {
    }

        @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        log.debug("Started testing class: " + WPSHelperTest.class.getName());
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        log.debug("Ended testing class: " + WPSHelperTest.class.getName());
    }

    @Before
    public void setUp() throws Exception {
        this.tempDir = System.getProperty("java.io.tmpdir");

        if (!(this.tempDir.endsWith("/") || this.tempDir.endsWith("\\"))) {
            this.tempDir = this.tempDir + System.getProperty("file.separator");
        }

        String systemTempDir = System.getProperty("java.io.tmpdir");
        this.seperator = java.io.File.separator;
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

        sampleDir = this.tempDir + this.seperator
                + "Sample_Files" + this.seperator
                + "Shapefiles" + this.seperator;

        testFilePath = sampleDir + testFile;
    }

    @After
    public void tearDown() {
        File delDir = new File(this.tempDir);
        try {
            FileHelper.deleteDirRecursively(delDir);
        } catch (IOException ex) {
        }
    }

    /**
     * Test of createWPSReceiveFilesXML method, of class WPSHelper.
     */
    @Test
    public void testCreateWPSReceiveFilesXML() throws Exception {
        String result = null;

        result = WPSHelper.createWPSReceiveFilesXML(new File(this.testFilePath + ".zip"), "http://localhost:8081/geoserver", "testFile");

        assertNotNull(result);
    }

}