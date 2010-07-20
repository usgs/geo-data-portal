package gov.usgs.gdp.analysis.grid;

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

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;

import org.opengis.referencing.operation.TransformException;

import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.dt.grid.GridDataset;
import ucar.nc2.ft.FeatureDataset;
import ucar.nc2.ft.FeatureDatasetFactoryManager;

import static gov.usgs.gdp.analysis.grid.GridCellHelper.*;

public class GridCellCoverageTest {
	
	@BeforeClass
	public static void setUpAll() {
		setupResourceDir();
	}
	
	@Test
	public void testWeightedGenerateTYX() throws IOException, InvalidRangeException, FactoryException, TransformException, SchemaException {
		String ncLocation =	RESOURCE_PATH + "testGridCellCoverageTYX.ncml";
		String sfLocation = RESOURCE_PATH + "Trout_Lake_HRUs_rotated_geo_WGS84.shp";
		String attributeName = "GRIDCODE";
		String variableName = GridTypeTest.DATATYPE_RH;
		Range timeRange = new Range(30, 39);

		FeatureDataset dataset = FeatureDatasetFactoryManager.open(null, ncLocation, null, new Formatter());
		FileDataStore dataStore = FileDataStoreFinder.getDataStore(new File(sfLocation));
		FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = dataStore.getFeatureSource();
		FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection = featureSource.getFeatures();

		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out));
		
		FeatureCoverageWeightedGridStatistics.generate(
				featureCollection,
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

		if (dataset != null) {
			dataset.close();
		}
		if (dataStore != null) {
			dataStore.dispose();
		}
	}
	
	@Test (expected=InvalidRangeException.class)
	public void testWeightedGenerateNegativeRange() throws InvalidRangeException, IOException, FactoryException, TransformException, SchemaException {
		String ncLocation =	RESOURCE_PATH + "testGridCellCoverageTYX.ncml";
		String sfLocation = RESOURCE_PATH + "Trout_Lake_HRUs_rotated_geo_WGS84.shp";
		String attributeName = "GRIDCODE";
		String variableName = GridTypeTest.DATATYPE_RH;
		Range timeRange = new Range(-1, -1);

		FeatureDataset dataset = FeatureDatasetFactoryManager.open(null, ncLocation, null, new Formatter());
		FileDataStore dataStore = FileDataStoreFinder.getDataStore(new File(sfLocation));
		FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = dataStore.getFeatureSource();
		FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection = featureSource.getFeatures();

		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out));
		
		FeatureCoverageWeightedGridStatistics.generate(
				featureCollection,
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

		if (dataset != null) {
			dataset.close();
		}
		if (dataStore != null) {
			dataStore.dispose();
		}
	}
	
	@Test (expected=InvalidRangeException.class)
	public void testWeightedGenerateFirstBiggerThanSecondRange() throws InvalidRangeException, IOException, FactoryException, TransformException, SchemaException {
		String ncLocation =	RESOURCE_PATH + "testGridCellCoverageTYX.ncml";
		String sfLocation = RESOURCE_PATH + "Trout_Lake_HRUs_rotated_geo_WGS84.shp";
		String attributeName = "GRIDCODE";
		String variableName = GridTypeTest.DATATYPE_RH;
		Range timeRange = new Range(2, 1);

		FeatureDataset dataset = FeatureDatasetFactoryManager.open(null, ncLocation, null, new Formatter());
		FileDataStore dataStore = FileDataStoreFinder.getDataStore(new File(sfLocation));
		FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = dataStore.getFeatureSource();
		FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection = featureSource.getFeatures();

		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out));
		
		FeatureCoverageWeightedGridStatistics.generate(
				featureCollection,
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

		if (dataset != null) {
			dataset.close();
		}
		if (dataStore != null) {
			dataStore.dispose();
		}
	}
	
	@Test
	public void testWeightedGenerateYX() throws InvalidRangeException, IOException, FactoryException, TransformException, SchemaException {
		String ncLocation =	RESOURCE_PATH + "testGridCellCoverageYX.ncml";
		String sfLocation = RESOURCE_PATH + "Trout_Lake_HRUs_rotated_geo_WGS84.shp";
		String attributeName = "GRIDCODE";
		String variableName = GridTypeTest.DATATYPE_RH;
		Range timeRange = new Range(0, 0);

		FeatureDataset dataset = FeatureDatasetFactoryManager.open(null, ncLocation, null, new Formatter());
		FileDataStore dataStore = FileDataStoreFinder.getDataStore(new File(sfLocation));
		FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = dataStore.getFeatureSource();
		FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection = featureSource.getFeatures();

		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out));
		
		FeatureCoverageWeightedGridStatistics.generate(
				featureCollection,
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

		if (dataset != null) {
			dataset.close();
		}
		if (dataStore != null) {
			dataStore.dispose();
		}
	}
	
	@Test (expected=IllegalStateException.class)
	public void testWeightedGenerateTZYXUnsupported() throws InvalidRangeException, IOException, FactoryException, TransformException, SchemaException {
		String ncLocation =	RESOURCE_PATH + "testGridCellCoverageTZYX.ncml";
		String sfLocation = RESOURCE_PATH + "Trout_Lake_HRUs_rotated_geo_WGS84.shp";
		String attributeName = "GRIDCODE";
		String variableName = GridTypeTest.DATATYPE_RH;
		Range timeRange = new Range(1, 1);

		FeatureDataset dataset = FeatureDatasetFactoryManager.open(FeatureType.GRID, ncLocation, null, new Formatter());
		FileDataStore dataStore = FileDataStoreFinder.getDataStore(new File(sfLocation));
		FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = dataStore.getFeatureSource();
		FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection = featureSource.getFeatures();

		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out));
		
		FeatureCoverageWeightedGridStatistics.generate(
				featureCollection,
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

		if (dataset != null) {
			dataset.close();
		}
		if (dataStore != null) {
			dataStore.dispose();
		}
	}
}
