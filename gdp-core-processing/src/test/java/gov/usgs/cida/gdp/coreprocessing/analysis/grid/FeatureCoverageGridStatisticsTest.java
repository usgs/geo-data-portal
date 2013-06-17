/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usgs.cida.gdp.coreprocessing.analysis.grid;

import gov.usgs.cida.gdp.coreprocessing.Delimiter;
import gov.usgs.cida.gdp.coreprocessing.analysis.grid.Statistics1DWriter.GroupBy;
import gov.usgs.cida.gdp.coreprocessing.analysis.grid.Statistics1DWriter.Statistic;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Formatter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.StringBuilderWriter;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.SchemaException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.dt.grid.GridDataset;
import ucar.nc2.ft.FeatureDataset;
import ucar.nc2.ft.FeatureDatasetFactoryManager;

/**
 *
 * @author tkunicki
 */
public class FeatureCoverageGridStatisticsTest {
    
    static GridDataset GRID_DATASET;
    static FileDataStore FEATURE_DATASTORE;
    static File RESPONSES_DIRECTORY;
    
    SimpleFeatureCollection featureCollection;
    StringBuilderWriter writer;
    
    public FeatureCoverageGridStatisticsTest() {
    }
    
    @BeforeClass
    public static void setUpClass() throws IOException {
        FeatureDataset fd = FeatureDatasetFactoryManager.open(
                FeatureType.GRID,
                getResourceURL("Sample_files/Trout_Lake_HRUs_coverage.ncml").toString(),
                null,
                new Formatter(System.err));
        if (fd instanceof GridDataset) {
            GRID_DATASET = (GridDataset)fd;
        }
        FEATURE_DATASTORE = FileDataStoreFinder.getDataStore(getResourceURL("Sample_files/Trout_Lake_HRUs_rotated_geo_WGS84.shp"));

        RESPONSES_DIRECTORY = new File(new File("src/test/resources"), "responses");
        if (!RESPONSES_DIRECTORY.exists()) {
            RESPONSES_DIRECTORY.mkdirs();
        }
    }
    
    @AfterClass
    public static void tearDownClass() {
        try { if (GRID_DATASET != null) { GRID_DATASET.close(); } } catch (IOException ignore) { }
        if (FEATURE_DATASTORE != null) { FEATURE_DATASTORE.dispose(); }
    }
    
    @Before
    public void setUp() throws IOException {
        writer = new StringBuilderWriter();
        featureCollection = FEATURE_DATASTORE.getFeatureSource().getFeatures();
    }
    
    @After
    public void tearDown() {
        IOUtils.closeQuietly(writer);
    }

    @Test
    public void validateTestPreconditions() {
        assertThat(GRID_DATASET, is(notNullValue()));
        assertThat(FEATURE_DATASTORE, is(notNullValue()));
        // weak sauce.  need to look at hamcrest file matchers
        assertThat(RESPONSES_DIRECTORY.exists(), is(equalTo(true)));
    }
    
    @Test
    public void testYX() throws IOException, InvalidRangeException, FactoryException, TransformException, SchemaException, URISyntaxException {
        FeatureCoverageWeightedGridStatistics.execute(
                featureCollection,
                "GRIDCODE",
                getGrid("yx"),
                (Range)null,
                Arrays.asList(Statistic.MINIMUM, Statistic.MAXIMUM),
                writer,
                GroupBy.FEATURE_ATTRIBUTE,
                Delimiter.COMMA,
                true,
                true,
                true);
                
        characterize("yx", writer.toString());
    }
    
    @Test
    public void testTYX() throws IOException, InvalidRangeException, FactoryException, TransformException, SchemaException, URISyntaxException {
        FeatureCoverageWeightedGridStatistics.execute(
                featureCollection,
                "GRIDCODE",
                getGrid("tyx"),
                (Range)null,
                Arrays.asList(Statistic.MINIMUM, Statistic.MAXIMUM),
                writer,
                GroupBy.FEATURE_ATTRIBUTE,
                Delimiter.COMMA,
                true,
                true,
                true);
                
        characterize("tyx", writer.toString());
    }
    
    @Test
    public void testZYX() throws IOException, InvalidRangeException, FactoryException, TransformException, SchemaException, URISyntaxException {
        FeatureCoverageWeightedGridStatistics.execute(
                featureCollection,
                "GRIDCODE",
                getGrid("zyx"),
                (Range)null,
                Arrays.asList(Statistic.MINIMUM, Statistic.MAXIMUM),
                writer,
                GroupBy.FEATURE_ATTRIBUTE,
                Delimiter.COMMA,
                true,
                true,
                true);
                
        characterize("zyx", writer.toString());
    }
    
    @Test
    public void testTZYX() throws IOException, InvalidRangeException, FactoryException, TransformException, SchemaException, URISyntaxException {
        FeatureCoverageWeightedGridStatistics.execute(
                featureCollection,
                "GRIDCODE",
                getGrid("tzyx"),
                (Range)null,
                Arrays.asList(Statistic.MINIMUM, Statistic.MAXIMUM),
                writer,
                GroupBy.FEATURE_ATTRIBUTE,
                Delimiter.COMMA,
                true,
                true,
                true);
                
        characterize("tzyx", writer.toString());
    }
    
    private GridDatatype getGrid(String gridName) {
        GridDatatype gdt = GRID_DATASET.findGridDatatype(gridName);
        assertThat(gdt, is(notNullValue()));
        return gdt;
    }
    
    private void characterize(String name, String response) throws URISyntaxException, IOException {

        File responseFile = new File(RESPONSES_DIRECTORY, getClass().getSimpleName() + "." + name + ".expected");

        if (responseFile.canRead()) {
            String expected = FileUtils.readFileToString(responseFile);
            assertThat(response, is(equalTo(expected)));
        } else {
            FileUtils.writeStringToFile(responseFile, response);
            // to harsh?  Maybe not, somebody forgot to commit test data...
            fail("characterization test failed, unable to find previous result to compare in " + responseFile.getAbsolutePath());
        }
    }
    
    private static URL getResourceURL(String fileName) {
        return FeatureCoverageGridStatisticsTest.class.getClassLoader().getResource(fileName);
    }

}