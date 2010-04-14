/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.usgs.gdp.bean;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
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
public class EmailMessageBeanTest {

    public EmailMessageBeanTest() {
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

    @Test
    public void testEmailMessageBeanDefault() {
        EmailMessageBean instance = new EmailMessageBean();
        assertEquals("", instance.getFrom());
        assertEquals("", instance.getTo());
        assertTrue(instance.getCc().isEmpty());
        assertEquals("", instance.getSubject());
        assertEquals("", instance.getContent());
    }

    /**
     * Test of getFrom method, of class EmailMessageBean.
     */
    @Test
    public void testGetFrom() {
        EmailMessageBean instance = new EmailMessageBean();
        String expResult = "";
        String result = instance.getFrom();
        assertEquals(expResult, result);
    }

    /**
     * Test of setFrom method, of class EmailMessageBean.
     */
    @Test
    public void testSetFrom() {
        String from = "";
        EmailMessageBean instance = new EmailMessageBean();
        instance.setFrom(from);
    }

    /**
     * Test of getTo method, of class EmailMessageBean.
     */
    @Test
    public void testGetTo() {
        EmailMessageBean instance = new EmailMessageBean();
        String expResult = "";
        String result = instance.getTo();
        assertEquals(expResult, result);
    }

    /**
     * Test of setTo method, of class EmailMessageBean.
     */
    @Test
    public void testSetTo() {
        String to = "";
        EmailMessageBean instance = new EmailMessageBean();
        instance.setTo(to);
    }

    /**
     * Test of getCc method, of class EmailMessageBean.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testGetCc() {
        EmailMessageBean instance = new EmailMessageBean();
        List expResult = new Vector<String>();
        List result = instance.getCc();
        assertEquals(expResult, result);
    }

    /**
     * Test of setCc method, of class EmailMessageBean.
     */
    @Test
    public void testSetCc() {
        List<String> cc = null;
        EmailMessageBean instance = new EmailMessageBean();
        instance.setCc(cc);
    }

    /**
     * Test of getSubject method, of class EmailMessageBean.
     */
    @Test
    public void testGetSubject() {
        EmailMessageBean instance = new EmailMessageBean();
        String expResult = "";
        String result = instance.getSubject();
        assertEquals(expResult, result);
    }

    /**
     * Test of setSubject method, of class EmailMessageBean.
     */
    @Test
    public void testSetSubject() {
        String subject = "";
        EmailMessageBean instance = new EmailMessageBean();
        instance.setSubject(subject);
    }

    /**
     * Test of getContent method, of class EmailMessageBean.
     */
    @Test
    public void testGetContent() {
        EmailMessageBean instance = new EmailMessageBean();
        String expResult = "";
        String result = instance.getContent();
        assertEquals(expResult, result);
    }

    /**
     * Test of setContent method, of class EmailMessageBean.
     */
    @Test
    public void testSetContent() {
        String content = "";
        EmailMessageBean instance = new EmailMessageBean();
        instance.setContent(content);
    }

    /**
     * Test of getBcc method, of class EmailMessageBean.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testGetBcc() {
        EmailMessageBean instance = new EmailMessageBean();
        List expResult = new ArrayList<String>();
        List result = instance.getBcc();
        assertEquals(expResult, result);
    }

    /**
     * Test of getBccToString method, of class EmailMessageBean.
     */
    @Test
    public void testGetBccToString() {
        EmailMessageBean instance = new EmailMessageBean();
        String expResult = "";
        String result = instance.getBccToString();
        assertEquals(expResult, result);
    }

    /**
     * Test of setBcc method, of class EmailMessageBean.
     */
    @Test
    public void testSetBcc() {
        List<String> bcc = null;
        EmailMessageBean instance = new EmailMessageBean();
        instance.setBcc(bcc);
    }

}