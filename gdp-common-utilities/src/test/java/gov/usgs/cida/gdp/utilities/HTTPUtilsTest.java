package gov.usgs.cida.gdp.utilities;

import java.io.FileInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.Date;
import org.junit.After;
import org.junit.Before;
import java.net.InetAddress;
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
import static org.hamcrest.Matchers.*;

/**
 *
 * @author isuftin
 */
public class HTTPUtilsTest {
    private static org.slf4j.Logger log = LoggerFactory.getLogger(HTTPUtilsTest.class);
    private static boolean isServerReachable = false;
    private static String server = "www.java2s.com";
    private static String tempDir= null;
    private static String seperator = null;
    private static String sampleDir = null;
    private static String testFilePath = null;
    private static final String testFile = "demo_HUCs";
    private static final String secondTestFile = "Yahara_River_HRUs_geo_WGS84";
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        log.debug("Started testing class : " + HTTPUtilsTest.class.getName());
        isServerReachable = InetAddress.getByName(server).isReachable(3000);
        String test = "";
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        log.debug("Ended testing class : " + HTTPUtilsTest.class.getName());
    }

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

        sampleDir = this.tempDir + this.seperator
                + "Sample_Files" + this.seperator
                + "Shapefiles" + this.seperator;

        testFilePath = sampleDir + secondTestFile + ".prj";
    }

    @After
    public void tearDown() {
        File delDir = new File(this.tempDir);
        try {
            FileHelper.deleteDirRecursively(delDir);
        } catch (IOException ex) {
            Logger.getLogger(FileHelperTest.class.getName()).log(Level.SEVERE, "Failed to delete: " + delDir.getPath() + "  -- Remember to clean project or remove this file/dir.", ex);
        }
    }

    /**
     * Test of sendPacket method, of class HTTPUtils.
     */
    @Test
    public void testSendPacket() {
        if (!isServerReachable) {
            log.debug(server + " is not reachable. Skipping testSendPacket()");
            return;
        }
        InputStream result = null;
        try {
            result = HTTPUtils.sendPacket(new URL("http://" + server), "GET");
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
        if (!isServerReachable) {
            log.debug(server + " is not reachable. Skipping testGetHttpConnectionHeaderFields()");
            return;
        }
        Map<String, List<String>> result = null;
        HttpURLConnection conn = null;
        try {
            conn = HTTPUtils.openHttpConnection(new URL("http://" + server), "GET");
        } catch (IOException ex) {
            Logger.getLogger(HTTPUtilsTest.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            result = HTTPUtils.getHttpConnectionHeaderFields(HTTPUtils.openHttpConnection(new URL("http://" + server), "GET"));
        } catch (IOException ex) {
            Logger.getLogger(HTTPUtilsTest.class.getName()).log(Level.SEVERE, null, ex);
        }

        assertFalse(result.isEmpty());
        assertNotNull(result.get("Date"));

        conn.disconnect();
    }

    @Test
    public void testGetStringFromInputStream() throws FileNotFoundException, IOException {
        File openFile = new File(HTTPUtilsTest.testFilePath);
        InputStream is = null;
        is = new FileInputStream(openFile);
        String result = HTTPUtils.getStringFromInputStream(is);
        if (is != null) is.close();
        assertThat(result, is(notNullValue()));
        assertThat(result.length(), is(not(equalTo(0))));
        assertThat(result, containsString("GCS"));

    }

}
