package gov.usgs.derivative.time;

import gov.usgs.derivative.time.TimeStepDescriptor;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tkunicki
 */
public abstract class AbstractTimeStepDescriptor implements TimeStepDescriptor {

    private final static Logger LOGGER = LoggerFactory.getLogger(AbstractTimeStepDescriptor.class);
    
    
    @Override
    public int getDaysFromTimeStepLowerBound(int timeStepIndex) {
        return Days.daysBetween(
                getOutputInterval().getStart(),
                getOutputTimeStepInterval(timeStepIndex).getStart()).getDays();
    }
    
    @Override
    public int getDaysFromTimeStepLowerBound(DateTime dateTime) {
        return getDaysFromTimeStepLowerBound(getOutputTimeStepIndex(dateTime));
    }
    
    @Override
    public int getDaysFromTimeStepUpperBound(int timeStepIndex) {
        return Days.daysBetween(
                getOutputInterval().getStart(),
                getOutputTimeStepInterval(timeStepIndex).getEnd()).getDays();
    }
    
    @Override
    public int getDaysFromTimeStepUpperBound(DateTime dateTime) {
        return getDaysFromTimeStepUpperBound(getOutputTimeStepIndex(dateTime));
    }
    
}
