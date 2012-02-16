package gov.usgs.cida.gdp.wps.util;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author tkunicki
 */
public class WCSUtilTest {


    @Test
    @Ignore
    public void testEROS_NED() throws URISyntaxException {

        File f = WCSUtil.generateTIFFFile(
                new URI("http://incus.cr.usgs.gov/ArcGIS/services/NED_1/MapServer/WCSServer"),
                "1",
                new ReferencedEnvelope(-91, -89, 46, 44, DefaultGeographicCRS.WGS84),
                true);
        f.renameTo(new File("target/test.tif"));
    }

    @Test
    @Ignore
    public void testCIDA_NLCD() throws URISyntaxException {

        File f = WCSUtil.generateTIFFFile(
                new URI("http://cida.usgs.gov/ArcGIS/services/NLCD_2006/MapServer/WCSServer"),
                "1",
                new ReferencedEnvelope(-91, -89, 46, 44, DefaultGeographicCRS.WGS84),
                true);
        f.renameTo(new File("target/test.tif"));
    }
}
