package gov.usgs.derivative;

import gov.usgs.derivative.grid.GridTraverser;
import gov.usgs.derivative.grid.GridVisitor;
import java.io.IOException;
import java.util.Arrays;
import java.util.Formatter;
import java.util.List;
import org.joda.time.Period;
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
public class TimeStepAveragingTest {

    @Test
    public void calculateP1MAverage() throws IOException {
        FeatureDataset fds = null;

        List<String> gridFileList = Arrays.asList(
                new String[]{
                    "/Users/tkunicki/Data/thredds/misc/markstro_grid/union.ncml"
                });

        for (String gridFile : gridFileList) {
            try {

                fds = FeatureDatasetFactoryManager.open(
                        FeatureType.GRID,
                        gridFile,
                        null,
                        new Formatter(System.err));
                if (fds instanceof GridDataset) {
                    GridDataset gds = (GridDataset) fds;
                    List<GridDatatype> gdtl = gds.getGrids();
                    for (GridDatatype gdt : gdtl) {
                        if (gdt.getName().startsWith("x")) {
                            System.out.println("running " + gdt.getName());
                            GridTraverser t = new GridTraverser(gdt);
                            GridVisitor v = new RepeatingPeriodTimeStepAveragingVisitor(Period.months(1));
                            t.traverse(v);
                        }
                    }
                }

            } finally {
                if (fds != null) {
                    fds.close();
                }
            }
        }
    }
}
