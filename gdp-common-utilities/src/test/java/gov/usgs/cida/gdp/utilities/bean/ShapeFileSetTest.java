package gov.usgs.cida.gdp.utilities.bean;

import gov.usgs.cida.gdp.utilities.FileHelper;
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

public class ShapeFileSetTest {

    private static final String testFile = "demo_HUCs";
    private static org.slf4j.Logger log = LoggerFactory.getLogger(ShapeFileSetTest.class);

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
    public void testSetGetChosenDataset() {
        ShapeFileSet sfsb = new ShapeFileSet();
        sfsb.setChosenDataset("test");
        assertNotNull(sfsb.getChosenDataset());
        assertEquals(sfsb.getChosenDataset(), "test");
    }

    @Test
    public void testGetSerialversionUID() {
        ShapeFileSet sfsb = new ShapeFileSet();
        long uid = sfsb.getSerialversionuid();
        assertTrue(uid != 0);
    }

    @Test
    public void testGetChosenFeature() {
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
        shapeFileSetBean.setChosenFeature("test");
        String result = shapeFileSetBean.getChosenFeature();
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(result, "test");
    }

    @Test
    public void testGetFeatureListWhenNull() {
        ShapeFileSet sfs = new ShapeFileSet();
        sfs.setFeatureList(null);
        List<String> result = sfs.getFeatureList();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetFeatureListWhenNotNull() {
        ShapeFileSet sfs = new ShapeFileSet();
        List<String> input = new ArrayList<String>();
        input.add("test");
        sfs.setFeatureList(input);
        List<String> result = sfs.getFeatureList();
        assertNotNull(result);
        assertEquals(result.get(0), "test");
    }

    @Test
    public void testGetAttributeListWhenNull() {
        ShapeFileSet sfs = new ShapeFileSet();
        sfs.setAttributeList(null);
        List<String> result = sfs.getAttributeList();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testGetAttributeListWhenNotNull() {
        ShapeFileSet sfs = new ShapeFileSet();
        List<String> input = new ArrayList<String>();
        input.add("test");
        sfs.setAttributeList(input);
        List<String> result = sfs.getAttributeList();
        assertNotNull(result);
        assertEquals(result.get(0), "test");
    }

    @Test
    public void testGetDBFileFromBean() {
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
        assertEquals(shapeFileSetBean.getDbfFile(), new File(dbfFile));
    }

     @Test
    public void testGetProjectionFileFromBean() {
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
        assertEquals(shapeFileSetBean.getProjectionFile(), new File(prjFile));
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

    @Test
    public void testGetShapeFileSetBeanFromFilesBeanWithAllFieldsNull() {
        ShapeFileSet result = null;
        Files filesBean = new Files();
        filesBean.setFiles(new ArrayList<File>());
        result = ShapeFileSet.getShapeFileSetBeanFromFilesBean(filesBean);
        assertNull(result);
    }

    @Test
    public void testGetShapeFileSetBeanFromFilesBeanListWithNullFilesBeanList() {
        ShapeFileSet sfs = ShapeFileSet.getShapeFileSetBeanFromFilesBeanList(new ArrayList<Files>(), testFile);
        assertNull(sfs);
    }

        @Test
    public void testGetShapeFileSetBeanFromFilesBeanList() {
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
        files.add(new File(shxFile));
        filesBean.setFiles(files);
        List<Files> input = new ArrayList<Files>();
        filesBean.setName(testFile + ".dbf");
        input.add(filesBean);
        result = ShapeFileSet.getShapeFileSetBeanFromFilesBeanList(input, testFile + ".dbf");
        assertNotNull(result);
    }

          @Test
    public void testGetShapeFileSetBeanFromFilesBeanListWithWrongFilesBeanName() {
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
        files.add(new File(shxFile));
        filesBean.setFiles(files);
        List<Files> input = new ArrayList<Files>();
        filesBean.setName("wrong");
        input.add(filesBean);
        result = ShapeFileSet.getShapeFileSetBeanFromFilesBeanList(input, testFile + ".dbf");
        assertNull(result);
    }

     @Test
    public void testGetShapeFileIndexSetBeanFromFilesBean() {
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
        files.add(new File(shxFile));
        filesBean.setFiles(files);
        result = ShapeFileSet.getShapeFileSetBeanFromFilesBean(filesBean);
        assertNotNull(result);
        assertEquals(result.getShapeFileIndexFile(),new File(shxFile));
    }

}
