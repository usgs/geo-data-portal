/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usgs.cida.derivative;

import gov.usgs.cida.derivative.TimeStepAveragingVisitor;
import gov.usgs.cida.gdp.coreprocessing.analysis.grid.GridCellTraverser;
import gov.usgs.cida.gdp.coreprocessing.analysis.grid.GridCellVisitor;
import java.io.IOException;
import java.util.Formatter;
import java.util.List;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.dt.GridDataset;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.ft.FeatureDataset;
import ucar.nc2.ft.FeatureDatasetFactoryManager;

/**
 *
 * @author tkunicki
 */
public class TimeStepAveragingVisitorTest {
    
    public TimeStepAveragingVisitorTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    @Ignore
    public void testSomeMethod() throws IOException {
        FeatureDataset fds = null;
//        try {
//            fds = FeatureDatasetFactoryManager.open(
//                FeatureType.GRID,
//                "/Users/tkunicki/Downloads/derivatives/derivative-days_above_threshold.ncml",
//                null,
//                new Formatter(System.err));
//            if (fds instanceof GridDataset) {
//                GridDataset gds = (GridDataset)fds;
//                List<GridDatatype> gdtl = gds.getGrids();
//                for (GridDatatype gdt : gdtl) {
//                    System.out.println("running " + gdt.getName());
//                    GridCellTraverser t = new GridCellTraverser(gdt);
//                    GridCellVisitor v = new TimeStepAveragingVisitor();
//                    t.traverse(v);
//                }
//            }
//           
//        } finally {
//            if (fds != null) fds.close();
//        }
        try {
            fds = FeatureDatasetFactoryManager.open(
                FeatureType.GRID,
                "/Users/tkunicki/Downloads/derivatives/derivative-days_below_threshold.ncml",
                null,
                new Formatter(System.err));
            if (fds instanceof GridDataset) {
                GridDataset gds = (GridDataset)fds;
                List<GridDatatype> gdtl = gds.getGrids();
                for (GridDatatype gdt : gdtl) {
                    System.out.println("running " + gdt.getName());
                    GridCellTraverser t = new GridCellTraverser(gdt);
                    GridCellVisitor v = new TimeStepAveragingVisitor();
                    t.traverse(v);
                }
            }
           
        } finally {
            if (fds != null) fds.close();
        }
    }
}
