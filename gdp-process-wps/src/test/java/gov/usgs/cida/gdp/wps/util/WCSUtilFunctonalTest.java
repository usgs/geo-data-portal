package gov.usgs.cida.gdp.wps.util;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.commons.io.FileUtils;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.junit.Ignore;
import org.junit.Test;


/**
 *
 * @author tkunicki
 */
//@Ignore
public class WCSUtilFunctonalTest {

    public ReferencedEnvelope testEnvelope = new ReferencedEnvelope(-90.05, -89.95, 44.95, 45.05, DefaultGeographicCRS.WGS84);

    @Test
    @Ignore
    public void testArcServer_EROS_NED() throws URISyntaxException {

        File f = WCSUtil.generateTIFFFile(
                new URI("http://incus.cr.usgs.gov/ArcGIS/services/NED_1/MapServer/WCSServer"),
                "1",
                testEnvelope,
                true);
        f.renameTo(new File("target/testArcServer_EROS_NED.tif"));
    }

    @Test
    @Ignore
    public void testGeoServer_CIDA_NED() throws URISyntaxException, IOException {

        File s = WCSUtil.generateTIFFFile(
                new URI("http://igsarmewmaccave.gs.doi.net:8082/geoserver/wcs"),
                "sample:ned-sample",
                testEnvelope,
                true);
        File d = new File("target/testGeoServer_CIDA_NED.tif");
        FileUtils.copyFile(s, d);
    }

    @Test
    @Ignore
    public void testGeoServer_CIDA_NEDMosaic() throws URISyntaxException, IOException {

        File s = WCSUtil.generateTIFFFile(
                new URI("http://igsarmewmaccave.gs.doi.net:8082/geoserver/wcs"),
                "sample:ned-mosaic",
                testEnvelope,
                true);
        File d = new File("target/testGeoServer_CIDA_NED.tif");
        FileUtils.copyFile(s, d);
    }

    @Test
    @Ignore
    public void testArcServer_CIDA_NLCD2006() throws URISyntaxException {

        File f = WCSUtil.generateTIFFFile(
                new URI("http://cida.usgs.gov/ArcGIS/services/NLCD_2006/MapServer/WCSServer"),
                "1",
                testEnvelope,
                true);
        f.renameTo(new File("target/testArcServer_CIDA_NLCD2006.tif"));
    }

    @Test
    @Ignore
    public void testArcServer_EROS_NLCD2001() throws URISyntaxException {

        File f = WCSUtil.generateTIFFFile(
                new URI("http://incus.cr.usgs.gov/ArcGIS/services/NLCD_2001/MapServer/WCSServer"),
                "2",
                testEnvelope,
                true);
        f.renameTo(new File("target/testArcServer_CIDA_NLCD2001.tif"));
    }
    
    @Test
    public void testArcServer_ScienceBase_TIFF() throws URISyntaxException {

        File f = WCSUtil.generateTIFFFile(
                new URI("http://my-beta.usgs.gov/catalogMaps/mapping/ows/4f4e4799e4b07f02db48f9dd"),
                "ucrb_nlcd1992_all5states_utmzone12.tif",
                new ReferencedEnvelope(-109.51, -109.49, 38.49, 38.51, DefaultGeographicCRS.WGS84),
                true);
        f.renameTo(new File("target/ucrb_nlcd1992_all5states_utmzone12.tif"));
    }
}
