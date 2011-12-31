/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usgs.derivative.time;

import gov.usgs.derivative.time.IntervalTimeStepDescriptor;
import java.util.Arrays;
import java.util.List;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tkunicki
 */
public class IntervalTimeStepDescriptorTest {

    public IntervalTimeStepDescriptorTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testOutputIntervalsAreSorted() {

        // intervals correctly sorted with no gaps
        validateIntervalsAreSorted(
                Arrays.asList(new Interval[]{
                    new Interval("1950-01-01/P30Y"),
                    new Interval("1980-01-01/P30Y"),
                    new Interval("2010-01-01/P30Y"),
                }));
        
        // intervals correctly sorted with gaps
        validateIntervalsAreSorted(
                Arrays.asList(new Interval[]{
                    new Interval("1950-01-01/P30Y"),
                    new Interval("1990-01-01/P30Y"),
                    new Interval("2030-01-01/P30Y"),
                }));

        // intervals reversed with no gaps
        validateIntervalsAreSorted(
                Arrays.asList(new Interval[]{
                    new Interval("2010-01-01/P30Y"),
                    new Interval("1980-01-01/P30Y"),
                    new Interval("1950-01-01/P30Y"),
                }));
        
        // intervals reversed with gaps
        validateIntervalsAreSorted(
                Arrays.asList(new Interval[]{
                    new Interval("2030-01-01/P30Y"),
                    new Interval("1990-01-01/P30Y"),
                    new Interval("1950-01-01/P30Y"),
                }));

    }

    private void validateIntervalsAreSorted(List<Interval> outputIntervals) {

        IntervalTimeStepDescriptor itsd = new IntervalTimeStepDescriptor(
                findIntervalOfIntervals(outputIntervals),
                outputIntervals);
        
        assertTrue(intervalsSorted(itsd.getOutputIntervals()));
    }

    @Test
    public void testExceptionOnOverlappingOutputIntervals() {
        
        // intervals correctly sorted with no gaps
        validateExceptionOnOverlappingOutputIntervals(
                Arrays.asList(new Interval[] {
                    new Interval("1950-01-01/P30Y"),
                    new Interval("1980-01-01/P30Y"),
                    new Interval("2010-01-01/P30Y"),
                }),
                false);
        
        // intervals correctly sorted with gaps
        validateExceptionOnOverlappingOutputIntervals(
                Arrays.asList(new Interval[] {
                    new Interval("1950-01-01/P40Y"),
                    new Interval("1980-01-01/P40Y"),
                    new Interval("2010-01-01/P40Y"),
                }),
                true);
    }
        
    private void validateExceptionOnOverlappingOutputIntervals(List<Interval> outputIntervals, boolean expectedException) {
        
        boolean actualException = false;
        try {
            IntervalTimeStepDescriptor itsd = new IntervalTimeStepDescriptor(
                    findIntervalOfIntervals(outputIntervals),
                    outputIntervals);
            actualException = false;
        } catch (IllegalArgumentException e) {
            actualException = true;
        }

        assertEquals(expectedException, actualException);
    }
    
        
    @Test
    public void testGetOutputTimeStepCount() {
        validateGetOutputTimeStepCount(
                Arrays.asList(new Interval[]{
                    new Interval("1950-01-01/P30Y"),
                }));
        validateGetOutputTimeStepCount(
                Arrays.asList(new Interval[]{
                    new Interval("1950-01-01/P30Y"),
                    new Interval("1980-01-01/P30Y"),
                }));
        validateGetOutputTimeStepCount(
                Arrays.asList(new Interval[]{
                    new Interval("1950-01-01/P30Y"),
                    new Interval("1980-01-01/P30Y"),
                    new Interval("2010-01-01/P30Y"),
                }));
    }
    
    private void validateGetOutputTimeStepCount(List<Interval> outputIntervals) {
        
        IntervalTimeStepDescriptor itsd = new IntervalTimeStepDescriptor(
                findIntervalOfIntervals(outputIntervals),
                outputIntervals);
        
        assertEquals(outputIntervals.size(), itsd.getOutputTimeStepCount());
    }
    
    @Test
    public void testOutputIntervalAndTimeStepBounds() {
        validateOutputIntervalAndTimeStepBounds(
                Arrays.asList(new Interval[]{
                    new Interval("1950-01-01/P30Y"),
                }));
        validateOutputIntervalAndTimeStepBounds(
                Arrays.asList(new Interval[]{
                    new Interval("1950-01-01/P30Y"),
                    new Interval("1980-01-01/P30Y"),
                }));
        validateOutputIntervalAndTimeStepBounds(
                Arrays.asList(new Interval[]{
                    new Interval("1950-01-01/P30Y"),
                    new Interval("1980-01-01/P30Y"),
                    new Interval("2010-01-01/P30Y"),
                }));
        validateOutputIntervalAndTimeStepBounds(
                Arrays.asList(new Interval[]{
                    new Interval("1950-01-01/P30Y"),
                    new Interval("1990-01-01/P30Y"),
                    new Interval("2030-01-01/P30Y"),
                }));
    }
    
    private void validateOutputIntervalAndTimeStepBounds(List<Interval> outputIntervals) {
        
        // check incoming test data, a failure here is in test, not tested code.
        assertTrue(intervalsSorted(outputIntervals));
        
        IntervalTimeStepDescriptor itsd = new IntervalTimeStepDescriptor(
                findIntervalOfIntervals(outputIntervals),
                outputIntervals);
        
        assertEquals(outputIntervals.size(), itsd.getOutputTimeStepCount());

        int timeStepCount = itsd.getOutputTimeStepCount();
        for (int timeStepIndex = 0; timeStepIndex < timeStepCount; ++timeStepIndex) {
            assertEquals(outputIntervals.get(timeStepIndex), itsd.getOutputTimeStepInterval(timeStepIndex));
            assertEquals(outputIntervals.get(timeStepIndex).getStart(), itsd.getOutputTimeStepLowerBound(timeStepIndex));
            assertEquals(outputIntervals.get(timeStepIndex).getEnd(), itsd.getOutputTimeStepUpperBound(timeStepIndex));
            
        }
    }
    
    private Interval findIntervalOfIntervals(List<Interval> intervals) {
        DateTime start = intervals.get(0).getStart();
        DateTime end = intervals.get(0).getEnd();
        for (int intervalIndex = 1; intervalIndex < intervals.size(); ++intervalIndex) {
            Interval interval = intervals.get(intervalIndex);
            if (interval.getStart().isBefore(start)) {
                start = interval.getStart();
            }
            if (interval.getEnd().isAfter(end)) {
                end = interval.getEnd();
            }
        }
        return new Interval(start, end);
    }
    
    private boolean intervalsSorted(List<Interval> intervals) {
        int timeStepCount = intervals.size();
        for (int timeStepIndex = 1; timeStepIndex < timeStepCount; ++timeStepIndex) {
            DateTime previousEnd = intervals.get(timeStepIndex - 1).getEnd();
            DateTime nextStart = intervals.get(timeStepIndex).getStart();
            if (!(previousEnd.equals(nextStart) || previousEnd.isBefore(nextStart))) {
                return false;
            }
        }
        return true;
    }
}
