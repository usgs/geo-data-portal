/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.usgs.gdp.servlet;

import javax.servlet.ServletConfig;
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
public class AppInitializationServletTest {

    public AppInitializationServletTest() {
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
     * Test of init method, of class AppInitializationServlet.
     */
    @Test
    public void testInit() throws Exception {
        ServletConfig config = null;
        AppInitializationServlet instance = new AppInitializationServlet();
        instance.init(config);
        assertTrue(true); // If we got to here, the servlet was init'd
    }

    /**
     * Test of destroy method, of class AppInitializationServlet.
     */
    @Test
    public void testDestroy() {
        AppInitializationServlet instance = new AppInitializationServlet();
        instance.destroy();
        assertTrue(true); // If we got to here, the servlet was destroyed
    }

    /**
     * Test of deleteApplicationTempDirs method, of class AppInitializationServlet.
     */
    @Test
    public void testDeleteApplicationTempDirs() {
        AppInitializationServlet instance = new AppInitializationServlet();
        boolean expResult = false;
        boolean result = instance.deleteApplicationTempDirs();
        assertEquals(expResult, result);
    }



}