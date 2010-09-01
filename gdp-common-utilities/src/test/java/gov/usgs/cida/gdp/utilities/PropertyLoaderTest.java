/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usgs.cida.gdp.utilities;

import static org.junit.Assert.*;
import java.util.Properties;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author isuftin
 */
public class PropertyLoaderTest {

    public PropertyLoaderTest() {
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
     * Test of loadProperties method, of class PropertyLoader.
     */
    @Test
    public void testLoadProperties_Null_String_() {
        try {
            Properties result = PropertyLoader.loadProperties(null);
        }
        catch (IllegalArgumentException e) {
            assertNotNull(e);
        }
    }

        /**
     * Test of loadProperties method, of class PropertyLoader.
     */
    @Test
    public void testLoadProperties_With_Valid_String() {
        Properties result = null;
        try {
             result = PropertyLoader.loadProperties("application.properties");
        }
        catch (IllegalArgumentException e) {
            fail(e.getMessage());
        }

        assertNotNull(result);
        assertFalse(result.isEmpty());

    }

        /**
     * Test of loadProperties method, of class PropertyLoader.
     */
    @Test
    public void testLoadProperties_String_Forward_Slash() {
        try {
            Properties result = PropertyLoader.loadProperties("/");
        }
        catch (IllegalArgumentException e) {
            assertNotNull(e);
        }
    }



     /**
     * Test of loadProperties method, of class PropertyLoader.
     */
    @Test
    public void testLoadProperties_String_Ends_With_Suffix_LocalLoader_Null() {
        try {
            Properties result = PropertyLoader.loadProperties(".properties", null);
        }
        catch (IllegalArgumentException e) {
            assertNotNull(e);
        }
    }

         /**
     * Test of loadProperties method, of class PropertyLoader.
     */
    @Test
    public void testLoadProperties_String_Ends_With_Invalid_Suffix() {
        try {
            Properties result = PropertyLoader.loadProperties(".invalid", null);
        }
        catch (IllegalArgumentException e) {
            assertNotNull(e);
        }
    }

     /**
     * Test of loadProperties method, of class PropertyLoader.
     */
    @Test
    public void testLoadProperties_String_Forward_Slash_LocalLoader_Null() {
        try {
            Properties result = PropertyLoader.loadProperties("/", null);
        }
        catch (IllegalArgumentException e) {
            assertNotNull(e);
        }
    }
    /**
     * Test of loadProperties method, of class PropertyLoader.
     */

        @Test
        @Ignore
        public void
testLoadProperties_String() {

    }

}
