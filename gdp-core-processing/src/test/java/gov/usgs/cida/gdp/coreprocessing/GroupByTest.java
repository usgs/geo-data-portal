package gov.usgs.cida.gdp.coreprocessing;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author isuftin
 */
public class GroupByTest {

    private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GroupByTest.class);

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        log.debug("Started testing class.");
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        log.debug("Ended testing class.");
    }

    @Test
    public void testCreateGroupBy() {
        GroupBy test = new GroupBy();
        assertEquals(GroupBy.class, test.getClass());
    }

    @Test
    public void testStationOption() {
        assertEquals(GroupBy.StationOption.station.toString(), "Station");
        assertEquals(GroupBy.StationOption.variable.toString(), "Variable");
        assertEquals(GroupBy.StationOption.getDefault().getClass(), GroupBy.StationOption.station.getClass());
    }

    @Test
    public void testGridOption() {
        assertEquals(GroupBy.GridOption.attributes.toString(), "Attributes");
        assertEquals(GroupBy.GridOption.statistics.toString(), "Statistics");
        assertEquals(GroupBy.GridOption.getDefault().getClass(), GroupBy.GridOption.attributes.getClass());
    }
}
