/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usgs.cida.derivative;

import gov.usgs.cida.gdp.coreprocessing.analysis.grid.GridCellTraverser;
import gov.usgs.cida.gdp.coreprocessing.analysis.grid.GridCellVisitor;
import java.io.IOException;
import java.util.Arrays;
import java.util.Formatter;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
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
public class DerivativeAnalysisTest {
    
    public DerivativeAnalysisTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    @Test
    @Ignore
    public void testSomeMethod() throws IOException {
        ExecutorService executorService = Executors.newFixedThreadPool(8);
        FeatureDataset fds = null;
        try {
            fds = FeatureDatasetFactoryManager.open(
                FeatureType.GRID,
                "/Volumes/Data/thredds/dcp/conus_grid.ncml",
                null,
                new Formatter(System.err));
            if (fds instanceof GridDataset) {
                GridDataset gds = (GridDataset)fds;
                List<GridDatatype> gdtl = gds.getGrids();
                for (final GridDatatype gdt : gdtl) {
                    executorService.submit(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                if (gdt.getName().endsWith("pr")) {
                                    System.out.println("running " + gdt.getName());
                                    GridCellTraverser t = new GridCellTraverser(gdt);
                                    GridCellVisitor v = new DaysAbovePrecipitationThresholdVisitor();
                                    t.traverse(v);
                                }
//                                if (gdt.getName().endsWith("tmax")) {
//                                    System.out.println("running " + gdt.getName());
//                                    GridCellTraverser t = new GridCellTraverser(gdt);
//                                    GridCellVisitor v = new DaysAboveTemperatureThresholdVisitor();
//                                    t.traverse(v);
//                                }
//                                if (gdt.getName().endsWith("tmin")) {
//                                    System.out.println("running " + gdt.getName());
//                                    GridCellTraverser t = new GridCellTraverser(gdt);
//                                    GridCellVisitor v = new DaysBelowTemperatureThresholdVisitor();
//                                    t.traverse(v);
//                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    System.out.println(gdt.getVariable().getName());
                }
            }
            executorService.shutdown();
            executorService.awaitTermination(1, TimeUnit.DAYS);
        } catch (Exception e) {
            System.out.println(e);
        } finally {
            if (fds != null) fds.close();
        }

    }
    
    @Test
    @Ignore
    public void testSomeMethod2() throws IOException {
        FeatureDataset fds = null;
        
        List<String> gridP1YList = Arrays.asList(
                new String[] {
                    "/Users/tkunicki/Downloads/derivatives/derivative-days_above_threshold.pr.ncml",
                    "/Users/tkunicki/Downloads/derivatives/derivative-days_above_threshold.tmax.ncml",
                    "/Users/tkunicki/Downloads/derivatives/derivative-days_below_threshold.tmin.ncml",
                });

        for (String gridP1Y : gridP1YList) {
            try {

                fds = FeatureDatasetFactoryManager.open(
                    FeatureType.GRID,
                    gridP1Y,
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
}
