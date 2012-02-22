package gov.usgs.cida.gdp.wps.util;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author tkunicki
 */
public class WCSUtilTest {

    @Test
    public void testIsOGC_URN() {
        // stuff we would expect to see that passes
        assertTrue(WCSUtil.isOGC("urn:ogc:def:crs:EPSG::4326"));
        assertTrue(WCSUtil.isOGC("urn:ogc-x:def:crs:EPSG::4326"));
        assertTrue(WCSUtil.isOGC("urn:x-ogc:def:crs:EPSG::4326"));
        assertTrue(WCSUtil.isOGC("urn:ogc:def:crs:EPSG::4326"));
        assertTrue(WCSUtil.isOGC("urn:ogc-x:def:crs:EPSG:6.9:4326"));
        assertTrue(WCSUtil.isOGC("urn:x-ogc:def:crs:EPSG:6.9:4326"));
        assertTrue(WCSUtil.isOGC("urn:ogc:def:crs:EPSG:6.9:4326"));

        // stuff we would expect to see that fails
        assertFalse(WCSUtil.isOGC("EPSG:4326"));

        // random permutations we wouldn't expect to see that should fail
        assertFalse(WCSUtil.isOGC("urn:ogc:def:crs:EPSG::4326:"));
        assertFalse(WCSUtil.isOGC("urn:y-ogc:def:crs:EPSG:6.9:4326"));
        assertFalse(WCSUtil.isOGC("urn:ogc-y:def:crs:EPSG:6.9:4326"));
        assertFalse(WCSUtil.isOGC("urn:cida:def:crs:EPSG::4326"));
        assertFalse(WCSUtil.isOGC("urn:cida-x:def:crs:EPSG::4326"));
        assertFalse(WCSUtil.isOGC("urn:x-cida:def:crs:EPSG::4326"));
    }

    @Test
    public void testConvertToNonOGC_URN() {
        assertEquals("EPSG:4326", WCSUtil.convertCRSToNonOGC("urn:ogc:def:crs:EPSG::4326"));
        assertEquals("EPSG:4326", WCSUtil.convertCRSToNonOGC("urn:ogc-x:def:crs:EPSG::4326"));
        assertEquals("EPSG:4326", WCSUtil.convertCRSToNonOGC("urn:x-ogc:def:crs:EPSG::4326"));
        assertEquals("EPSG:4326", WCSUtil.convertCRSToNonOGC("urn:ogc:def:crs:EPSG::4326"));
        assertEquals("EPSG:4326", WCSUtil.convertCRSToNonOGC("urn:ogc-x:def:crs:EPSG:6.9:4326"));
        assertEquals("EPSG:4326", WCSUtil.convertCRSToNonOGC("urn:x-ogc:def:crs:EPSG:6.9:4326"));
        assertEquals("EPSG:4326", WCSUtil.convertCRSToNonOGC("urn:ogc:def:crs:EPSG:6.9:4326"));
    }

}
