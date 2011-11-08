package gov.usgs.cida.derivative.time;

import org.joda.time.DateTime;
import org.joda.time.Interval;

/**
 *
 * @author tkunicki
 */
public interface TimeStepDescriptor {

    int getOutputTimeStepCount();

    int getOutputTimeStepIndex(DateTime dateTime);

    Interval getInputInterval();
    
    Interval getOutputInterval();
    
    DateTime getOutputTimeStepLowerBound(int timeStepIndex);

    DateTime getOutputTimeStepUpperBound(int timeStepIndex);
    
    Interval getOutputTimeStepInterval(int timeStepIndex);
    
    int getDaysFromTimeStepLowerBound(int timeStepIndex);

    int getDaysFromTimeStepLowerBound(DateTime dateTime);
    
    int getDaysFromTimeStepUpperBound(int timeStepIndex);
    
    int getDaysFromTimeStepUpperBound(DateTime dateTime);

}
