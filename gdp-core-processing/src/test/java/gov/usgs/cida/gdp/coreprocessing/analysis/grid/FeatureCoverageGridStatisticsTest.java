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
        executeAndCharacterize("yx");
    }
    
    @Test
    public void testTYX() throws IOException, InvalidRangeException, FactoryException, TransformException, SchemaException, URISyntaxException {
        executeAndCharacterize("tyx");
    }
    
    @Test
    public void testZYX() throws IOException, InvalidRangeException, FactoryException, TransformException, SchemaException, URISyntaxException {
        executeAndCharacterize("zyx");
    }
    
    @Test
    public void testTZYX() throws IOException, InvalidRangeException, FactoryException, TransformException, SchemaException, URISyntaxException {
        executeAndCharacterize("tzyx");
    }
    
    private GridDatatype getGrid(String gridName) {
        GridDatatype gdt = GRID_DATASET.findGridDatatype(gridName);
        assertThat(gdt, is(notNullValue()));
        return gdt;
    }
    
    private void executeAndCharacterize(String gridName) throws URISyntaxException, IOException, InvalidRangeException, FactoryException, TransformException, SchemaException {
        CharacterizationUtil.characterize(
                RESPONSES_DIRECTORY,
                getClass(),
                gridName,
                execute(gridName),
                "csv");
    }
    
    private String execute(String gridName) throws IOException, InvalidRangeException, FactoryException, TransformException, SchemaException {
        FeatureCoverageGridStatistics.execute(
                featureCollection,
                "GRIDCODE",
                getGrid(gridName),
                Arrays.asList(Statistic.values()),
                writer,
                GroupBy.STATISTIC,
                Delimiter.COMMA,
                true,
                true,
                true);
        return writer.toString();
    }
    
    
    
    private static URL getResourceURL(String fileName) {
        return FeatureCoverageGridStatisticsTest.class.getClassLoader().getResource(fileName);
    }

}