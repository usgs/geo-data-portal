package gov.usgs.cida.gdp.coreprocessing.analysis.grid;

import java.io.IOException;
import java.util.Formatter;
import java.util.List;
import static org.junit.Assert.*;
import org.junit.Test;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.ProjectedCRS;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.dt.GridDataset;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.ft.FeatureDataset;
import ucar.nc2.ft.FeatureDatasetFactoryManager;

/**
 *
 * @author tkunicki
 */
public class CRSUtilityTest {
    

    @Test
    public void testCRSSleuth2010() throws IOException {
        FeatureDataset fd = FeatureDatasetFactoryManager.open(
                FeatureType.ANY,
                "src/test/resources/crs/sleuth.2010.ncml",
                null, 
                new Formatter(System.err));
        if (fd != null && fd instanceof GridDataset) {
            GridDataset gd = (GridDataset)fd;
            List<GridDatatype> gdts = gd.getGrids();
            assertEquals(1, gdts.size());
            CoordinateReferenceSystem crs = CRSUtility.getCRSFromGridCoordSystem(gdts.get(0).getCoordinateSystem());
            
            // verify prior issue resolution.
            assertTrue(crs instanceof ProjectedCRS);
            ProjectedCRS pcrs = (ProjectedCRS)crs;
            assertEquals(6378137., pcrs.getDatum().getEllipsoid().getSemiMajorAxis(), 0);
            assertEquals(298.257222101, pcrs.getDatum().getEllipsoid().getInverseFlattening(), 1e-7);
            
        } else {
            fail("Unable to open test dataset.");
        }
    }
   
}
