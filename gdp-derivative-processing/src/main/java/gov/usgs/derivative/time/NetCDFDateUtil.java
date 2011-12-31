package gov.usgs.derivative.time;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import ucar.nc2.units.DateRange;

/**
 *
 * @author tkunicki
 */
public class NetCDFDateUtil {
    
    public static Interval convertDateRangeToInterval(DateRange dateRange) {
        return new Interval(
                new DateTime(dateRange.getStart().getDate(), DateTimeZone.UTC),
                new DateTime(dateRange.getEnd().getDate(), DateTimeZone.UTC));
    }
    
}
