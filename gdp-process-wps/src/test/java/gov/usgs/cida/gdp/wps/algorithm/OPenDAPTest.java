package gov.usgs.cida.gdp.wps.algorithm;

import gov.usgs.cida.gdp.coreprocessing.analysis.grid.GridUtility;
import java.util.Formatter;
import org.junit.Ignore;
import org.junit.Test;
import ucar.ma2.Array;
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
public class OPenDAPTest {

    public OPenDAPTest() {
    }

    @Test
    @Ignore
    public void testConnectionReuse() throws Exception {
        FeatureDataset fd = null;
        try {
            fd = FeatureDatasetFactoryManager.open(
                    FeatureType.ANY,
                    "dods://cida.usgs.gov/thredds/dodsC/qpe/ancrfcqpe_w_meta.ncml",
                    null,
                    new Formatter(System.err));
            if (fd instanceof GridDataset) {
                GridDataset gds = (GridDataset) fd;
                GridDatatype gdt = gds.findGridDatatype("P01M_NONE");
                if (gdt != null) {
                    GridCoordSystem gcs = gdt.getCoordinateSystem();
                    int tCount = gcs.getTimeAxis1D().getShape(0);
                    int xIndex = GridUtility.getXAxisLength(gcs) / 2;
                    int yIndex = GridUtility.getYAxisLength(gcs) / 2;
                    for (int tIndex = 0; tIndex < tCount; ++tIndex) {
                        Array a = gdt.readDataSlice(tIndex, -1, yIndex, xIndex);
                        while (a.hasNext()) {
                            System.out.println(tIndex + ":" + yIndex + ":" + xIndex + " -> " + a.nextDouble());
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }finally {
            if (fd != null) {
                fd.close();
            }
        }
    }
}
