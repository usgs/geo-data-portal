/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usgs.cida.gdp.utilities.bean;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author isuftin
 */
public class UserDirectoryTest {

    private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(UserDirectoryTest.class);

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        log.debug("Started testing class.");
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        log.debug("Ended testing class.");
    }

    /**
     * Test of getDirectory method, of class UserDirectory.
     */
    @Test
    public void testSetGetDirectory() {
        String test = "test";
        UserDirectory target = new UserDirectory();
        target.setDirectory(test);
        String result = target.getDirectory();
        assertEquals(test, result);
    }

    /**
     * Test of setDirectory method, of class UserDirectory.
     */
    @Test
    public void testCreateUserDirectory() {
        UserDirectory test = new UserDirectory();
        assertEquals(test.getClass(), UserDirectory.class);
    }
}
