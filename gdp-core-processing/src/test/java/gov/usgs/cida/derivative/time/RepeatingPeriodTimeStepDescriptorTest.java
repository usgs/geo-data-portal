package gov.usgs.cida.derivative.time;

import gov.usgs.cida.derivative.time.RepeatingPeriodTimeStepDescriptor;
import org.joda.time.Days;
import org.joda.time.MutableDateTime;
import org.joda.time.ReadablePeriod;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.DateTimeZone;
import org.joda.time.Months;
import org.joda.time.Period;
import org.joda.time.Years;
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
public class RepeatingPeriodTimeStepDescriptorTest {
    
    public final static long MILLIS_PER_DAY = 24 * 60 * 60 * 1000;
    
    public RepeatingPeriodTimeStepDescriptorTest() {
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
    public void testGetOutputTimeStepCount() {
        
        RepeatingPeriodTimeStepDescriptor dtsd = null;
        
        Interval interval0 = new Interval(
                new DateTime("1950-01-01T00:00:00Z", DateTimeZone.UTC),
                new DateTime("1959-12-31T23:59:59Z", DateTimeZone.UTC));
        dtsd = new RepeatingPeriodTimeStepDescriptor(
                interval0,
                Months.ONE);
        assertEquals("Test P1M, input interval upper bound 1S before new period",
                120, dtsd.getOutputTimeStepCount());
        
        dtsd = new RepeatingPeriodTimeStepDescriptor(
                interval0,
                Years.ONE);
        assertEquals("Test P1Y, input interval upper bound 1S before new period",
                10, dtsd.getOutputTimeStepCount());
        
        Interval interval1 = new Interval(
                new DateTime("1950-01-01T00:00:00Z", DateTimeZone.UTC),
                new DateTime("1960-01-01T00:00:00Z", DateTimeZone.UTC));
        dtsd = new RepeatingPeriodTimeStepDescriptor(
                interval1,
                Months.ONE);
        assertEquals("Test P1M, input interval upper matches beginning of new period but exclusive",
                120, dtsd.getOutputTimeStepCount());
                dtsd = new RepeatingPeriodTimeStepDescriptor(
                interval1,
                Years.ONE);
        assertEquals("Test P1Y, input interval upper matches beginning of new period but exclusive",
                10, dtsd.getOutputTimeStepCount());
        
        Interval interval2 = new Interval(
                new DateTime("1950-01-01T00:00:00Z", DateTimeZone.UTC),
                new DateTime("1960-01-01T00:00:01Z", DateTimeZone.UTC));
        dtsd = new RepeatingPeriodTimeStepDescriptor(
                interval2,
                Months.ONE);
        assertEquals("Test P1M, input interval upper bound 1S after new period",
                121, dtsd.getOutputTimeStepCount());
                dtsd = new RepeatingPeriodTimeStepDescriptor(
                interval2,
                Years.ONE);
        assertEquals("Test P1Y, input interval upper bound 1S after new period",
                11, dtsd.getOutputTimeStepCount());
        
    }
    
    @Test
    public void testTimeStepIndex() {
            
        validatePeriodsByIndex(
                new Interval(
                    new DateTime("1950-01-01", DateTimeZone.UTC),
                    Years.years(10)),
                Days.ONE);
        validatePeriodsByIndex(
                new Interval(
                    new DateTime("1950-01-01", DateTimeZone.UTC),
                    Years.years(10)),
                Period.parse("P1D"));
        validatePeriodsByIndex(
                new Interval(
                    new DateTime("1950-01-01", DateTimeZone.UTC),
                    Years.years(10)),
                Days.TWO);
        validatePeriodsByIndex(
                new Interval(
                    new DateTime("1950-01-01", DateTimeZone.UTC),
                    Years.years(10)),
                Period.parse("P2D"));
        
        validatePeriodsByIndex(
                new Interval(
                    new DateTime("1950-01-01", DateTimeZone.UTC),
                    Years.years(10)),
                Months.ONE);
        validatePeriodsByIndex(
                new Interval(
                    new DateTime("1950-01-01", DateTimeZone.UTC),
                    Years.years(10)),
                Months.TWO);
        validatePeriodsByIndex(
                new Interval(
                    new DateTime("1950-01-01", DateTimeZone.UTC),
                    Years.years(10)),
                Years.ONE);
        validatePeriodsByIndex(
                new Interval(
                    new DateTime("1950-01-01", DateTimeZone.UTC),
                    Years.years(10)),
                Years.TWO);
        
        validatePeriodsByIndex(
                new Interval(
                    new DateTime("1950-01-01", DateTimeZone.UTC),
                    Years.years(10)),
                Period.parse("P1DT12H"));
        validatePeriodsByIndex(
                new Interval(
                    new DateTime("1950-01-01", DateTimeZone.UTC),
                    Years.years(10)),
                Period.parse("P45D"));
    }
    
    private void validatePeriodsByIndex(Interval interval, ReadablePeriod repeatingPeriod) {

        RepeatingPeriodTimeStepDescriptor dtsd = new RepeatingPeriodTimeStepDescriptor(
                interval,
                repeatingPeriod);
        
        assertEquals(interval, dtsd.getInputInterval());
        assertEquals(repeatingPeriod, dtsd.getRepeatingPeriod());
        assertTrue(dtsd.getOutputInterval().overlaps(dtsd.getInputInterval()));
        
        DateTime startDateTime = dtsd.getOutputInterval().getStart();
        long startMillis = startDateTime.getMillis();
            
        // increment over period bounds
        int expectedTimeStepIndex = 0;
        DateTime expectedDateTimeLowerBound = startDateTime;
        while (interval.contains(expectedDateTimeLowerBound)) {
            
            // pull out index from descriptor...
            int actualTimeStepIndex = dtsd.getOutputTimeStepIndex(expectedDateTimeLowerBound);
            assertEquals(expectedTimeStepIndex, actualTimeStepIndex);
            
            // regenerate a expected lower bound from index using the returned value.
            MutableDateTime expectedDataTimeLowerBoundFromIndex = interval.getStart().toMutableDateTime();
            expectedDataTimeLowerBoundFromIndex.add(repeatingPeriod, expectedTimeStepIndex);
            
            Interval expectedTimeStepInterval = new Interval(
                    expectedDataTimeLowerBoundFromIndex.toDateTime(),
                    repeatingPeriod);
            
            // verify the index is correct by 
            assertEquals(expectedDateTimeLowerBound, expectedTimeStepInterval.getStart());
            
            // test bounds of period at index
            assertEquals(expectedTimeStepInterval.getStart(), dtsd.getOutputTimeStepLowerBound(expectedTimeStepIndex));
            assertEquals(expectedTimeStepInterval.getEnd(), dtsd.getOutputTimeStepUpperBound(expectedTimeStepIndex));
            assertEquals(expectedTimeStepInterval, dtsd.getOutputTimeStepInterval(expectedTimeStepIndex));
            
            // NOTE!  assumes API used behind tested class doesn't respect leap values below day resolution
            long expectedDaysFromLowerBound = (expectedTimeStepInterval.getStartMillis() - startMillis) / MILLIS_PER_DAY;
            long expectedDaysFromUpperBound = (expectedTimeStepInterval.getEndMillis() - startMillis) / MILLIS_PER_DAY;
            
            assertEquals(expectedDaysFromLowerBound, dtsd.getDaysFromTimeStepLowerBound(expectedTimeStepIndex));
            assertEquals(expectedDaysFromUpperBound, dtsd.getDaysFromTimeStepUpperBound(expectedTimeStepIndex));
            
            assertEquals(expectedDaysFromLowerBound, dtsd.getDaysFromTimeStepLowerBound(expectedDateTimeLowerBound));
            // expectedDateTimeLowerBound usage here is not a typo, since upper bounds are exclusive, the datetime of the upperbound belongs to the next period
            assertEquals(expectedDaysFromUpperBound, dtsd.getDaysFromTimeStepUpperBound(expectedDateTimeLowerBound));
            
            expectedDateTimeLowerBound = expectedDateTimeLowerBound.plus(repeatingPeriod);
            ++expectedTimeStepIndex;
        }
    }
    
}
