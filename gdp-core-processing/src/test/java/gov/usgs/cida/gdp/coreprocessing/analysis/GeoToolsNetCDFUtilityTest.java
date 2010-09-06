/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.usgs.cida.gdp.coreprocessing.analysis;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.opengis.referencing.operation.TransformException;
import ucar.unidata.geoloc.LatLonRect;
import org.geotools.geometry.jts.ReferencedEnvelope;
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
public class GeoToolsNetCDFUtilityTest {

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