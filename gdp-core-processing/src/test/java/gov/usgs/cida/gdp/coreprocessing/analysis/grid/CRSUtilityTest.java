/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usgs.cida.gdp.coreprocessing.analysis.grid;

import java.io.IOException;
import java.util.Formatter;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDataset;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.ft.FeatureDataset;
import ucar.nc2.ft.FeatureDatasetFactoryManager;

/**
 *
 * @author tkunicki
 */
public class CRSUtilityTest {
    
    public CRSUtilityTest() {
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

    /**
     * 
     */
    @Ignore
    @Test
    public void testGetCRSFromGridDatatype() throws IOException {
        FeatureDataset fd = FeatureDatasetFactoryManager.open(
                FeatureType.ANY,
                "dods://igsarm-cida-javatest2.er.usgs.gov:8080/thredds/dodsC/testdata/molokai_radar.ncml",
                null, new Formatter(System.err));
        if (fd != null && fd instanceof GridDataset) {
            GridDataset gd = (GridDataset)fd;
            List<GridDatatype> gdts = gd.getGrids();
            for (GridDatatype gdt : gdts) {
                System.out.println (CRSUtility.getCRSFromGridDatatype(gdt));
            }
        }
    }
}
