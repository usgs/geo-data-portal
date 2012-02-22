package gov.usgs.cida.gdp.wps.util;

import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import static org.junit.Assert.*;
import org.xml.sax.SAXException;

/**
 *
 * @author tkunicki
 */
public class WCSDescribeCoverageInspector_1_1_XTest {

    public WCSDescribeCoverageInspector_1_1_XTest() {
    }

    @Test
    public void testArcGISServer_9_Response_1_1_0() throws SAXException, IOException {
        validateArcGISServer_9_ResponseParsing("/wcs/ArcGIS-9.31-DescribeCoverage-1.1.0.xml");
    }

    @Test
    public void testArcGISServer_9_Response_1_1_1() throws SAXException, IOException {
        validateArcGISServer_9_ResponseParsing("/wcs/ArcGIS-9.31-DescribeCoverage-1.1.1.xml");
    }

    @Test
    public void testArcGISServer_10_Response_1_1_0() throws SAXException, IOException {
        validateArcGISServer_10_ResponseParsing("/wcs/ArcGIS-10.03-DescribeCoverage-1.1.0.xml");
    }

    @Test
    public void testArcGISServer_10_Response_1_1_1() throws SAXException, IOException {
        validateArcGISServer_10_ResponseParsing("/wcs/ArcGIS-10.03-DescribeCoverage-1.1.1.xml");
    }

    @Test
    public void testGeoServer_2_1_Response_1_1_1() throws SAXException, IOException {
        validateGeoServerResponseParsing("/wcs/GeoServer-2.1.3-DescribeCoverage-1.1.1.xml");
    }

    @Test
    public void testGeoServer_2_1_Response_1_1_0() throws SAXException, IOException {
        validateGeoServerResponseParsing("/wcs/GeoServer-2.1.3-DescribeCoverage-1.1.0.xml");
    }

    private void validateArcGISServer_9_ResponseParsing(String resourceName) throws SAXException, IOException {

        InputStream inputStream = null;
        try {
            inputStream = getClass().getResourceAsStream(resourceName);
            assertNotNull(inputStream);

            WCSDescribeCoverageInspector_1_1_X inspector = new WCSDescribeCoverageInspector_1_1_X(
                    DocumentUtil.createDocument(inputStream),
                    "1");

            assertEquals("urn:ogc:def:crs:EPSG::4269", inspector.getGridBaseCRSAsString());

            assertEquals("", inspector.getGridTypeAsString());

            double[] gridOffsets = inspector.getGridOffsets();
            assertNotNull(gridOffsets);
            assertEquals(0.00027777777799542502, gridOffsets[0], 1e-12);
            assertEquals(-0.00027777777779647273, gridOffsets[1], 1e-12);

            double[] gridOrigin = inspector.getGridOrigin();
            assertNotNull(gridOrigin);
            assertEquals(-125.099861111189, gridOrigin[0], 1e-12);
            assertEquals(50.001527777925197, gridOrigin[1], 1e-12);

            double[] gridLowerCorner = inspector.getGridLowerCorner();
            assertNotNull(gridLowerCorner);
            assertEquals(-125.100000000078, gridLowerCorner[0], 1e-9);
            assertEquals(14.499999997758103, gridLowerCorner[1], 1e-10);


            double[] gridUpperCorner = inspector.getGridUpperCorner();
            assertNotNull(gridUpperCorner);
            assertEquals(-65.998333287103392, gridUpperCorner[0], 1e-10);
            assertEquals(50.001666666814096, gridUpperCorner[1], 1e-10);

            assertEquals("Float32", inspector.getGridDataTypeAsString());

            String[] supportedFormats = inspector.getGridSupportedFormats();
            assertArrayEquals(
                    new String[]{
                        "image/GeoTIFF",
                        "image/NITF",
                        "image/HDF"},
                    supportedFormats);

            String[] supportedCRS = inspector.getGridSupportedCRS();
            assertArrayEquals(
                    new String[]{
                        "urn:ogc:def:crs:EPSG::4269",
                        "urn:ogc:def:crs:EPSG::4326"},
                    supportedCRS);

        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    private void validateArcGISServer_10_ResponseParsing(String resourceName) throws SAXException, IOException {

        InputStream inputStream = null;
        try {
            inputStream = getClass().getResourceAsStream(resourceName);
            assertNotNull(inputStream);

            WCSDescribeCoverageInspector_1_1_X inspector = new WCSDescribeCoverageInspector_1_1_X(
                    DocumentUtil.createDocument(inputStream),
                    "1");

            assertEquals("urn:ogc:def:crs:EPSG::4326", inspector.getGridBaseCRSAsString());

            assertEquals("", inspector.getGridTypeAsString());

            double[] gridOffsets = inspector.getGridOffsets();
            assertNotNull(gridOffsets);
            assertEquals(0.00035088406961701602, gridOffsets[0], 1e-12);
            assertEquals(-0.00035088406961701602, gridOffsets[1], 1e-12);

            double[] gridOrigin = inspector.getGridOrigin();
            assertNotNull(gridOrigin);
            assertEquals(-130.23265257377369, gridOrigin[0], 1e-12);
            assertEquals(52.877088523030338, gridOrigin[1], 1e-12);

            double[] gridLowerCorner = inspector.getGridLowerCorner();
            assertNotNull(gridLowerCorner);
            assertEquals(-130.23282801580851, gridLowerCorner[0], 1e-9);
            assertEquals(21.742267815738465, gridLowerCorner[1], 1e-10);


            double[] gridUpperCorner = inspector.getGridUpperCorner();
            assertNotNull(gridUpperCorner);
            assertEquals(-63.672225313878272, gridUpperCorner[0], 1e-10);
            assertEquals(52.877263965065147, gridUpperCorner[1], 1e-10);

            assertEquals("Byte", inspector.getGridDataTypeAsString());

            String[] supportedFormats = inspector.getGridSupportedFormats();
            assertArrayEquals(
                    new String[]{
                        "image/GeoTIFF",
                        "image/NITF",
                        "image/HDF",
                        "image/JPEG",
                        "image/JPEG2000",
                        "image/PNG"},
                    supportedFormats);

            String[] supportedCRS = inspector.getGridSupportedCRS();
            assertArrayEquals(
                    new String[]{
                        "urn:ogc:def:crs:EPSG::4326",
                        "urn:ogc:def:crs:EPSG::4326"},
                    supportedCRS);

        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    private void validateGeoServerResponseParsing(String resourceName) throws SAXException, IOException {
        InputStream inputStream = null;
        try {
            inputStream = getClass().getResourceAsStream(resourceName);
            assertNotNull(inputStream);

            WCSDescribeCoverageInspector_1_1_X inspector = new WCSDescribeCoverageInspector_1_1_X(
                    DocumentUtil.createDocument(inputStream),
                    "sample:ned");

            assertEquals("urn:ogc:def:crs:EPSG::4269", inspector.getGridBaseCRSAsString());

            assertEquals("urn:ogc:def:method:WCS:1.1:2dGridIn2dCrs", inspector.getGridTypeAsString());

            double[] gridOffsets = inspector.getGridOffsets();
            assertNotNull(gridOffsets);
            assertEquals(9.259259259300022E-5, gridOffsets[0], 1e-12);
            assertEquals(0, gridOffsets[1], 1e-12);
            assertEquals(0, gridOffsets[2], 1e-12);
            assertEquals(-9.259259259300022E-5, gridOffsets[3], 1e-12);

            double[] gridOrigin = inspector.getGridOrigin();
            assertNotNull(gridOrigin);
            assertEquals(-125.00050925930371, gridOrigin[0], 1e-12);
            assertEquals(50.000509259254905, gridOrigin[1], 1e-12);

            double[] gridLowerCorner = inspector.getGridLowerCorner();
            assertNotNull(gridLowerCorner);
            assertEquals(23.999444444325704, gridLowerCorner[0], 1e-9);
            assertEquals(-125.0005555556, gridLowerCorner[1], 1e-10);


            double[] gridUpperCorner = inspector.getGridUpperCorner();
            assertNotNull(gridUpperCorner);
            assertEquals(50.0005555555512, gridUpperCorner[0], 1e-10);
            assertEquals(-65.99944444444448, gridUpperCorner[1], 1e-10);

            assertEquals("", inspector.getGridDataTypeAsString());

            String[] supportedFormats = inspector.getGridSupportedFormats();
            assertArrayEquals(
                    new String[]{
                        "image/tiff;subtype=\"geotiff\"",
                        "image/gif",
                        "image/png",
                        "image/jpeg",
                        "image/tiff"},
                    supportedFormats);

            String[] supportedCRS = inspector.getGridSupportedCRS();
            assertArrayEquals(
                    new String[]{
                        "urn:ogc:def:crs:EPSG::4269",
                        "EPSG:4269"},
                    supportedCRS);

        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

}
