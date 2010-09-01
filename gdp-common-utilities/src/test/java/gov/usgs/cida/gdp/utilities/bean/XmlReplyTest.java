package gov.usgs.cida.gdp.utilities.bean;

import gov.usgs.cida.gdp.utilities.bean.Acknowledgement;
import gov.usgs.cida.gdp.utilities.bean.Error;
import gov.usgs.cida.gdp.utilities.bean.ErrorEnum;
import gov.usgs.cida.gdp.utilities.bean.Message;
import gov.usgs.cida.gdp.utilities.bean.XmlResponse;
import gov.usgs.cida.gdp.utilities.bean.XmlReply;
import java.util.ArrayList;
import java.util.List;
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
public class XmlReplyTest {

    XmlReply instance = null;

    public XmlReplyTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        this.instance = new XmlReply();
    }

    @After
    public void tearDown() {
        this.instance = null;
    }

    @Test
    public void testXmlReplyBeanConstructorAckBeanAndContentArray() {
        XmlReply result = new XmlReply(new Acknowledgement(Acknowledgement.ACK_OK), new Acknowledgement(Acknowledgement.ACK_OK));
        assertNotNull(result);
    }

    @Test
    public void testXmlReplyBeanConstructorWithInStatusAndContentArray() {
        XmlReply result = new XmlReply(1, new Acknowledgement(Acknowledgement.ACK_OK));
        assertNotNull(result);
    }

    @Test
    public void testXmlReplyBeanConstructorWithInStatusAndContentArrayWithIllegalArgumentException() {
        try {
            XmlReply result = new XmlReply(-1, new Acknowledgement(Acknowledgement.ACK_OK));
            assertNotNull(result);
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testXmlReplyBeanConstructorWithInStatusAndMessageBean() {
        XmlReply result = new XmlReply(1, new Message());
        assertNotNull(result);
    }

    @Test
    public void testXmlReplyBeanConstructorWithInStatusAndMessageBeanWithIllegalArgumentException() {

        try {
            XmlReply result = new XmlReply(-1, new Message());
            assertNotNull(result);
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testXmlReplyBeanConstructorWithInStatusAndErrorEnumAndMessageBeanWithIllegalArgumentException() {
        try {
            @SuppressWarnings("unused")
	    XmlReply result = new XmlReply(-1, ErrorEnum.ERR_ATTRIBUTES_NOT_FOUND, new Message());
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testXmlReplyBeanConstructorWithIntAndXmlBeanList() {
        List content = new ArrayList();
        content.add(new Acknowledgement(Acknowledgement.ACK_OK));
        XmlReply result = new XmlReply(1, content);
        assertNotNull(result);
        assertTrue(result.getContent().length > 0);
    }

    @Test
    public void testXmlReplyBeanConstructorWithMessageBeanAndContentArray() {
        XmlReply result = new XmlReply(new Message("Test"), new XmlResponse[]{null, null});
        assertNotNull(result);
    }

    @Test
    public void testXmlReplyBeanConstructorWithStatusInt() {
        XmlReply result = new XmlReply(1);
        assertNotNull(result);
    }

    @Test
    public void testXmlReplyBeanConstructorWithStatusIntWithIllegalArgumentException() {
        try {
            @SuppressWarnings("unused")
	    XmlReply result = new XmlReply(-1);
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testXmlReplyBeanConstructorWithStatusIntAndErrorBean() {
        XmlReply result = new XmlReply(1, new Error());
        assertNotNull(result);
    }

    @Test
    public void testXmlReplyBeanConstructorWithStatusIntAndErrorBeanWithIllegalArgumentException() {
        try {
            @SuppressWarnings("unused")
	    XmlReply result = new XmlReply(-1, new Error());
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testXmlReplyBeanConstructorWithStatusIntAndErrorEnum() {
        XmlReply result = new XmlReply(1, ErrorEnum.ERR_ATTRIBUTES_NOT_FOUND);
        assertNotNull(result);
    }

    @Test
    public void testXmlReplyBeanConstructorWithStatusIntAndErrorEnumWithIllegalArgumentException() {
        try {
            @SuppressWarnings("unused")
	    XmlReply result = new XmlReply(-1, ErrorEnum.ERR_ATTRIBUTES_NOT_FOUND);
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testXmlReplyBeanConstructorWithStatusIntAndErrorEnumAndMessageBean() {
        XmlReply result = new XmlReply(1, ErrorEnum.ERR_ATTRIBUTES_NOT_FOUND, new Message());
        assertNotNull(result);
    }

    @Test
    public void testXmlReplyBeanConstructorWithStatusIntAndErrorEnumAndMessageBeanWithIllegalArgumentException() {
        try {
            @SuppressWarnings("unused")
	    XmlReply result = new XmlReply(-1, ErrorEnum.ERR_ATTRIBUTES_NOT_FOUND, new Message());
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testXmlReplyBeanConstructorWithStatusIntAndErrorBeanAndMessageBean() {
        XmlReply result = new XmlReply(1, new Error(), new Message());
        assertNotNull(result);
    }

    @Test
    public void testXmlReplyBeanConstructorWithStatusIntAndErrorBeanAndMessageBeanWithIllegalArgumentException() {
        try {
            @SuppressWarnings("unused")
	    XmlReply result = new XmlReply(-1, new Error(), new Message());
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }
    }

    /**
     * Test of toXml method, of class XmlReply.
     */
    @Test
    public void testToXml() {
        XmlReply instance = new XmlReply();
        instance.setMessage(new Message("TEST"));
        String result = instance.toXML();
        assertTrue(result.contains("TEST"));
    }

    /**
     * Test of setAcknowledgment method, of class XmlReply.
     */
    @Test
    public void testSetAcknowledgment() {
        Acknowledgement acknowledgment = new Acknowledgement(1);
        XmlReply instance = new XmlReply();
        instance.setAcknowledgment(acknowledgment);
        assertEquals("OK", instance.getAcknowledgment().getStatus());
    }

    /**
     * Test of getAcknowledgment method, of class XmlReply.
     */
    @Test
    public void testGetAcknowledgment() {
        Acknowledgement acknowledgment = new Acknowledgement(1);
        XmlReply instance = new XmlReply();
        instance.setAcknowledgment(acknowledgment);
        Acknowledgement result = instance.getAcknowledgment();
        assertEquals("OK", result.getStatus());

    }

    /**
     * Test of setContent method, of class XmlReply.
     */
    @Test
    public void testSetContent() {
        XmlResponse[] content = {null, null};
        XmlReply instance = new XmlReply();
        instance.setContent(content);
        assertEquals(2, instance.getContent().length);
    }

    /**
     * Test of getContent method, of class XmlReply.
     */
    @Test
    public void testGetContent() {
        XmlReply instance = new XmlReply();
        instance.setContent(new XmlResponse[] {null, null});
        Object[] result = instance.getContent();
        assertEquals(2, result.length);
    }

    /**
     * Test of setMessage method, of class XmlReply.
     */
    @Test
    public void testSetMessage() {
        Message message = new Message("TEST");
        XmlReply instance = new XmlReply();
        instance.setMessage(message);
        assertEquals("TEST", instance.getMessage().getMessages().get(0));
    }

    /**
     * Test of getMessage method, of class XmlReply.
     */
    @Test
    public void testGetMessage() {
        Message message = new Message("TEST");
        XmlReply instance = new XmlReply();
        instance.setMessage(message);
        assertEquals("TEST", instance.getMessage().getMessages().get(0));
    }

    /**
     * Test of getErrorEnum method, of class XmlReply.
     */
    @Test
    public void testGetErrorEnum() {
        XmlReply instance = new XmlReply();
        instance.setErrorEnum(ErrorEnum.ERR_ATTRIBUTES_NOT_FOUND);
        ErrorEnum expResult = ErrorEnum.ERR_ATTRIBUTES_NOT_FOUND;
        ErrorEnum result = instance.getErrorEnum();
        assertEquals(expResult, result);
    }

    /**
     * Test of setErrorEnum method, of class XmlReply.
     */
    @Test
    public void testSetErrorEnum() {
        XmlReply instance = new XmlReply();
        instance.setErrorEnum(ErrorEnum.ERR_ATTRIBUTES_NOT_FOUND);
        ErrorEnum expResult = ErrorEnum.ERR_ATTRIBUTES_NOT_FOUND;
        ErrorEnum result = instance.getErrorEnum();
        assertEquals(expResult, result);
    }
}
