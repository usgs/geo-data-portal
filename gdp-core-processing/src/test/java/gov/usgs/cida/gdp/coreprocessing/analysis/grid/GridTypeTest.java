package gov.usgs.cida.gdp.coreprocessing.analysis.grid;

import static org.junit.Assert.fail;
import static gov.usgs.cida.gdp.coreprocessing.GridCellHelper.*;

import gov.usgs.cida.gdp.utilities.FileHelper;

import java.io.IOException;
import java.util.Formatter;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import org.junit.Ignore;
import org.junit.Test;

import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.dt.grid.GridDataset;
import ucar.nc2.ft.FeatureDataset;
import ucar.nc2.ft.FeatureDatasetFactoryManager;

/**
 * @author jordan
 *
 */
public class GridTypeTest {

    public static final String DATATYPE_RH = "rh";
    private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GridTypeTest.class);

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        log.debug("Started testing class.");
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        log.debug("Ended testing class.");
    }

    @Test
    public void testSimpleTYXGrid() throws IOException {
        String datasetUrl = getResourceDir() + FileHelper.getSeparator() + "testSimpleTYXGrid.ncml";
        if (getGridType(datasetUrl, DATATYPE_RH) != GridType.TYX) {
            fail("Should be TYX dataset.");
        }
    }

    @Test
    public void testSimpleTZYXGrid() throws IOException {
        String datasetUrl = getResourceDir() + FileHelper.getSeparator() + "testSimpleTZYXGrid.ncml";
        if (getGridType(datasetUrl, DATATYPE_RH) != GridType.TZYX) {
            fail("Should be TZYX dataset.");
        }
    }

    @Test
    public void testSimpleYXGrid() throws IOException {
        String datasetUrl = getResourceDir() + FileHelper.getSeparator() + "testSimpleYXGrid.ncml";
        if (getGridType(datasetUrl, DATATYPE_RH) != GridType.YX) {
            fail("Should be YX dataset.");
        }
    }

    @Test
    public void testSimpleZYXGrid() throws IOException {
        String datasetUrl = getResourceDir() + FileHelper.getSeparator() + "testSimpleZYXGrid.ncml";
        if (getGridType(datasetUrl, DATATYPE_RH) != GridType.ZYX) {
            fail("Should be ZYX dataset");
        }
    }

    /**
     * Basically the OTHER is tricky to do.  So right now it isn't working.
     * I don't know if my ncml is bad, or netCDF java can't recognize it
     * @throws IOException
     */
    @Test
    @Ignore // throws null pointer exception // cannot find dataset
    public void testSimpleOtherGrid() throws IOException {
        String datasetUrl = getResourceDir() + FileHelper.getSeparator() + "testSimpleOtherGrid.ncml";
        GridType gt = getGridType(datasetUrl, DATATYPE_RH);
        if (gt != GridType.OTHER) {
            fail("Should be OTHER dataset not " + gt.toString());
        }
    }

    public static GridType getGridType(String filename, String type) throws IOException {
        FeatureDataset fd = FeatureDatasetFactoryManager.open(null, filename, null, new Formatter(System.err));
        GridDataset dataset = (GridDataset) fd;
        GridDatatype gdt = dataset.findGridDatatype(type);
        GridCoordSystem gcs = gdt.getCoordinateSystem();
        return GridType.findGridType(gcs);
    }
}
