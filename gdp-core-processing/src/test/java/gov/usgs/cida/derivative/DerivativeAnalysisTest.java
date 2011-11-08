/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usgs.cida.derivative;

import gov.usgs.cida.derivative.DaysAboveTemperatureThresholdVisitor;
import gov.usgs.cida.derivative.DaysBelowTemperatureThresholdVisitor;
import gov.usgs.cida.gdp.coreprocessing.analysis.grid.GridCellTraverser;
import gov.usgs.cida.gdp.coreprocessing.analysis.grid.GridCellVisitor;
import java.io.IOException;
import java.util.Formatter;
import java.util.List;
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
                for (GridDatatype gdt : gdtl) {
                    if (gdt.getName().endsWith("tmax")) {
                        System.out.println("running " + gdt.getName());
                        GridCellTraverser t = new GridCellTraverser(gdt);
                        GridCellVisitor v = new DaysAboveTemperatureThresholdVisitor();
                        t.traverse(v);
                    } else if (gdt.getName().endsWith("tmin")) {
                        System.out.println("running " + gdt.getName());
                        GridCellTraverser t = new GridCellTraverser(gdt);
                        GridCellVisitor v = new DaysBelowTemperatureThresholdVisitor();
                        t.traverse(v);
                    }
                }
            }
           
        } finally {
            if (fds != null) fds.close();
        }
    }
}
