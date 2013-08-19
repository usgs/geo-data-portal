package gov.usgs.cida.gdp.wps.util;

import gov.usgs.cida.gdp.wps.parser.GMLStreamingFeatureCollectionTest;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import org.apache.commons.io.FileUtils;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.instanceOf;
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

	@Test
	public void testDetermineEnvelopeFromBoundedByNodeFromArcGISExampleFile() throws IOException {
		System.out.println("testDetermineEnvelopeFromBoundedByNodeFromArcGISExampleFile");
		File file = arcGis;
		ReferencedEnvelope result = GMLUtil.determineCollectionEnvelope(file);
		assertNotNull(result);
		assertEquals(2, result.getCoordinateReferenceSystem().getCoordinateSystem().getDimension());
		assertEquals("World between 85°S and 85°N.", result.getCoordinateReferenceSystem().getDomainOfValidity().getDescription().toString());
		assertEquals("EPSG:3857", result.getCoordinateReferenceSystem().getIdentifiers().toArray()[0].toString());
		assertEquals(-7187336.4004999995, result.getMaxX(), 1E-1);
		assertEquals(6498778.364500001, result.getMaxY(), 1E-1);
		assertEquals(-1.78385662145E7, result.getMinX(), 1E-1);
		assertEquals(1999393.0584000014, result.getMinY(), 1E-1);
	}
	
	@Test
	public void testExtractCRSFromBoundedByNodeFromArcGISExampleFile() throws IOException {
		System.out.println("testExtractCRSAgainstArcGISExampleUsingFile");
		File file = arcGis;
		CoordinateReferenceSystem result = GMLUtil.determineCollectionCRS(file);
		assertNotNull(result);
		assertEquals(2, result.getCoordinateSystem().getDimension());
		assertEquals("World between 85°S and 85°N.", result.getDomainOfValidity().getDescription().toString());
		assertEquals("EPSG:3857", result.getIdentifiers().toArray()[0].toString());
	}
	
	@Test
	public void testExtractCRSUsingMissingFile() {
		System.out.println("testExtractCRSUsingMissingFile");
		File file = new File(FileUtils.getTempDirectory(), "I_do_not_exist");
		try {
			GMLUtil.determineCollectionCRS(file);
		} catch (IOException ex) {
			assertThat(ex, instanceOf(IOException.class));
			assertThat(ex.getCause(), instanceOf(FileNotFoundException.class));
		}
	}

	@Test
	public void testExtractCRSFromBoundedByNodeFromConusStatesExampleFile() throws IOException {
		System.out.println("testExtractCRSAgainstConusStatesExampleUsingFile");
		File file = conusStates;
		CoordinateReferenceSystem result = GMLUtil.determineCollectionCRS(file);
		assertNull(result);
	}
}