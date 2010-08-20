/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usgs.cida.gdp.utilities;

import gov.usgs.cida.gdp.utilities.bean.AckBean;
import gov.usgs.cida.gdp.utilities.bean.ErrorBean;
import gov.usgs.cida.gdp.utilities.bean.ErrorEnum;
import gov.usgs.cida.gdp.utilities.bean.MessageBean;
import gov.usgs.cida.gdp.utilities.bean.XmlBean;
import gov.usgs.cida.gdp.utilities.bean.XmlReplyBean;
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
public class XmlReplyBeanTest {

    XmlReplyBean instance = null;

    public XmlReplyBeanTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        this.instance = new XmlReplyBean();
    }

    @After
    public void tearDown() {
        this.instance = null;
    }

    @Test
    public void testXmlReplyBeanConstructorAckBeanAndContentArray() {
        XmlReplyBean result = new XmlReplyBean(new AckBean(AckBean.ACK_OK), new AckBean(AckBean.ACK_OK));
        assertNotNull(result);
    }

    @Test
    public void testXmlReplyBeanConstructorWithInStatusAndContentArray() {
        XmlReplyBean result = new XmlReplyBean(1, new AckBean(AckBean.ACK_OK));
        assertNotNull(result);
    }

    @Test
    public void testXmlReplyBeanConstructorWithInStatusAndContentArrayWithIllegalArgumentException() {
        try {
            XmlReplyBean result = new XmlReplyBean(-1, new AckBean(AckBean.ACK_OK));
            assertNotNull(result);
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testXmlReplyBeanConstructorWithInStatusAndMessageBean() {
        XmlReplyBean result = new XmlReplyBean(1, new MessageBean());
        assertNotNull(result);
    }

    @Test
    public void testXmlReplyBeanConstructorWithInStatusAndMessageBeanWithIllegalArgumentException() {

        try {
            XmlReplyBean result = new XmlReplyBean(-1, new MessageBean());
            assertNotNull(result);
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testXmlReplyBeanConstructorWithInStatusAndErrorEnumAndMessageBeanWithIllegalArgumentException() {
        try {
            @SuppressWarnings("unused")
	    XmlReplyBean result = new XmlReplyBean(-1, ErrorEnum.ERR_ATTRIBUTES_NOT_FOUND, new MessageBean());
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testXmlReplyBeanConstructorWithIntAndXmlBeanList() {
        List content = new ArrayList();
        content.add(new AckBean(AckBean.ACK_OK));
        XmlReplyBean result = new XmlReplyBean(1, content);
        assertNotNull(result);
        assertTrue(result.getContent().length > 0);
    }

    @Test
    public void testXmlReplyBeanConstructorWithMessageBeanAndContentArray() {
        XmlReplyBean result = new XmlReplyBean(new MessageBean("Test"), new XmlBean[]{null, null});
        assertNotNull(result);
    }

    @Test
    public void testXmlReplyBeanConstructorWithStatusInt() {
        XmlReplyBean result = new XmlReplyBean(1);
        assertNotNull(result);
    }

    @Test
    public void testXmlReplyBeanConstructorWithStatusIntWithIllegalArgumentException() {
        try {
            @SuppressWarnings("unused")
	    XmlReplyBean result = new XmlReplyBean(-1);
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testXmlReplyBeanConstructorWithStatusIntAndErrorBean() {
        XmlReplyBean result = new XmlReplyBean(1, new ErrorBean());
        assertNotNull(result);
    }

    @Test
    public void testXmlReplyBeanConstructorWithStatusIntAndErrorBeanWithIllegalArgumentException() {
        try {
            @SuppressWarnings("unused")
	    XmlReplyBean result = new XmlReplyBean(-1, new ErrorBean());
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testXmlReplyBeanConstructorWithStatusIntAndErrorEnum() {
        XmlReplyBean result = new XmlReplyBean(1, ErrorEnum.ERR_ATTRIBUTES_NOT_FOUND);
        assertNotNull(result);
    }

    @Test
    public void testXmlReplyBeanConstructorWithStatusIntAndErrorEnumWithIllegalArgumentException() {
        try {
            @SuppressWarnings("unused")
	    XmlReplyBean result = new XmlReplyBean(-1, ErrorEnum.ERR_ATTRIBUTES_NOT_FOUND);
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testXmlReplyBeanConstructorWithStatusIntAndErrorEnumAndMessageBean() {
        XmlReplyBean result = new XmlReplyBean(1, ErrorEnum.ERR_ATTRIBUTES_NOT_FOUND, new MessageBean());
        assertNotNull(result);
    }

    @Test
    public void testXmlReplyBeanConstructorWithStatusIntAndErrorEnumAndMessageBeanWithIllegalArgumentException() {
        try {
            @SuppressWarnings("unused")
	    XmlReplyBean result = new XmlReplyBean(-1, ErrorEnum.ERR_ATTRIBUTES_NOT_FOUND, new MessageBean());
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testXmlReplyBeanConstructorWithStatusIntAndErrorBeanAndMessageBean() {
        XmlReplyBean result = new XmlReplyBean(1, new ErrorBean(), new MessageBean());
        assertNotNull(result);
    }

    @Test
    public void testXmlReplyBeanConstructorWithStatusIntAndErrorBeanAndMessageBeanWithIllegalArgumentException() {
        try {
            @SuppressWarnings("unused")
	    XmlReplyBean result = new XmlReplyBean(-1, new ErrorBean(), new MessageBean());
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }
    }

    /**
     * Test of toXml method, of class XmlReplyBean.
     */
    @Test
    public void testToXml() {
        XmlReplyBean instance = new XmlReplyBean();
        instance.setMessage(new MessageBean("TEST"));
        String result = instance.toXml();
        assertTrue(result.contains("TEST"));
    }

    /**
     * Test of setAcknowledgment method, of class XmlReplyBean.
     */
    @Test
    public void testSetAcknowledgment() {
        AckBean acknowledgment = new AckBean(1);
        XmlReplyBean instance = new XmlReplyBean();
        instance.setAcknowledgment(acknowledgment);
        assertEquals("OK", instance.getAcknowledgment().getStatus());
    }

    /**
     * Test of getAcknowledgment method, of class XmlReplyBean.
     */
    @Test
    public void testGetAcknowledgment() {
        AckBean acknowledgment = new AckBean(1);
        XmlReplyBean instance = new XmlReplyBean();
        instance.setAcknowledgment(acknowledgment);
        AckBean result = instance.getAcknowledgment();
        assertEquals("OK", result.getStatus());

    }

    /**
     * Test of setContent method, of class XmlReplyBean.
     */
    @Test
    public void testSetContent() {
        XmlBean[] content = {null, null};
        XmlReplyBean instance = new XmlReplyBean();
        instance.setContent(content);
        assertEquals(2, instance.getContent().length);
    }

    /**
     * Test of getContent method, of class XmlReplyBean.
     */
    @Test
    public void testGetContent() {
        XmlReplyBean instance = new XmlReplyBean();
        instance.setContent(new XmlBean[] {null, null});        
        Object[] result = instance.getContent();
        assertEquals(2, result.length);
    }

    /**
     * Test of setMessage method, of class XmlReplyBean.
     */
    @Test
    public void testSetMessage() {
        MessageBean message = new MessageBean("TEST");
        XmlReplyBean instance = new XmlReplyBean();
        instance.setMessage(message);
        assertEquals("TEST", instance.getMessage().getMessages().get(0));
    }

    /**
     * Test of getMessage method, of class XmlReplyBean.
     */
    @Test
    public void testGetMessage() {
        MessageBean message = new MessageBean("TEST");
        XmlReplyBean instance = new XmlReplyBean();
        instance.setMessage(message);
        assertEquals("TEST", instance.getMessage().getMessages().get(0));
    }

    /**
     * Test of getErrorEnum method, of class XmlReplyBean.
     */
    @Test
    public void testGetErrorEnum() {
        XmlReplyBean instance = new XmlReplyBean();
        instance.setErrorEnum(ErrorEnum.ERR_ATTRIBUTES_NOT_FOUND);
        ErrorEnum expResult = ErrorEnum.ERR_ATTRIBUTES_NOT_FOUND;
        ErrorEnum result = instance.getErrorEnum();
        assertEquals(expResult, result);
    }

    /**
     * Test of setErrorEnum method, of class XmlReplyBean.
     */
    @Test
    public void testSetErrorEnum() {
        XmlReplyBean instance = new XmlReplyBean();
        instance.setErrorEnum(ErrorEnum.ERR_ATTRIBUTES_NOT_FOUND);
        ErrorEnum expResult = ErrorEnum.ERR_ATTRIBUTES_NOT_FOUND;
        ErrorEnum result = instance.getErrorEnum();
        assertEquals(expResult, result);
    }
}
