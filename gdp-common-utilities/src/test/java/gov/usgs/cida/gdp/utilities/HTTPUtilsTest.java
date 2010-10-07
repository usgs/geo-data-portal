package gov.usgs.cida.gdp.utilities;

import org.slf4j.LoggerFactory;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author isuftin
 */
public class HTTPUtilsTest {
    private static org.slf4j.Logger log = LoggerFactory.getLogger(HTTPUtilsTest.class);

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        log.debug("Started testing class.");
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        log.debug("Ended testing class.");
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
