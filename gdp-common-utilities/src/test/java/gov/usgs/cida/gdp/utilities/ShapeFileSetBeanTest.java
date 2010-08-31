package gov.usgs.cida.gdp.utilities;

import gov.usgs.cida.gdp.utilities.bean.Files;
import gov.usgs.cida.gdp.utilities.bean.ShapeFileSet;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.slf4j.LoggerFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ShapeFileSetBeanTest {
	
	private static final String testFile = "demo_HUCs";
	
    private static org.slf4j.Logger log = LoggerFactory.getLogger(ShapeFileSetBeanTest.class);

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
    public void testGetAttributeListWithNullMemberVariable() {
        ShapeFileSet sfsb = new ShapeFileSet();
        List<String> result = sfsb.getAttributeList();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetAttributeListFromBean() {
        String shpFile = this.tempDir
                + this.seperator
                + "Sample_Files"
                + this.seperator
                + "Shapefiles"
                + this.seperator
                + testFile + ".shp";

        String prjFile = this.tempDir
                + this.seperator
                + "Sample_Files"
                + this.seperator
                + "Shapefiles"
                + this.seperator
                + testFile + ".prj";

        String dbfFile = this.tempDir
                + this.seperator
                + "Sample_Files"
                + this.seperator
                + "Shapefiles"
                + this.seperator
                + testFile + ".dbf";

        Files filesBean = new Files();
        Collection<File> files = new ArrayList<File>();
        files.add(new File(shpFile));
        files.add(new File(prjFile));
        files.add(new File(dbfFile));
        filesBean.setFiles(files);
        ShapeFileSet shapeFileSetBean = filesBean.getShapeFileSetBean();
        assertNotNull(shapeFileSetBean);
        List<String> result = null;
        try {
            result = ShapeFileSet.getAttributeListFromBean(shapeFileSetBean);
        } catch (IOException e) {
            assertNotNull(result);
        }

        assertNotNull(result);
        if (result != null) {
            assertFalse(result.isEmpty());
        }
    }

    @Test
    public void testGetFeatureListFromBean() {
        String shpFile = this.tempDir
                + this.seperator
                + "Sample_Files"
                + this.seperator
                + "Shapefiles"
                + this.seperator
                + testFile + ".shp";

        String prjFile = this.tempDir
                + this.seperator
                + "Sample_Files"
                + this.seperator
                + "Shapefiles"
                + this.seperator
                + testFile + ".prj";

        String dbfFile = this.tempDir
                + this.seperator
                + "Sample_Files"
                + this.seperator
                + "Shapefiles"
                + this.seperator
                + testFile + ".dbf";

        Files filesBean = new Files();
        Collection<File> files = new ArrayList<File>();
        files.add(new File(shpFile));
        files.add(new File(prjFile));
        files.add(new File(dbfFile));
        filesBean.setFiles(files);
        ShapeFileSet shapeFileSetBean = filesBean.getShapeFileSetBean();
        shapeFileSetBean.setChosenAttribute("HUC_8");
        List<String> result = null;
        try {
            result = ShapeFileSet.getFeatureListFromBean(shapeFileSetBean);
            assertNotNull(result);
            assertFalse(result.isEmpty());
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testGetShapeFileSetBeanFromFilesBean() {
        String shpFile = this.tempDir
                + this.seperator
                + "Sample_Files"
                + this.seperator
                + "Shapefiles"
                + this.seperator
                + testFile + ".shp";

        String prjFile = this.tempDir
                + this.seperator
                + "Sample_Files"
                + this.seperator
                + "Shapefiles"
                + this.seperator
                + testFile + ".prj";

        String dbfFile = this.tempDir
                + this.seperator
                + "Sample_Files"
                + this.seperator
                + "Shapefiles"
                + this.seperator
                + testFile + ".dbf";

        String shxFile = this.tempDir
                + this.seperator
                + "Sample_Files"
                + this.seperator
                + "Shapefiles"
                + this.seperator
                + testFile + ".shx";

        ShapeFileSet result = null;
        Files filesBean = new Files();
        Collection<File> files = new ArrayList<File>();
        files.add(new File(shpFile));
        files.add(new File(prjFile));
        files.add(new File(dbfFile));

        result = ShapeFileSet.getShapeFileSetBeanFromFilesBean(filesBean);
        assertNull(result);

        files.add(new File(shxFile));
        filesBean.setFiles(files);
        result = ShapeFileSet.getShapeFileSetBeanFromFilesBean(filesBean);
        assertNotNull(result);
    }
}
