
package gov.usgs.cida.gdp.wps.parser;

import com.vividsolutions.jts.geom.Geometry;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import org.geotools.feature.FeatureIterator;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;

/**
 *
 * @author tkunicki
 */
public class GMLStreamingFeatureCollectionTest {
    
	private static File conusStates;
	private static File arcGis;
	
	@BeforeClass
	public static void before() throws URISyntaxException {
		URL url = GMLStreamingFeatureCollectionTest.class.getResource("/gml/conus-states-sample.xml");
		conusStates = new File(url.toURI());
		
		url = GMLStreamingFeatureCollectionTest.class.getResource("/gml/arcgis-sample.xml");
		arcGis = new File(url.toURI());
	}
	
    public GMLStreamingFeatureCollectionTest() {
    }

    @Test
	@Ignore
    public void testSimpleWFSParse() {
        GMLStreamingFeatureCollection fc = new GMLStreamingFeatureCollection(conusStates);
        assertThat(fc, notNullValue());
        assertThat(fc.getBounds(), notNullValue());
        assertThat(fc.getSchema(), notNullValue());
        assertThat(fc.size(), equalTo(1));
        
        FeatureIterator fi = null;
        try {
            fi = fc.features();
            assertThat(fi, notNullValue());
            assertThat(fi.hasNext(), is(true));
            while(fi.hasNext()) {
                Feature f = fi.next();
                assertThat(f, instanceOf(SimpleFeature.class));
                SimpleFeature sf = (SimpleFeature)f;
                Object go = sf.getDefaultGeometry();
                assertThat(go, instanceOf(Geometry.class));
            }
        } finally {
            if (fi != null) {
                fi.close();
            }
        }
    }

	@Test
	public void testArcGISWFSParse() {
		try {
			GMLStreamingFeatureCollection fc = new GMLStreamingFeatureCollection(arcGis);
		} catch (RuntimeException ex) {
			assertEquals(ex.getMessage(), "Error extracting CRS from feature geometry");
		}
	}
}