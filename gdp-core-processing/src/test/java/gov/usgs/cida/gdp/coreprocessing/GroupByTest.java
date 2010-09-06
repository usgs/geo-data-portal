/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usgs.cida.gdp.coreprocessing;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author isuftin
 */
public class GroupByTest {

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
