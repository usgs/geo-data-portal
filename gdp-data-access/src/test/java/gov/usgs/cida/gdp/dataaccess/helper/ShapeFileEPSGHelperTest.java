/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.usgs.cida.gdp.dataaccess.helper;

import java.io.File;
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
public class ShapeFileEPSGHelperTest {

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
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getShapeFile method, of class ShapeFileEPSGHelper.
     */
    @Test
    public void testGetShapeFile() {
        System.out.println("getShapeFile");
        ShapeFileEPSGHelper instance = new ShapeFileEPSGHelper();
        File expResult = new File(System.getProperty("java.io.tmpdir"));
        instance.setZippedShapeFile(expResult);
        File result = instance.getZippedShapeFile();
        assertEquals(expResult, result);
    }

    /**
     * Test of setZippedShapeFile method, of class ShapeFileEPSGHelper.
     */
    @Test
    public void testSetShapeFile() {
        System.out.println("setShapeFile");
        ShapeFileEPSGHelper instance = new ShapeFileEPSGHelper();
        File expResult = new File(System.getProperty("java.io.tmpdir"));
        instance.setZippedShapeFile(expResult);
        File result = instance.getZippedShapeFile();
        assertEquals(expResult, result);
    }

}