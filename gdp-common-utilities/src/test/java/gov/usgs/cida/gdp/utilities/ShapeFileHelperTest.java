/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.usgs.cida.gdp.utilities;

import gov.usgs.cida.gdp.utilities.bean.Files;
import gov.usgs.cida.gdp.utilities.bean.ShapeFileSet;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import static  org.junit.Assert.*;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author isuftin
 */
public class ShapeFileHelperTest {

    private String tempDir = "";
    private String seperator = "";
    private String testFile = "demo_HUCs";
    List<ShapeFileSet> shapeBeanList = new ArrayList<ShapeFileSet>();;
    
    public ShapeFileHelperTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp(){
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
            try {
                FileHelper.copyFileToFile(sampleFiles, this.tempDir + this.seperator);
            } catch (IOException ex) {
                Logger.getLogger(ShapeFileHelperTest.class.getName()).log(Level.SEVERE, null, ex);
                fail(ex.getMessage());
            }
        } else {
            assertTrue("Sample files could not be loaded for test", false);
        }

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
        shapeFileSetBean.setName("demo_HUCs");
        shapeBeanList.add(shapeFileSetBean);

    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getShapeFileFromShapeSetName method, of class ShapeFileHelper.
     */
    @Test
    public void testGetShapeFileFromShapeSetWithCorrectName() {
        File result = ShapeFileHelper.getShapeFileFromShapeSetName("demo_HUCs", shapeBeanList);
        assertNotNull(result);
    }


    /**
     * Test of getShapeFileFromShapeSetName method, of class ShapeFileHelper.
     */
    @Test
    public void testGetShapeFileFromShapeSetWithIncorrectName() {
        File result = ShapeFileHelper.getShapeFileFromShapeSetName("incorrect", shapeBeanList);
        assertNull(result);
    }

        /**
     * Test of getShapeFileFromShapeSetName method, of class ShapeFileHelper.
     */
    @Test
    public void testGetShapeFilePathFromShapeSetWithCorrectName() {
        String  result = ShapeFileHelper.getShapeFilePathFromShapeSetName("demo_HUCs", shapeBeanList);
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

            /**
     * Test of getShapeFileFromShapeSetName method, of class ShapeFileHelper.
     */
    @Test
    public void testGetShapeFilePathFromShapeSetWithInCorrectName() {
        String  result = ShapeFileHelper.getShapeFilePathFromShapeSetName("incorrect", shapeBeanList);
        assertNull(result);
    }
        /**
     * Test of getShapeFileFromShapeSetName method, of class ShapeFileHelper.
     */
    @Test
    public void testCreateShapefileHelper() {
        ShapeFileHelper result = new ShapeFileHelper();
        assertNotNull(result);
    }

}