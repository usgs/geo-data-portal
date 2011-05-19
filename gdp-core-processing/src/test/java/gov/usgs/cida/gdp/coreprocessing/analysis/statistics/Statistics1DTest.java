package gov.usgs.cida.gdp.coreprocessing.analysis.statistics;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tkunicki
 */
public class Statistics1DTest {
    
    public Statistics1DTest() {
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
    public void testSampleCountOfZero() {
        Statistics1D statistics = new Statistics1D();
        assertEquals(0, statistics.getCount());
        assertTrue(Double.isNaN(statistics.getMean()));
        assertTrue(Double.isNaN(statistics.getMinimum()));
        assertTrue(Double.isNaN(statistics.getMaximum()));
        assertTrue(Double.isNaN(statistics.getSampleVariance()));
        assertTrue(Double.isNaN(statistics.getSampleStandardDeviation()));
        assertTrue(Double.isNaN(statistics.getPopulationVariance()));
        assertTrue(Double.isNaN(statistics.getPopulationStandardDeviation()));
    }
    
    @Test
    public void testSampleCountOfOne() {
        validateSampleCountOfOne(0);
        validateSampleCountOfOne(1);
        validateSampleCountOfOne(-1);
        validateSampleCountOfOne(Integer.MAX_VALUE);
        validateSampleCountOfOne(Integer.MIN_VALUE);
        validateSampleCountOfOne(Double.MAX_VALUE);
        validateSampleCountOfOne(Double.MIN_VALUE);
    }
    
    @Test
    public void testMultipleSampleCountOfSameValue() {
        validateMultipleSampleCountOfSameValue(0, 2);
        validateMultipleSampleCountOfSameValue(1, 2);
        validateMultipleSampleCountOfSameValue(-1, 2);
        validateMultipleSampleCountOfSameValue(Integer.MAX_VALUE, 2);
        validateMultipleSampleCountOfSameValue(Integer.MIN_VALUE, 2);
        validateMultipleSampleCountOfSameValue(Double.MAX_VALUE, 2);
        validateMultipleSampleCountOfSameValue(Double.MIN_VALUE, 2);
        
        
        validateMultipleSampleCountOfSameValue(0, 4);
        validateMultipleSampleCountOfSameValue(1, 4);
        validateMultipleSampleCountOfSameValue(-1, 4);
        validateMultipleSampleCountOfSameValue(Integer.MAX_VALUE, 4);
        validateMultipleSampleCountOfSameValue(Integer.MIN_VALUE, 4);
        validateMultipleSampleCountOfSameValue(Double.MAX_VALUE, 4);
        validateMultipleSampleCountOfSameValue(Double.MIN_VALUE, 4);
    }
    
    private void validateSampleCountOfOne(double sample) {
        Statistics1D statistics = new Statistics1D();
        statistics.accumulate(sample);
        assertEquals(1, statistics.getCount());
        assertEquals(sample, statistics.getMean(), 0d);
        assertEquals(sample, statistics.getMinimum(), 0d);
        assertEquals(sample, statistics.getMaximum(), 0d);
        assertTrue(Double.isNaN(statistics.getSampleVariance()));
        assertTrue(Double.isNaN(statistics.getSampleStandardDeviation()));
        assertEquals(0d, statistics.getPopulationVariance(), 0d);
        assertEquals(0d, statistics.getPopulationStandardDeviation(), 0d);
    }
    
    private void validateMultipleSampleCountOfSameValue(double sample, int sampleCount) {
        Statistics1D statistics = new Statistics1D();
        for (int sampleIndex = 0; sampleIndex < sampleCount; ++sampleIndex) {
            statistics.accumulate(sample);
        }
        assertEquals(sampleCount, statistics.getCount());
        assertEquals(sample, statistics.getMean(), 0d);
        assertEquals(sample, statistics.getMinimum(), 0d);
        assertEquals(sample, statistics.getMaximum(), 0d);
        assertEquals(0d, statistics.getSampleVariance(), 0d);
        assertEquals(0d, statistics.getSampleStandardDeviation(), 0d);
        assertEquals(0d, statistics.getPopulationVariance(), 0d);
        assertEquals(0d, statistics.getPopulationStandardDeviation(), 0d);
    }
}
