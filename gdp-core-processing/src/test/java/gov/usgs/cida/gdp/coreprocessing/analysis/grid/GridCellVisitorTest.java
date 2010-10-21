package gov.usgs.cida.gdp.coreprocessing.analysis.grid;

import static gov.usgs.cida.gdp.coreprocessing.GridCellHelper.*;
import java.io.File;
import static org.junit.Assert.assertEquals;


import java.io.IOException;
import java.util.Formatter;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import org.junit.Test;

import ucar.nc2.dt.GridDatatype;
import ucar.nc2.dt.grid.GridDataset;
import ucar.nc2.ft.FeatureDataset;
import ucar.nc2.ft.FeatureDatasetFactoryManager;

public class GridCellVisitorTest {

    private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GridCellVisitorTest.class);

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        log.debug("Started testing class.");
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        log.debug("Ended testing class.");
    }

    @Test
    public void testTZYXVisitor() throws IOException {
        String datasetUrl = getResourceDir() + File.separator + "testSimpleTZYXGrid.ncml";
        FeatureDataset fd = FeatureDatasetFactoryManager.open(null, datasetUrl, null, new Formatter(System.err));
        GridDataset dataset = (GridDataset) fd;
        GridDatatype gdt = dataset.findGridDatatype(GridTypeTest.DATATYPE_RH);
        GridCellTraverser gct = new GridCellTraverser(gdt);
        GridCellVisitorInheritMock gcvim = new GridCellVisitorInheritMock();
        gct.traverse(gcvim);
        int cells = X_SIZE * Y_SIZE * T_SIZE * Z_SIZE;
        assertEquals("Should count all the cells", gcvim.getProcessCount(), cells);

    }
}
