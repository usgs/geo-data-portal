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
public class WCSDescribeCoverageInspector_1_1Test {

    public WCSDescribeCoverageInspector_1_1Test() {
    }

    @Test
    public void testArcGISServerResponse_1_1_0() throws SAXException, IOException {
        // ArcGIS server (10.x??)
        // wcs -> "http://www.opengis.net/wcs/1.1";
        // ows -> "http://www.opengis.net/ows";
        // owcs -> "http://www.opengis.net/wcs/1.1/ows";
        validateArcGISServerResponseParsing("/ArcGIS.DescribeCoverage.1.1.0.xml");
    }

    @Test
    public void testArcGISServerResponse_1_1_1() throws SAXException, IOException {
        // ArcGIS server (10.x??)
        // wcs -> "http://www.opengis.net/wcs/1.1.1";
        // ows -> "http://www.opengis.net/ows/1.1";
        // owcs -> <none>;
        validateArcGISServerResponseParsing("/ArcGIS.DescribeCoverage.1.1.1.xml");
    }

    public void validateArcGISServerResponseParsing(String resourceName) throws SAXException, IOException {

        InputStream inputStream = null;
        try {
            inputStream = getClass().getResourceAsStream(resourceName);
            assertNotNull(inputStream);

            WCSDescribeCoverageInspector_1_1 inspector = new WCSDescribeCoverageInspector_1_1(
                    DocumentUtil.createDocument(inputStream),
                    "1");

            assertEquals("urn:ogc:def:crs:EPSG::4269", inspector.getGridBaseCRSAsString());
            assertEquals("", inspector.getGridTypeAsString());

            double[] gridOffsets = inspector.getGridOffsets();
            assertNotNull(gridOffsets);
            assertEquals(0.00027777777799542502, gridOffsets[0], 1e-12);
            assertEquals(-0.00027777777779647273, gridOffsets[1], 1e-12);

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
                    new String[]{"image/GeoTIFF", "image/NITF", "image/HDF"},
                    supportedFormats);

        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

}
