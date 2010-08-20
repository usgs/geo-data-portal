package gov.usgs.gdp.interfaces.geotools;

import gov.usgs.cida.gdp.filemanagement.interfaces.geotools.AnalyzeFile;
import static org.junit.Assert.*;
import gov.usgs.cida.gdp.utilities.bean.ShapeFileSetBean;
import gov.usgs.cida.gdp.utilities.FileHelper;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class AnalyzeFileTest {

    private static final String testFile = "demo_HUCs";
    private static org.apache.log4j.Logger log = Logger.getLogger(AnalyzeFileTest.class);

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
    }

    @After
    public void tearDown() throws Exception {
        FileUtils.deleteDirectory((new File(this.tempDir)));
    }

    @Test
    public void createClass() {
        AnalyzeFile result = new AnalyzeFile();
        assertNotNull(result);
    }

    @Test
    public void testGetFileSummaryFromShapeFileSetBean() {
        String shpFile = this.tempDir
                + this.seperator
                + "Sample_Files"
                + this.seperator
                + "Shapefiles"
                + this.seperator
                + testFile + ".shp";

        String dbfFile = this.tempDir
                + this.seperator
                + "Sample_Files"
                + this.seperator
                + "Shapefiles"
                + this.seperator
                + testFile + ".dbf";

        List<String> result = null;

        ShapeFileSetBean testBean = new ShapeFileSetBean();
        testBean.setDbfFile(new File(dbfFile));
        testBean.setShapeFile(new File(shpFile));

        try {
            result = AnalyzeFile.getFileSummary(testBean);
            assertNotNull(result);
            assertFalse(result.isEmpty());
        } catch (IOException e1) {
            fail(e1.getMessage());
        }


        testBean.setDbfFile(null);
        testBean.setShapeFile(null);
        try {
            result = AnalyzeFile.getFileSummary(testBean);
            assertNotNull(result);
            assertTrue(result.isEmpty());
        } catch (IOException e1) {
            fail(e1.getMessage());
        }


        File zeroByteFile = null;
        try {
            zeroByteFile = File.createTempFile("temp", "tmp");
        } catch (IOException e) {
            fail(e.getMessage());
        }

        testBean.setDbfFile(zeroByteFile);
        testBean.setShapeFile(zeroByteFile);
        try {
            result = AnalyzeFile.getFileSummary(testBean);
            assertNotNull(result);
            assertTrue(result.isEmpty());
        } catch (IOException e) {
            fail(e.getMessage());
        }


        testBean.setDbfFile(new File("does/not/exist"));
        testBean.setShapeFile(new File("does/not/exist"));
        try {
            result = AnalyzeFile.getFileSummary(testBean);
            assertNotNull(result);
            assertTrue(result.isEmpty());
        } catch (IOException e) {
            fail(e.getMessage());
        }

    }

    @Test
    public void testGetFileSummary() {
        String shpFile = this.tempDir
                + this.seperator
                + "Sample_Files"
                + this.seperator
                + "Shapefiles"
                + this.seperator
                + testFile + ".shp";

        String dbfFile = this.tempDir
                + this.seperator
                + "Sample_Files"
                + this.seperator
                + "Shapefiles"
                + this.seperator
                + testFile + ".dbf";

        File DBFFILE = new File(dbfFile);
        File SHPFILE = new File(shpFile);

        List<String> result;
        try {
            result = AnalyzeFile.getFileSummary(new File("does/not/exist"));
            assertNotNull(result);
            assertTrue(result.isEmpty());
        } catch (IOException e1) {
            fail(e1.getMessage());
        }


        try {
            result = AnalyzeFile.getFileSummary(dbfFile);
            assertNotNull(result);
            assertFalse(result.isEmpty());
        } catch (IOException e1) {
            fail(e1.getMessage());
        }


        try {
            result = AnalyzeFile.getFileSummary(shpFile);
            assertNotNull(result);
            assertFalse(result.isEmpty());
        } catch (IOException e1) {
            fail(e1.getMessage());
        }


        try {
            result = AnalyzeFile.getFileSummary(DBFFILE);
            assertNotNull(result);
            assertFalse(result.isEmpty());
        } catch (IOException e1) {
            fail(e1.getMessage());
        }


        try {
            result = AnalyzeFile.getFileSummary(SHPFILE);
            assertNotNull(result);
            assertFalse(result.isEmpty());
        } catch (IOException e1) {
            fail(e1.getMessage());
        }


        try {
            File zeroByteFile = File.createTempFile("temp", "tmp");
            result = AnalyzeFile.getFileSummary(zeroByteFile);
            assertNotNull(result);
            assertTrue(result.isEmpty());
        } catch (IOException e) {
            fail(e.getMessage());
        }

        File directory = new File(this.tempDir
                + this.seperator
                + "Sample_Files"
                + this.seperator
                + "Shapefiles"
                + this.seperator);
        try {
            result = AnalyzeFile.getFileSummary(directory);
            assertNotNull(result);
            assertTrue(result.isEmpty());
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }
}
