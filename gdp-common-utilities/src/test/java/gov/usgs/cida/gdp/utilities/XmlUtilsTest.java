package gov.usgs.cida.gdp.utilities;

import gov.usgs.cida.gdp.utilities.bean.XmlReply;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.easymock.EasyMock.*;
import org.junit.Ignore;
import javax.servlet.http.HttpServletResponse;
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
public class XmlUtilsTest {

    HttpServletResponse response = null;
    PrintWriter writer = null;

    public XmlUtilsTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        response = createNiceMock(HttpServletResponse.class);
        writer = createNiceMock(PrintWriter.class);
        try {
            expect(response.getWriter()).andReturn(writer);
        } catch (IOException ex) {
            Logger.getLogger(XmlUtilsTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        replay(response, writer);
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of sendXml method, of class XmlUtils.
     */
    @Test
    public void testSendXml() {
        try {
            XmlUtils.sendXml("test", Long.MIN_VALUE, response);
        } catch (IOException ex) {
            fail(ex.getMessage());
        }
       assertTrue(true);
    }

        /**
     * Test of sendXml method, of class XmlUtils.
     */
    @Test
    public void testSendXmlWithInvalidCode() {
        try {
            XmlUtils.sendXml("test" + '\u00Bf', Long.MIN_VALUE, response);
        } catch (IOException ex) {
            fail(ex.getMessage());
        }
        assertTrue(true);
    }

    /**
     * Test of sendXml method, of class XmlUtils.
     */
    @Test
    public void testSendXmlUsingXmlBean() throws Exception {
        XmlReply xmlReply = new XmlReply();
        try {
            XmlUtils.sendXml(xmlReply, Long.MIN_VALUE, response);
        } catch (IOException ex) {
            fail(ex.getMessage());
        }
       assertTrue(true);
    }

    @Test
    public void testCreateXmlUtilsObject() throws Exception {
        XmlUtils result = new XmlUtils();
        assertNotNull(result);
    }
}
