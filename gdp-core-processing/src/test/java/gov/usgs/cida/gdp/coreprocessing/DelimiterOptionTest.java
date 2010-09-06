/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.usgs.cida.gdp.coreprocessing;

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
public class DelimiterOptionTest {

    public DelimiterOptionTest() {
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
     * Test of values method, of class DelimiterOption.
     */
    @Test
    public void testValues() {
        assertEquals(DelimiterOption.c.toString(), "[comma]");
        assertEquals(DelimiterOption.s.toString(), "[space]");
        assertEquals(DelimiterOption.t.toString(), "[tab]");
        assertEquals(DelimiterOption.getDefault(), DelimiterOption.c);
    }

}