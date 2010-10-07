package gov.usgs.cida.gdp.coreprocessing.analysis.grid;

import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * @author jordan
 * I realize that this class is pretty much just to test the default Visitor
 * functions, and that they don't really do anything currently.
 * So this isn't very useful, but I'm going to leave it in place anyway.
 */
public class GridCellVisitorInheritMock extends GridCellVisitor {

    private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GridCellVisitorInheritMock.class);

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        log.debug("Started testing class.");
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        log.debug("Ended testing class.");
    }
    private int processCount = 0;

    @Override
    public void processGridCell(int xCellIndex, int yCellIndex, double value) {
        processCount++;
    }

    public int getProcessCount() {
        return processCount;
    }
}
