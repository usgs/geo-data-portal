package gov.usgs.cida.gdp.coreprocessing.analysis;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.opengis.referencing.operation.TransformException;
import ucar.unidata.geoloc.LatLonRect;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author isuftin
 */
public class GeoToolsNetCDFUtilityTest {

    private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GeoToolsNetCDFUtilityTest.class);

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        log.debug("Started testing class.");
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        log.debug("Ended testing class.");
    }

    @Test
    public void testCreateNewGeoToolsNetCDFUtilityObject() {
        GeoToolsNetCDFUtility result = new GeoToolsNetCDFUtility();
        assertEquals(result.getClass(), GeoToolsNetCDFUtility.class);
    }

    @Test
    public void testGetLatLonRectFromEnvelope() {
        ReferencedEnvelope input = new ReferencedEnvelope();
        LatLonRect result = null;
        try {
            result = GeoToolsNetCDFUtility.getLatLonRectFromEnvelope(input);
        } catch (TransformException ex) {
            Logger.getLogger(GeoToolsNetCDFUtilityTest.class.getName()).log(Level.SEVERE, null, ex);
            fail(ex.getMessage());
        }
        assertNotNull(result);
        assertEquals(result.getUpperRightPoint().getLatitude(), 0.0, 0.0);
        assertEquals(result.getUpperRightPoint().getLongitude(), -1.0, 0.0);
        assertEquals(result.getLowerLeftPoint().getLatitude(), -1.0, 0.0);
        assertEquals(result.getLowerLeftPoint().getLongitude(), 0.0, 0.0);

    }
}
