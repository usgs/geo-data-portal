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

    @Test
    public void testTEMP_LOCATION() {
        String test = AppConstant.TEMP_LOCATION.toString();
        assertNotNull(test);
        assertTrue(test.contains("/GDP-TEMP"));
    }

    @Test
    public void testNEW_SHAPEFILE_LOCATION() {
        String test = AppConstant.NEW_SHAPEFILE_LOCATION.toString();
        assertNotNull(test);
        assertTrue(test.contains("/GDP-TEMP"));
    }

}