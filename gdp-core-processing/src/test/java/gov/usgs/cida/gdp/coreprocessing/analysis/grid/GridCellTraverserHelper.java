package gov.usgs.cida.gdp.coreprocessing.analysis.grid;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import ucar.nc2.dt.GridDatatype;

public class GridCellTraverserHelper extends GridCellTraverser {

    private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GridCellTraverserHelper.class);

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        log.debug("Started testing class.");
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        log.debug("Ended testing class.");
    }

    public GridCellTraverserHelper(GridDatatype gridDatatype) {
        super(gridDatatype);
    }

    public int getXCellCount() {
        return this.xCellCount;
    }

    public int getYCellCount() {
        return this.yCellCount;
    }

    public int getZCellCount() {
        return this.zCellCount;
    }

    public int getTCellCount() {
        return this.tCellCount;
    }
}
