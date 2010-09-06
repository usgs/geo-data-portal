/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.usgs.cida.gdp.utilities.bean;

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
public class UserDirectoryTest {

    public UserDirectoryTest() {
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