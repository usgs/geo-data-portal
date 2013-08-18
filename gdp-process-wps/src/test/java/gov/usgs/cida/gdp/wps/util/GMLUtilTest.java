package gov.usgs.cida.gdp.wps.util;

import gov.usgs.cida.gdp.wps.parser.GMLStreamingFeatureCollectionTest;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 *
 * @author isuftin
 */
public class GMLUtilTest {

	private static File conusStates;
	private static File arcGis;

	public GMLUtilTest() {
	}

	@BeforeClass
	public static void setUpClass() throws URISyntaxException {
		URL url = GMLStreamingFeatureCollectionTest.class.getResource("/gml/conus-states-sample.xml");
		conusStates = new File(url.toURI());

		url = GMLStreamingFeatureCollectionTest.class.getResource("/gml/arcgis-sample.xml");
		arcGis = new File(url.toURI());
	}

	@AfterClass
	public static void tearDownClass() {
	}

	@Test
	public void testExtractCRSFromBoundedByNodeFromArcGISExampleFile() {
		System.out.println("testExtractCRSAgainstArcGISExampleUsingFile");
		File file = arcGis;
		CoordinateReferenceSystem result = GMLUtil.extractCRS(file);
		assertNotNull(result);
		assertEquals(2, result.getCoordinateSystem().getDimension());
		assertEquals("World between 85°S and 85°N.", result.getDomainOfValidity().getDescription().toString());
		assertEquals("EPSG:3857", result.getIdentifiers().toArray()[0].toString());
	}

	@Test
	public void testExtractCRSFromBoundedByNodeFromConusStatesExampleFile() {
		System.out.println("testExtractCRSAgainstConusStatesExampleUsingFile");
		File file = conusStates;
		CoordinateReferenceSystem result = GMLUtil.extractCRS(file);
		assertNull(result);
	}
}