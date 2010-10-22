/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.usgs.cida.gdp.constants;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author isuftin
 */
public class AppConstantTest {

    /**
     * Test of toString method, of class AppConstant.
     */
    @Test
    public void testToString() {
        String test = AppConstant.TEMP_LOCATION.toString();
        assertNotNull(test);
        assertTrue(test.contains("/GDP-TEMP"));
    }

}