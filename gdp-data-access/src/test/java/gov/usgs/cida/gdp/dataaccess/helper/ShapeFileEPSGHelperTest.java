/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.usgs.cida.gdp.dataaccess.helper;

import gov.usgs.cida.gdp.utilities.FileHelper;
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
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.opengis.referencing.FactoryException;

/**
 *
 * @author isuftin
 */
public class ShapeFileEPSGHelperTest {

    private File prjFile = null;
    private String tempDir = "";
    private String seperator = "";
    private String testFile = "demo_HUCs";
    private List<ShapeFileSet> shapeBeanList = new ArrayList<ShapeFileSet>();
    private String wkt = "PROJCS[\"NAD_1983_Albers\",GEOGCS[\"GCS_North_American_1983\",DATUM[\"D_North_American_1983\",SPHEROID[\"GRS_1980\",6378137.0,298.257222101]],PRIMEM[\"Greenwich\",0.0],UNIT[\"Degree\",0.0174532925199433]],PROJECTION[\"Albers\"],PARAMETER[\"False_Easting\",0.0],PARAMETER[\"False_Northing\",0.0],PARAMETER[\"Central_Meridian\",-96.0],PARAMETER[\"Standard_Parallel_1\",29.5],PARAMETER[\"Standard_Parallel_2\",45.5],PARAMETER[\"Latitude_Of_Origin\",23.0],UNIT[\"Meter\",1.0]]";


    public ShapeFileEPSGHelperTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

 @Before
    public void setUp() {
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
                Logger.getLogger(ShapeFileEPSGHelperTest.class.getName()).log(Level.SEVERE, null, ex);
                fail(ex.getMessage());
            }
        } else {
            assertTrue("Sample files could not be loaded for test", false);
        }
        String prjFile = this.tempDir
                + this.seperator
                + "Sample_Files"
                + this.seperator
                + "Shapefiles"
                + this.seperator
                + testFile + ".prj";

        this.prjFile = new File(prjFile);

    }

    @After
    public void tearDown() {
        File delDir = new File(this.tempDir);
        try {
            FileHelper.deleteDirRecursively(delDir);
        } catch (IOException ex) {
            Logger.getLogger(ShapeFileEPSGHelperTest.class.getName()).log(Level.SEVERE, "Failed to delete: " + delDir.getPath() + "  -- Remember to clean project or remove this file/dir.", ex);
        }
    }

    /**
     * Test of getShapeFile method, of class ShapeFileEPSGHelper.
     */
    @Test
    public void testGetShapeFileDirectory() {
        System.out.println("getShapeFile");
        File expResult = new File(System.getProperty("java.io.tmpdir"));
        ShapeFileEPSGHelper instance = new ShapeFileEPSGHelper(expResult);
        instance.setPrjFile(expResult);
        File result = instance.getPrjFile();
        assertEquals(expResult, result);
    }

    /**
     * Test of setPrjFile method, of class ShapeFileEPSGHelper.
     */
    @Test
    public void testSetShapeFileDirectory() {
        System.out.println("setShapeFile");
        File expResult = new File(System.getProperty("java.io.tmpdir"));
        ShapeFileEPSGHelper instance = new ShapeFileEPSGHelper(expResult);
        instance.setPrjFile(expResult);
        File result = instance.getPrjFile();
        assertEquals(expResult, result);
    }

    /**
     * Test of setPrjFile method, of class ShapeFileEPSGHelper.
     */
    @Test
    public void testGetEpsgFromPrj() {
        System.out.println("getEpsgFromPrj");
        String result = null;
        try {
            result = ShapeFileEPSGHelper.getEpsgFromPrj(this.prjFile);
        } catch (IOException ex) {
            Logger.getLogger(ShapeFileEPSGHelperTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FactoryException ex) {
            Logger.getLogger(ShapeFileEPSGHelperTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        String expResult = "EPSG:4269";
        assertEquals(expResult, result);
    }

    /**
     * Test of setPrjFile method, of class ShapeFileEPSGHelper.
     */
    @Test
    public void testGetEpsgFromWkt() {
        System.out.println("getEpsgFromWkt");
        String result = null;
        try {
            result = ShapeFileEPSGHelper.getEpsgFromWkt(wkt);
        } catch (FactoryException ex) {
            Logger.getLogger(ShapeFileEPSGHelperTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        String expResult = "EPSG:5070";
        assertEquals(expResult, result);
    }

}