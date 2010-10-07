package gov.usgs.cida.gdp.utilities.bean;

import static org.junit.Assert.*;
import gov.usgs.cida.gdp.utilities.FileHelper;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.LoggerFactory;

public class AvailableFilesTest {

    private String tempDir = "";
    private String seperator = "";
    private static org.slf4j.Logger log = LoggerFactory.getLogger(AvailableFilesTest.class);

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        log.debug("Started testing class.");
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        log.debug("Ended testing class.");
    }

    @Before
    public void setUp() throws Exception {
        this.seperator = FileHelper.getSeparator();
        this.tempDir = FileHelper.getSystemTemp()
                + this.seperator
                + "GDP-APP-TEMP"
                + this.seperator
                + "testing-feel-free-to-delete"
                + this.seperator;
        (new File(this.tempDir)).mkdir();
        (new File(this.tempDir + "user_dir" + this.seperator)).mkdir();

        // Copy example files
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        URL sampleFileLocation = cl.getResource("Sample_Files" + FileHelper.getSeparator());
        if (sampleFileLocation != null) {
            File sampleFiles = null;
            try {
                sampleFiles = new File(sampleFileLocation.toURI());
            } catch (URISyntaxException e) {
                assertTrue("Exception encountered: " + e.getMessage(), false);
            }
            FileHelper.copyFileToFile(sampleFiles, this.tempDir + this.seperator);
            FileHelper.copyFileToFile(sampleFiles, this.tempDir + "user_dir" + this.seperator);
        } else {
            assertTrue("Sample files could not be loaded for test", false);
        }
    }

    @After
    public void tearDown() throws Exception {
        FileUtils.deleteDirectory((new File(this.tempDir)));
    }

    @Test
    public void testGetAvailableFilesBeanWithEmptyStringDirectory() {
        AvailableFiles result =
                AvailableFiles.getAvailableFilesBean("", "");

        assertNull(result);
    }

    @Test
    public void testGetAvailableFilesBeanWithNullDirectory() {
        AvailableFiles result =
                AvailableFiles.getAvailableFilesBean(null, null);

        assertNull(result);
    }

    @Test
    public void testGetAvailableFilesBeanWithPopulatedExamplesDirectoryNoUserDirectory() {
        AvailableFiles result =
                AvailableFiles.getAvailableFilesBean(this.tempDir);

        assertNotNull(result);
        assertNotNull(result.getExampleFileList());
        assertFalse(result.getExampleFileList().isEmpty());
        assertNotNull(result.getExampleFileList());
        assertFalse(result.getShapeSetList().isEmpty());
        assertTrue(result.getUserFileList().isEmpty());
    }

    @Test
    public void testGetAvailableFilesBeanWithPopulatedExamplesDirectory() {
        AvailableFiles result =
                AvailableFiles.getAvailableFilesBean(this.tempDir, null);

        assertNotNull(result);
        assertNotNull(result.getExampleFileList());
        assertFalse(result.getExampleFileList().isEmpty());
        assertNotNull(result.getExampleFileList());
        assertFalse(result.getShapeSetList().isEmpty());
        assertTrue(result.getUserFileList().isEmpty());
    }

    @Test
    public void testGetAvailableFilesBeanWithPopulatedExamplesDirectoryAndQuotesForUserDir() {
        AvailableFiles result =
                AvailableFiles.getAvailableFilesBean(this.tempDir, "");

        assertNotNull(result);
        assertNotNull(result.getExampleFileList());
        assertFalse(result.getExampleFileList().isEmpty());
        assertNotNull(result.getExampleFileList());
        assertFalse(result.getShapeSetList().isEmpty());
        assertTrue(result.getUserFileList().isEmpty());
    }

    @Test
    public void testSetShapesetListWithPopulatedExamplesDirectory() {
        AvailableFiles result =
                AvailableFiles.getAvailableFilesBean(this.tempDir);

        result.setShapeSetList(null);
        assertEquals(result.getShapeSetList().size(), 0);
    }

    @Test
    public void testSetExampleFileList() {
        AvailableFiles result = new AvailableFiles();
        List<Files> fileList = new ArrayList<Files>();
        fileList.add(new Files());

        result.setExampleFileList(fileList);
        assertNotNull(result.getExampleFileList());
        assertFalse(result.getExampleFileList().isEmpty());
    }

    @Test
    public void testSetUserFileList() {
        AvailableFiles result = new AvailableFiles();
        List<Files> fileList = new ArrayList<Files>();
        fileList.add(new Files());

        result.setUserFileList(fileList);
        assertNotNull(result.getUserFileList());
        assertFalse(result.getUserFileList().isEmpty());
    }

    @Test
    public void testGetAvailableFilesBeanWithPopulatedUserDirectory() {
        AvailableFiles result =
                AvailableFiles.getAvailableFilesBean(this.tempDir,
                this.tempDir + "user_dir"
                + this.seperator
                + "Sample_Files"
                + this.seperator
                + "Shapefiles"
                + this.seperator);
        assertNotNull(result);
        assertNotNull(result.getExampleFileList());
        assertFalse(result.getExampleFileList().isEmpty());
        assertNotNull(result.getExampleFileList());
        assertFalse(result.getShapeSetList().isEmpty());
        assertFalse(result.getUserFileList().isEmpty());


    }

    @Test
    public void testGetAvailableFilesBeanWithBogusUserDirectory() {
        try {
            @SuppressWarnings("unused")
            AvailableFiles result =
                    AvailableFiles.getAvailableFilesBean(this.tempDir,
                    this.tempDir + "user_dir"
                    + this.seperator
                    + "Sample_Files"
                    + this.seperator
                    + "test"
                    + this.seperator);
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);

        }
    }
}
