package gov.usgs.cida.gdp.webapp.servlet;

import javax.servlet.ServletConfig;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author isuftin
 */
public class AppInitializationServletTest {

    private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AppInitializationServletTest.class);

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        log.debug("Started testing class.");
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        log.debug("Ended testing class.");
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
