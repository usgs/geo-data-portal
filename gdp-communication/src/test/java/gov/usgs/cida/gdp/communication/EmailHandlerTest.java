/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.usgs.cida.gdp.communication;

import gov.usgs.cida.gdp.communication.bean.EmailMessage;
import gov.usgs.cida.gdp.utilities.PropertyFactory;
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
public class EmailHandlerTest {

    public EmailHandlerTest() {
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
     * Test of sendMessage method, of class EmailHandler.
     */
    @Test
    public void testSendMessageWithBcc() throws Exception {
        List<String> bcc = new ArrayList<String>();
        bcc.add("test@test.ing.gov");
        bcc.add("test@test.ing.gov");
        EmailMessage message = new EmailMessage("test@test.doesnt.exist.gov", "test@testing.purposes.on.ly.gov", bcc, bcc, "Test", "Test");
        EmailHandler instance = new EmailHandler();
        boolean expResult = true;
        boolean result = instance.sendMessage(message);
        assertEquals(expResult, result);
    }

    /**
     * Test of sendMessage method, of class EmailHandler.
     */
    @Test
    public void testSendMessageWithCc() throws Exception {
        List<String> bcc = new ArrayList<String>();
        bcc.add("test@test.ing.gov");
        bcc.add("test@test.ing.gov");
        EmailMessage message = new EmailMessage("test@test.doesnt.exist.gov", "test@testing.purposes.on.ly.gov", bcc, "Test", "Test");
        EmailHandler instance = new EmailHandler();
        boolean expResult = true;
        boolean result = instance.sendMessage(message);
        assertEquals(expResult, result);
    }
}