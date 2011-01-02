package gov.usgs.cida.gdp.coreprocessing.analysis.grid;

import gov.usgs.cida.gdp.coreprocessing.Delimiter;
import org.junit.AfterClass;
import org.junit.BeforeClass;

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
import ucar.nc2.dt.grid.GridDataset;
import ucar.nc2.ft.FeatureDataset;
import ucar.nc2.ft.FeatureDatasetFactoryManager;

import static gov.usgs.cida.gdp.coreprocessing.GridCellHelper.*;

public class GridCellCoverageTest {

    private String ncLocation;
    private String sfLocation;
    private FeatureDataset dataset;
    private FileDataStore dataStore;
    private String attributeName;
    private String variableName;
    private Range timeRange;
    private BufferedWriter writer;
    private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GridCellCoverageTest.class);

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        log.debug("Started testing class.");
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        log.debug("Ended testing class.");
    }

    @Before
    public void setUp() throws IOException, InvalidRangeException {
        ncLocation = getResourceDir() + File.separator + "testGridCellCoverageTYX.ncml";
        sfLocation = getResourceDir() + File.separator + "Trout_Lake_HRUs_rotated_geo_WGS84.shp";
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
        FeatureCoverageWeightedGridStatistics.execute(
                getFeatureCollection(ncLocation),
                attributeName,
                (GridDataset) dataset,
                variableName,
                timeRange,
                Arrays.asList(new FeatureCoverageWeightedGridStatisticsWriter.Statistic[]{
                    FeatureCoverageWeightedGridStatisticsWriter.Statistic.mean,
                    FeatureCoverageWeightedGridStatisticsWriter.Statistic.maximum,}),
                writer,
                FeatureCoverageWeightedGridStatistics.GroupBy.FEATURE_ATTRIBUTE,
                Delimiter.COMMA);
        // Check it in some way?
    }

    @Test
    public void testWeightedGenerateYX() throws InvalidRangeException, IOException, FactoryException, TransformException, SchemaException {
        ncLocation = getResourceDir() + File.separator + "testGridCellCoverageYX.ncml";

        FeatureCoverageWeightedGridStatistics.execute(
                getFeatureCollection(ncLocation),
                attributeName,
                (GridDataset) dataset,
                variableName,
                timeRange,
                Arrays.asList(new FeatureCoverageWeightedGridStatisticsWriter.Statistic[]{
                    FeatureCoverageWeightedGridStatisticsWriter.Statistic.mean,
                    FeatureCoverageWeightedGridStatisticsWriter.Statistic.maximum,}),
                writer,
                FeatureCoverageWeightedGridStatistics.GroupBy.FEATURE_ATTRIBUTE,
                Delimiter.COMMA);
    }

    @Test(expected = InvalidRangeException.class)
    public void testWeightedGenerateSmallX() throws InvalidRangeException, IOException, FactoryException, TransformException, SchemaException {
        ncLocation = getResourceDir() + File.separator + "testCoverageTYXSmallX.ncml";

        FeatureCoverageWeightedGridStatistics.execute(
                getFeatureCollection(ncLocation),
                attributeName,
                (GridDataset) dataset,
                variableName,
                timeRange,
                Arrays.asList(new FeatureCoverageWeightedGridStatisticsWriter.Statistic[]{
                    FeatureCoverageWeightedGridStatisticsWriter.Statistic.mean,
                    FeatureCoverageWeightedGridStatisticsWriter.Statistic.maximum,}),
                writer,
                FeatureCoverageWeightedGridStatistics.GroupBy.FEATURE_ATTRIBUTE,
                Delimiter.COMMA);
    }

    @Test(expected = InvalidRangeException.class)
    public void testWeightedGenerateSmallY() throws InvalidRangeException, IOException, FactoryException, TransformException, SchemaException {
        ncLocation = getResourceDir() + File.separator + "testCoverageTYXSmallY.ncml";

        FeatureCoverageWeightedGridStatistics.execute(
                getFeatureCollection(ncLocation),
                attributeName,
                (GridDataset) dataset,
                variableName,
                timeRange,
                Arrays.asList(new FeatureCoverageWeightedGridStatisticsWriter.Statistic[]{
                    FeatureCoverageWeightedGridStatisticsWriter.Statistic.mean,
                    FeatureCoverageWeightedGridStatisticsWriter.Statistic.maximum,}),
                writer,
                FeatureCoverageWeightedGridStatistics.GroupBy.FEATURE_ATTRIBUTE,
                Delimiter.COMMA);
    }

    @Test(expected = IllegalStateException.class)
    public void testWeightedGenerateTZYXUnsupported() throws InvalidRangeException, IOException, FactoryException, TransformException, SchemaException {
        ncLocation = getResourceDir() + File.separator + "testGridCellCoverageTZYX.ncml";
        timeRange = new Range(0, 0);

        FeatureCoverageWeightedGridStatistics.execute(
                getFeatureCollection(ncLocation),
                attributeName,
                (GridDataset) dataset,
                variableName,
                timeRange,
                Arrays.asList(new FeatureCoverageWeightedGridStatisticsWriter.Statistic[]{
                    FeatureCoverageWeightedGridStatisticsWriter.Statistic.mean,
                    FeatureCoverageWeightedGridStatisticsWriter.Statistic.maximum,}),
                writer,
                FeatureCoverageWeightedGridStatistics.GroupBy.FEATURE_ATTRIBUTE,
                Delimiter.COMMA);
    }

    @Test
    public void testWeightedGenerateAllStats() throws IOException, InvalidRangeException, FactoryException, TransformException, SchemaException {
        FeatureCoverageWeightedGridStatistics.execute(
                getFeatureCollection(ncLocation),
                attributeName,
                (GridDataset) dataset,
                variableName,
                timeRange,
                Arrays.asList(new FeatureCoverageWeightedGridStatisticsWriter.Statistic[]{
                    FeatureCoverageWeightedGridStatisticsWriter.Statistic.mean,
                    FeatureCoverageWeightedGridStatisticsWriter.Statistic.maximum,
                    FeatureCoverageWeightedGridStatisticsWriter.Statistic.minimum,
                    FeatureCoverageWeightedGridStatisticsWriter.Statistic.variance,
                    FeatureCoverageWeightedGridStatisticsWriter.Statistic.std_dev,
                    FeatureCoverageWeightedGridStatisticsWriter.Statistic.weight_sum,
                    FeatureCoverageWeightedGridStatisticsWriter.Statistic.count,}),
                writer,
                FeatureCoverageWeightedGridStatistics.GroupBy.FEATURE_ATTRIBUTE,
                Delimiter.COMMA);
    }

    private FeatureCollection<SimpleFeatureType, SimpleFeature> getFeatureCollection(String ncLocation) throws IOException {
        dataset = FeatureDatasetFactoryManager.open(FeatureType.GRID, ncLocation, null, new Formatter());
        dataStore = FileDataStoreFinder.getDataStore(new File(sfLocation));
        FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = dataStore.getFeatureSource();
        FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection = featureSource.getFeatures();
        return featureCollection;
    }
}
