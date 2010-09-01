/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usgs.cida.gdp.utilities;

import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
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
public class HTTPUtilsTest {

    public HTTPUtilsTest() {
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
     * Test of sendPacket method, of class HTTPUtils.
     */
    @Test
    public void testSendPacket() {
        InputStream result = null;
        try {
            result = HTTPUtils.sendPacket(new URL("http://www.google.com"), "GET");
            assertTrue(result.read() > -1);
        } catch (IOException ex) {
            Logger.getLogger(HTTPUtilsTest.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            result.close();
        } catch (IOException ex) {
            Logger.getLogger(HTTPUtilsTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Test
    public void testClassCreation() {
        HTTPUtils result = new HTTPUtils();
        assertNotNull(result);
    }

    /**
     * Test of getHttpConnectionHeaderFields method, of class HTTPUtils.
     */
    @Test
    public void testGetHttpConnectionHeaderFields() {
        Map<String, List<String>> result = null;
        HttpURLConnection conn = null;
        try {
            conn = HTTPUtils.openHttpConnection(new URL("http://www.google.com"), "GET");
        } catch (IOException ex) {
            Logger.getLogger(HTTPUtilsTest.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            result = HTTPUtils.getHttpConnectionHeaderFields(HTTPUtils.openHttpConnection(new URL("http://www.google.com"), "GET"));
        } catch (IOException ex) {
            Logger.getLogger(HTTPUtilsTest.class.getName()).log(Level.SEVERE, null, ex);
        }

        assertFalse(result.isEmpty());
        assertNotNull(result.get("Date"));

        conn.disconnect();
    }
}
