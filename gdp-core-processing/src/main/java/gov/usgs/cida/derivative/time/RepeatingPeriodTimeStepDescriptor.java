package gov.usgs.cida.derivative.time;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Interval;
import org.joda.time.MutableDateTime;
import org.joda.time.ReadablePeriod;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tkunicki
 */
public class RepeatingPeriodTimeStepDescriptor extends AbstractTimeStepDescriptor {

    private final static Logger LOGGER = LoggerFactory.getLogger(RepeatingPeriodTimeStepDescriptor.class);

    private final Interval inputInterval;
    private final Interval outputInterval;
    private final ReadablePeriod repeatingPeriod;
    
    private int timeStepCount;
    
    private final PeriodUtility.RepeatingPeriodIndexGenerator indexGenerator;
    
    
    public RepeatingPeriodTimeStepDescriptor(Interval inputInterval, ReadablePeriod repeatingPeriod) {
        
        this.inputInterval = inputInterval;
        this.repeatingPeriod = repeatingPeriod;
        
        // check duration, can't suppport periods less that P1D due to NetCDF time axis units
        // TODO: support periods < P1D
        if (inputInterval.withPeriodAfterStart(Days.ONE).toDuration().isLongerThan(inputInterval.withPeriodAfterStart(repeatingPeriod).toDuration())) {
            throw new IllegalArgumentException("output period must be longer than P1D");
        }

        LOGGER.debug("Start time step is {}", inputInterval.getStart().toString());
        LOGGER.debug("End time step is {}", inputInterval.getEnd().toString());

        timeStepCount = 0;
        DateTime timeStepDateTime = new DateTime(inputInterval.getStart());
        // NOTE:  inputInterval.getEnd() is EXCLUSIVE
        while (timeStepDateTime.isBefore(inputInterval.getEnd())) {
            timeStepCount++;
            LOGGER.trace("time step {} lower bound is {}", timeStepCount, ISODateTimeFormat.dateTime().print(timeStepDateTime));
            timeStepDateTime = timeStepDateTime.plus(repeatingPeriod);
            LOGGER.trace("time step {} upper bound is {}", timeStepCount, ISODateTimeFormat.dateTime().print(timeStepDateTime));
        }
        this.outputInterval = new Interval(
                inputInterval.getStart(),
                timeStepDateTime);
        
        indexGenerator = PeriodUtility.createRepeatingPeriodIndexGenerator(repeatingPeriod, outputInterval.getStart());
    }

    @Override
    public Interval getInputInterval() {
        return inputInterval;
    }
    
    public ReadablePeriod getRepeatingPeriod() {
        return repeatingPeriod;
    }
    
    @Override
    public Interval getOutputInterval() {
        return outputInterval;
    }
    
    @Override
    public DateTime getOutputTimeStepLowerBound(int timeStepIndex) {
        MutableDateTime lowerBound = outputInterval.getStart().toMutableDateTime();
        lowerBound.add(repeatingPeriod, timeStepIndex);
        return lowerBound.toDateTime();
    }

    @Override
    public DateTime getOutputTimeStepUpperBound(int timeStepIndex) {
        return getOutputTimeStepLowerBound(timeStepIndex + 1);
    }

    @Override
    public Interval getOutputTimeStepInterval(int timeStepIndex) {
        return new Interval(getOutputTimeStepLowerBound(timeStepIndex), repeatingPeriod);
    }
    
    @Override
    public int getOutputTimeStepCount() {
        return timeStepCount;
    }

    @Override
    public int getOutputTimeStepIndex(DateTime timeStepDateTime) {
        return indexGenerator.generateTimeStepIndex(timeStepDateTime);
    }
    
}
