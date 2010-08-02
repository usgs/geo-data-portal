package gov.usgs.gdp.analysis.grid;

import gov.usgs.gdp.analysis.GeoToolsNetCDFUtility;
import gov.usgs.gdp.helper.FileHelper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Formatter;


import org.geotools.data.FeatureSource;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.SchemaException;
import org.geotools.referencing.crs.DefaultGeographicCRS;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;

import org.opengis.referencing.operation.TransformException;

import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.dt.grid.GridDataset;
import ucar.nc2.ft.FeatureDataset;
import ucar.nc2.ft.FeatureDatasetFactoryManager;
import ucar.unidata.geoloc.LatLonPointImpl;
import ucar.unidata.geoloc.LatLonRect;
import visad.GridCoordinateSystem;

import static gov.usgs.gdp.analysis.grid.GridCellHelper.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class GridCellCoverageTest {
	
	private String ncLocation;
	private String sfLocation;
	private FeatureDataset dataset;
	private FileDataStore dataStore;
	private String attributeName;
	private String variableName;
	private Range timeRange;
	private BufferedWriter writer;
	
	@Before
	public void setUp() throws IOException, InvalidRangeException {
		ncLocation = getResourceDir() + FileHelper.getSeparator() + "testGridCellCoverageTYX.ncml";
		sfLocation = getResourceDir() + FileHelper.getSeparator() + "Trout_Lake_HRUs_rotated_geo_WGS84.shp";
		dataStore = FileDataStoreFinder.getDataStore(new File(sfLocation));
		attributeName = "GRIDCODE";
		variableName = GridTypeTest.DATATYPE_RH;
		timeRange = new Range(30, 39);
		writer = new BufferedWriter(new OutputStreamWriter(System.out));
	}
	
	@After
	public void tearDown() throws IOException {
		if (dataset != null) {
			dataset.close();
		}
		if (dataStore != null) {
			dataStore.dispose();
		}
	}
	
	@Test
	public void testWeightedGenerateTYX() throws IOException, InvalidRangeException, FactoryException, TransformException, SchemaException {
		FeatureCoverageWeightedGridStatistics.generate(
				getFeatureCollection(ncLocation),
				attributeName,
				(GridDataset)dataset,
				variableName,
				timeRange,
				Arrays.asList(new FeatureCoverageWeightedGridStatisticsWriter.Statistic[] {
						FeatureCoverageWeightedGridStatisticsWriter.Statistic.mean,
						FeatureCoverageWeightedGridStatisticsWriter.Statistic.maximum, }),
				writer,
				false,
				","
		);
		// Check it in some way?
	}
	
	@Test
	public void testWeightedGenerateYX() throws InvalidRangeException, IOException, FactoryException, TransformException, SchemaException {
		ncLocation =	getResourceDir() + FileHelper.getSeparator() + "testGridCellCoverageYX.ncml";
		
		FeatureCoverageWeightedGridStatistics.generate(
				getFeatureCollection(ncLocation),
				attributeName,
				(GridDataset)dataset,
				variableName,
				timeRange,
				Arrays.asList(new FeatureCoverageWeightedGridStatisticsWriter.Statistic[] {
						FeatureCoverageWeightedGridStatisticsWriter.Statistic.mean,
						FeatureCoverageWeightedGridStatisticsWriter.Statistic.maximum, }),
				writer,
				false,
				","
		);
	}
	
	@Test (expected=InvalidRangeException.class)
	public void testWeightedGenerateSmallX() throws InvalidRangeException, IOException, FactoryException, TransformException, SchemaException {
		ncLocation =	getResourceDir() + FileHelper.getSeparator() + "testCoverageTYXSmallX.ncml";
		
		FeatureCoverageWeightedGridStatistics.generate(
				getFeatureCollection(ncLocation),
				attributeName,
				(GridDataset)dataset,
				variableName,
				timeRange,
				Arrays.asList(new FeatureCoverageWeightedGridStatisticsWriter.Statistic[] {
						FeatureCoverageWeightedGridStatisticsWriter.Statistic.mean,
						FeatureCoverageWeightedGridStatisticsWriter.Statistic.maximum, }),
				writer,
				false,
				","
		);
	}
	
	@Test (expected=InvalidRangeException.class)
	public void testWeightedGenerateSmallY() throws InvalidRangeException, IOException, FactoryException, TransformException, SchemaException {
		ncLocation =	getResourceDir() + FileHelper.getSeparator() + "testCoverageTYXSmallY.ncml";
		
		FeatureCoverageWeightedGridStatistics.generate(
				getFeatureCollection(ncLocation),
				attributeName,
				(GridDataset)dataset,
				variableName,
				timeRange,
				Arrays.asList(new FeatureCoverageWeightedGridStatisticsWriter.Statistic[] {
						FeatureCoverageWeightedGridStatisticsWriter.Statistic.mean,
						FeatureCoverageWeightedGridStatisticsWriter.Statistic.maximum, }),
				writer,
				false,
				","
		);
	}
	
	@Test (expected=IllegalStateException.class)
	public void testWeightedGenerateTZYXUnsupported() throws InvalidRangeException, IOException, FactoryException, TransformException, SchemaException {
		ncLocation = getResourceDir() + FileHelper.getSeparator() + "testGridCellCoverageTZYX.ncml";
		timeRange = new Range(0, 0);

		FeatureCoverageWeightedGridStatistics.generate(
				getFeatureCollection(ncLocation),
				attributeName,
				(GridDataset)dataset,
				variableName,
				timeRange,
				Arrays.asList(new FeatureCoverageWeightedGridStatisticsWriter.Statistic[] {
						FeatureCoverageWeightedGridStatisticsWriter.Statistic.mean,
						FeatureCoverageWeightedGridStatisticsWriter.Statistic.maximum, }),
				writer,
				false,
				","
		);
	}
	
	@Test
	public void testWeightedGenerateAllStats() throws IOException, InvalidRangeException, FactoryException, TransformException, SchemaException {
		FeatureCoverageWeightedGridStatistics.generate(
				getFeatureCollection(ncLocation),
				attributeName,
				(GridDataset)dataset,
				variableName,
				timeRange,
				Arrays.asList(new FeatureCoverageWeightedGridStatisticsWriter.Statistic[] {
						FeatureCoverageWeightedGridStatisticsWriter.Statistic.mean,
						FeatureCoverageWeightedGridStatisticsWriter.Statistic.maximum, 
						FeatureCoverageWeightedGridStatisticsWriter.Statistic.minimum,
						FeatureCoverageWeightedGridStatisticsWriter.Statistic.variance,
						FeatureCoverageWeightedGridStatisticsWriter.Statistic.std_dev,
						FeatureCoverageWeightedGridStatisticsWriter.Statistic.weight_sum,
						FeatureCoverageWeightedGridStatisticsWriter.Statistic.count,
				}),
				writer,
				false,
				","
		);
	}
	
	@Test
	public void testRangesFromLatLonRect_Standard() throws TransformException, IOException, InvalidRangeException {
		LatLonRect llr = makeLatLonRect(ncLocation);
		GridCoordSystem gcs = getGridCoordinateSystem();
		Range[] range = FeatureCoverageWeightedGridStatistics.getRangesFromLatLonRect(llr, gcs);
		assertNotNull(range);
	}
	
	@Test (expected=InvalidRangeException.class)
	public void testRangesFromLatLonRect_InvalidRange() throws InvalidRangeException, IOException {
		getFeatureCollection(ncLocation);
		LatLonRect llr =new LatLonRect(
                new LatLonPointImpl(-1, -1),
                new LatLonPointImpl(-1, -1));
		GridCoordSystem gcs = getGridCoordinateSystem();
		FeatureCoverageWeightedGridStatistics.getRangesFromLatLonRect(llr, gcs);
	}
	
	@Test
	public void testRangesFromLatLonRect_Lower1LessThan() throws InvalidRangeException, IOException {
		getFeatureCollection(ncLocation);
		LatLonRect llr =new LatLonRect(
                new LatLonPointImpl(42, -89),
                new LatLonPointImpl(43, -88));
		GridCoordSystem gcs = getGridCoordinateSystem();
		FeatureCoverageWeightedGridStatistics.getRangesFromLatLonRect(llr, gcs);
	}
	
	@Test
	public void testRangesFromLatLonRect_Lower0LessThan() throws InvalidRangeException, IOException {
		getFeatureCollection(ncLocation);
		LatLonRect llr =new LatLonRect(
                new LatLonPointImpl(42, -89),
                new LatLonPointImpl(43, -90));
		GridCoordSystem gcs = getGridCoordinateSystem();
		FeatureCoverageWeightedGridStatistics.getRangesFromLatLonRect(llr, gcs);
	}
	
	@Test
	public void testTopOfGrid() throws IOException, InvalidRangeException {
		ncLocation = getResourceDir() + FileHelper.getSeparator() + "testCoverageTYXSmallestValid.ncml";
		getFeatureCollection(ncLocation);
		LatLonRect llr =new LatLonRect(
                new LatLonPointImpl(42, -91),
                new LatLonPointImpl(42, -89));
		GridCoordSystem gcs = getGridCoordinateSystem();
		FeatureCoverageWeightedGridStatistics.getRangesFromLatLonRect(llr, gcs);
	}
	
	@Test
	public void testBottomOfGrid() throws IOException, InvalidRangeException {
		ncLocation = getResourceDir() + FileHelper.getSeparator() + "testCoverageTYXSmallestValid.ncml";
		getFeatureCollection(ncLocation);
		LatLonRect llr =new LatLonRect(
                new LatLonPointImpl(40, -91),
                new LatLonPointImpl(40, -89));
		GridCoordSystem gcs = getGridCoordinateSystem();
		FeatureCoverageWeightedGridStatistics.getRangesFromLatLonRect(llr, gcs);
	}
	
	@Test
	public void testLeftOfGrid() throws IOException, InvalidRangeException {
		ncLocation = getResourceDir() + FileHelper.getSeparator() + "testCoverageTYXSmallestValid.ncml";
		getFeatureCollection(ncLocation);
		LatLonRect llr =new LatLonRect(
                new LatLonPointImpl(40, -91),
                new LatLonPointImpl(42, -91));
		GridCoordSystem gcs = getGridCoordinateSystem();
		FeatureCoverageWeightedGridStatistics.getRangesFromLatLonRect(llr, gcs);
	}
	
	@Test
	public void testMiddleOfGrid() throws IOException, InvalidRangeException {
		ncLocation = getResourceDir() + FileHelper.getSeparator() + "testCoverageTYXSmallestValid.ncml";
		getFeatureCollection(ncLocation);
		LatLonRect llr =new LatLonRect(
                new LatLonPointImpl(41, -90),
                new LatLonPointImpl(41, -90));
		GridCoordSystem gcs = getGridCoordinateSystem();
		FeatureCoverageWeightedGridStatistics.getRangesFromLatLonRect(llr, gcs);
	}
	
	@Test
	public void testMiddleOfBiggerGrid() throws IOException, InvalidRangeException {
		ncLocation = getResourceDir() + FileHelper.getSeparator() + "testCoverageTYXFourByFour.ncml";
		getFeatureCollection(ncLocation);
		LatLonRect llr =new LatLonRect(
                new LatLonPointImpl(41, -90),
                new LatLonPointImpl(42, -89));
		GridCoordSystem gcs = getGridCoordinateSystem();
		FeatureCoverageWeightedGridStatistics.getRangesFromLatLonRect(llr, gcs);
	}
	
	@Test
	public void testTopMiddleOfBiggerGrid() throws IOException, InvalidRangeException {
		ncLocation = getResourceDir() + FileHelper.getSeparator() + "testCoverageTYXFourByFour.ncml";
		getFeatureCollection(ncLocation);
		LatLonRect llr =new LatLonRect(
                new LatLonPointImpl(43, -90),
                new LatLonPointImpl(42, -89));
		GridCoordSystem gcs = getGridCoordinateSystem();
		FeatureCoverageWeightedGridStatistics.getRangesFromLatLonRect(llr, gcs);
	}
	
	@Test
	public void testLeftMiddleOfBiggerGrid() throws IOException, InvalidRangeException {
		ncLocation = getResourceDir() + FileHelper.getSeparator() + "testCoverageTYXFourByFour.ncml";
		getFeatureCollection(ncLocation);
		LatLonRect llr =new LatLonRect(
                new LatLonPointImpl(41, -91),
                new LatLonPointImpl(42, -90));
		GridCoordSystem gcs = getGridCoordinateSystem();
		FeatureCoverageWeightedGridStatistics.getRangesFromLatLonRect(llr, gcs);
	}
	
	private FeatureCollection<SimpleFeatureType, SimpleFeature> getFeatureCollection(String ncLocation) throws IOException {
		dataset = FeatureDatasetFactoryManager.open(FeatureType.GRID, ncLocation, null, new Formatter());
		dataStore = FileDataStoreFinder.getDataStore(new File(sfLocation));
		FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = dataStore.getFeatureSource();
		FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection = featureSource.getFeatures();
		return featureCollection;
	}
	
	private GridCoordSystem getGridCoordinateSystem() {
		GridDataset gd = (GridDataset)dataset;
		GridDatatype gdt = gd.findGridDatatype(GridTypeTest.DATATYPE_RH);
		return gdt.getCoordinateSystem();
	}
	
	private LatLonRect makeLatLonRect(String ncLocation) throws TransformException, IOException {
		return GeoToolsNetCDFUtility.getLatLonRectFromEnvelope(getFeatureCollection(ncLocation).getBounds());
	}
}
