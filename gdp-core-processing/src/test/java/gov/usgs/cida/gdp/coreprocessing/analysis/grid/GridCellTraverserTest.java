package gov.usgs.cida.gdp.coreprocessing.analysis.grid;

import static gov.usgs.cida.gdp.coreprocessing.GridCellHelper.*;
import java.io.File;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;



import java.io.IOException;
import java.util.Formatter;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import org.junit.Test;
import ucar.ma2.Array;
import ucar.ma2.IndexIterator;
import ucar.nc2.constants.FeatureType;

import ucar.nc2.dt.GridDatatype;
import ucar.nc2.dt.grid.GridDataset;
import ucar.nc2.ft.FeatureDataset;
import ucar.nc2.ft.FeatureDatasetFactoryManager;

public class GridCellTraverserTest {

    private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GridCellTraverserTest.class);

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        log.debug("Started testing class.");
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        log.debug("Ended testing class.");
    }

    @Test
    public void testYXTraversal() throws IOException {
        String datasetUrl = getResourceDir() + File.separator + "testSimpleYXGrid.ncml";
        GridCellTraverserHelper gct = getGridCellTraverserHelper(datasetUrl);
        assertEquals("X cell count should match ncml", gct.getXCellCount(), X_SIZE);
        assertEquals("Y cell count should match ncml", gct.getYCellCount(), Y_SIZE);
        assertEquals("Z cell count should match ncml", gct.getZCellCount(), 0);
        assertEquals("T cell count should match ncml", gct.getTCellCount(), 0);
    }

    @Test
    public void testZYXTraversal() throws IOException {
        String datasetUrl = getResourceDir() + File.separator + "testSimpleZYXGrid.ncml";
        GridCellTraverserHelper gct = getGridCellTraverserHelper(datasetUrl);
        assertEquals("X cell count should match ncml", gct.getXCellCount(), X_SIZE);
        assertEquals("Y cell count should match ncml", gct.getYCellCount(), Y_SIZE);
        assertEquals("Z cell count should match ncml", gct.getZCellCount(), Z_SIZE);
        assertEquals("T cell count should match ncml", gct.getTCellCount(), 0);
    }

    @Test
    public void testTYXTraversal() throws IOException {
        String datasetUrl = getResourceDir() + File.separator + "testSimpleTYXGrid.ncml";
        GridCellTraverserHelper gct = getGridCellTraverserHelper(datasetUrl);
        assertEquals("X cell count should match ncml", gct.getXCellCount(), X_SIZE);
        assertEquals("Y cell count should match ncml", gct.getYCellCount(), Y_SIZE);
        assertEquals("Z cell count should match ncml", gct.getZCellCount(), 0);
        assertEquals("T cell count should match ncml", gct.getTCellCount(), T_SIZE);
    }

    @Test
    public void testTZYXTraversal() throws IOException {
        String datasetUrl = getResourceDir() + File.separator + "testSimpleTZYXGrid.ncml";
        GridCellTraverserHelper gct = getGridCellTraverserHelper(datasetUrl);
        assertEquals("X cell count should match ncml", gct.getXCellCount(), X_SIZE);
        assertEquals("Y cell count should match ncml", gct.getYCellCount(), Y_SIZE);
        assertEquals("Z cell count should match ncml", gct.getZCellCount(), Z_SIZE);
        assertEquals("T cell count should match ncml", gct.getTCellCount(), T_SIZE);
    }

    private GridCellTraverserHelper getGridCellTraverserHelper(String datasetUrl) throws IOException {
        FeatureDataset fd = FeatureDatasetFactoryManager.open(null, datasetUrl, null, new Formatter(System.err));
        GridDataset dataset = (GridDataset) fd;
        GridDatatype gdt = dataset.findGridDatatype(GridTypeTest.DATATYPE_RH);
        return new GridCellTraverserHelper(gdt);
    }
    
        @Test
    public void testMissingValues() throws IOException {
        GridDataset gds = null;
        try {
            GridDatatype gdt;
            
            gds = getGridDataset("src/test/resources/Sample_files/testMissingValuesYXGrid.ncml");
            
            gdt = gds.findGridByName("missingValueWithInteger");
            validateCDMWithMissingData(gdt, 1, 7, 5);
            validateTraverseMissingData(gdt, 7, 5);
            
            gdt = gds.findGridByName("missingValueWithFloat");
            validateCDMWithMissingData(gdt, 1, 7, 5);
            validateTraverseMissingData(gdt, 7, 5);
            
            gdt = gds.findGridByName("missingValueSetButNoMissingValues");
            validateCDMWithMissingData(gdt, -1, 0, 12);
            validateTraverseMissingData(gdt, 0, 12);
            
            gdt = gds.findGridByName("missingValueNotSet");
            // NOTE: NaN passed to signal grid doesn't have missing data value
            validateCDMWithMissingData(gdt, Float.NaN, 0, 12);
            validateTraverseMissingData(gdt, 0, 12);
            
        } finally {
            if (gds != null) {
                gds.close();
            }
        }
    }
    
    private void validateCDMWithMissingData(GridDatatype gdt, float missingValue, int expectedMissing, int expectedValid) throws IOException {
        
        // NOTE: NaN passed to signal grid doesn't have missing data value
        if(Float.isNaN(missingValue)) {
            assertFalse("Grid reports missing data", gdt.hasMissingData());
        } else {
            assertTrue("Grid reports missing data", gdt.hasMissingData());
            assertTrue("Grid reports correct MissingValue", gdt.isMissingData(missingValue));
        }
        
        float[] f = new float[] { missingValue } ;
        f = gdt.setMissingToNaN(f);
        assertTrue("Grid sets MissingValue to NaN", Float.isNaN(f[0]));
        int observedValid = 0;
        int observedMissing = 0;
        Array a = gdt.readVolumeData(0);
        IndexIterator ii = a.getIndexIterator();
        while (ii.hasNext()) {
            double d = ii.getDoubleNext();
            if (gdt.isMissingData(d)) {
                observedMissing++;
            }else {
                observedValid++;
            }
        }
        assertEquals("Grid reports correct missing data", expectedMissing, observedMissing);
        assertEquals("Grid reports correct valid data", expectedValid, observedValid);
    }
    
    private void validateTraverseMissingData(GridDatatype gdt,  int expectedMissing, int expectedValid) throws IOException {
        MissingValueTestVisitor visitor = new MissingValueTestVisitor();
        GridCellTraverser traverser = new GridCellTraverser(gdt);
        traverser.traverse(visitor);
        assertEquals("Grid reports correct missing data", expectedMissing, visitor.observedMissing);
        assertEquals("Grid reports correct valid data", expectedValid, visitor.observedValid);
    }
    
    public GridDataset getGridDataset(String path) throws IOException {
        FeatureDataset fds = FeatureDatasetFactoryManager.open(FeatureType.GRID, path, null, new Formatter(System.err));
        return fds instanceof GridDataset ? (GridDataset)fds : null;
    }
    
    private class MissingValueTestVisitor extends GridCellVisitor {
        int observedMissing = 0;
        int observedValid = 0;
        @Override public void processGridCell(int xCellIndex, int yCellIndex, double value) {
            if (value == value) {
                observedValid++;
            } else {
                observedMissing++;
            }
        }
        
    }
}
