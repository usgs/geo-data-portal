/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.usgs.cida.gdp.constants;

import java.io.File;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author isuftin
 */
public class AppConstantTest {

    @Test
    public void TEMP_LOCATIONtest() {
        String test = AppConstant.TEMP_LOCATION.toString();
        assertNotNull(test);
        assertEquals(System.getProperty("java.io.tmpdir") + File.separator + "GDP-TEMP", test);
    }

    @Test
    public void USERSPACE_LOCATIONtest() {
        String test = AppConstant.USERSPACE_LOCATION.toString();
        assertNotNull(test);
        assertEquals(System.getProperty("java.io.tmpdir") + File.separator + "GDP-TEMP" + File.separator + "UserSpace", test);
    }

    @Test
    public void NEW_USER_LOCATIONtest() {
        String test = AppConstant.NEW_USER_LOCATION.toString();
        assertNotNull(test);
        assertTrue(test.contains(System.getProperty("java.io.tmpdir") + File.separator + "GDP-TEMP" + File.separator + "UserSpace"));
    }

    @Test
    public void SHAPEFILE_LOCATIONtest() {
        String test = AppConstant.SHAPEFILE_LOCATION.toString();
        assertNotNull(test);
        assertEquals(System.getProperty("java.io.tmpdir") + File.separator + "GDP-TEMP" + File.separator + "ShapeFiles", test);
    }

    @Test
    public void WORK_LOCATIONtest() {
        String test = AppConstant.WORK_LOCATION.toString();
        assertNotNull(test);
        assertEquals(System.getProperty("java.io.tmpdir") + File.separator + "GDP-TEMP" + File.separator + "Work", test);
    }

    @Test
    public void NEW_SHAPEFILE_LOCATIONtest() {
        String test = AppConstant.NEW_SHAPEFILE_LOCATION.toString();
        assertNotNull(test);
        assertTrue(test.contains(System.getProperty("java.io.tmpdir") + File.separator + "GDP-TEMP" + File.separator + "ShapeFiles" + File.separator ));
    }

    @Test
    public void WFS_ENDPOINTtest() {
        String test = AppConstant.WFS_ENDPOINT.toString();
        assertNotNull(test);
        assertEquals("http://localhost:8080/geoserver", test);
    }

    @Test
    public void WFS_USERtest() {
        String test = AppConstant.WFS_USER.toString();
        assertNotNull(test);
        assertEquals("admin", test);
    }

    @Test
    public void WFS_PASStest() {
        String test = AppConstant.WFS_PASS.toString();
        assertNotNull(test);
        assertEquals("geoserver", test);
    }

    @Test
    public void WPS_ENDPOINTtest() {
        String test = AppConstant.WPS_ENDPOINT.toString();
        assertNotNull(test);
        assertEquals("http://localhost:8080/wps/WebProcessingService", test);
    }

    @Test
    public void FILE_WIPE_MILLIStest() {
        String test = AppConstant.FILE_WIPE_MILLIS.toString();
        assertNotNull(test);
        assertEquals("48", test);
    }

    @Test
    public void CHECK_COMPLETE_MILLIStest() {
        String test = AppConstant.CHECK_COMPLETE_MILLIS.toString();
        assertNotNull(test);
        assertEquals(new Long(60000l).toString(), test);
    }

    @Test
    public void FROM_EMAILtest() {
        String test = AppConstant.FROM_EMAIL.toString();
        assertNotNull(test);
        assertEquals("gdp_data@usgs.gov", test);
    }


    @Test
    public void EMAIL_HOSTtest() {
        String test = AppConstant.EMAIL_HOST.toString();
        assertNotNull(test);
        assertEquals("gsvaresh01.er.usgs.gov", test);
    }

    @Test
    public void EMAIL_PORTtest() {
        String test = AppConstant.EMAIL_PORT.toString();
        assertNotNull(test);
        assertEquals("25", test);
    }

    @Test
    public void TRACK_EMAILtest() {
        String test = AppConstant.TRACK_EMAIL.toString();
        assertNotNull(test);
        assertEquals("", test);
    }

    @Test
    public void SHAPEFILE_MAX_SIZEtest() {
        String test = AppConstant.SHAPEFILE_MAX_SIZE.toString();
        assertNotNull(test);
        assertEquals(Integer.toString(Integer.MAX_VALUE), test);
    }
}