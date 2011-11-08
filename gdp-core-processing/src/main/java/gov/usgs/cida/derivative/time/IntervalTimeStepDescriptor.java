package gov.usgs.cida.derivative.time;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tkunicki
 */
public class IntervalTimeStepDescriptor extends AbstractTimeStepDescriptor {

    private final static Logger LOGGER = LoggerFactory.getLogger(IntervalTimeStepDescriptor.class);

    private final Interval inputInterval;
    private final Interval outputInterval;
    private final List<Interval> outputIntervals;
    
    public IntervalTimeStepDescriptor(Interval inputInterval, List<Interval> outputIntervals) {
        
        this.inputInterval = inputInterval;
       
        List<Interval> sortedIntervals = new ArrayList<Interval>(outputIntervals);
        Collections.sort(sortedIntervals, new IntervalComparator());
        this.outputIntervals = Collections.unmodifiableList(sortedIntervals);
        
        // check overlap
        int timeStepCount = outputIntervals.size();
        for (int timeStepIndex = 1; timeStepIndex < timeStepCount; ++timeStepIndex) {
            Interval interval0 = this.outputIntervals.get(timeStepIndex - 1);
            Interval interval1 = this.outputIntervals.get(timeStepIndex);
            if (interval0.overlaps(interval1)) {
                throw new IllegalArgumentException("overlapping intervals are not supported: " + interval0.toString() + " overlaps " + interval1.toString());
            }
        }
        
        // check duration, can't suppport intervals less that P1D due to NetCDF time axis units
        // TODO: support intervals < P1D
        for (int timeStepIndex = 0; timeStepIndex < timeStepCount; ++timeStepIndex) {
            Interval interval = this.outputIntervals.get(timeStepIndex);
            if (interval.getStart().plusDays(1).isAfter(interval.getEnd())) {
                throw new IllegalArgumentException("intervals less that P1D unsupported: " + interval.toString());
            }
        }

        outputInterval = new Interval(
                this.outputIntervals.get(0).getStart(),
                this.outputIntervals.get(timeStepCount - 1).getEnd());
        
        LOGGER.debug("Start time step is {}", inputInterval.getStart().toString());
        LOGGER.debug("End time step is {}", inputInterval.getEnd().toString());


    }

    @Override
    public Interval getInputInterval() {
        return inputInterval;
    }
    
    public List<Interval> getOutputIntervals() {
        return outputIntervals;
    }
    
    @Override
    public Interval getOutputInterval() {
        return outputInterval;
    }
    
    @Override
    public DateTime getOutputTimeStepLowerBound(int timeStepIndex) {
        return outputIntervals.get(timeStepIndex).getStart();
    }

    @Override
    public DateTime getOutputTimeStepUpperBound(int timeStepIndex) {
        return outputIntervals.get(timeStepIndex).getEnd();
    }

    @Override
    public Interval getOutputTimeStepInterval(int timeStepIndex) {
        return outputIntervals.get(timeStepIndex);
    }
    
    @Override
    public int getOutputTimeStepCount() {
        return outputIntervals.size();
    }

    @Override
    public int getOutputTimeStepIndex(DateTime timeStepDateTime) {
        int timeStepCount = getOutputTimeStepCount();
        for (int timeStepIndex = 0; timeStepIndex < timeStepCount; ++timeStepIndex) {
            if (getOutputTimeStepInterval(timeStepIndex).contains(timeStepDateTime)) {
                return timeStepIndex;
            }
        }
        return -1;
    }
    
    private class IntervalComparator implements Comparator<Interval> {
        @Override
        public int compare(Interval interval0, Interval interval1) {
            return interval0.getStart().compareTo(interval1.getStart());
        }
        
    }
    
}
