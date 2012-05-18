package gov.usgs.derivative.time;

import java.util.Date;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import ucar.nc2.dataset.CoordinateAxis1DTime;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.units.DateRange;

/**
 *
 * @author tkunicki
 */
public class NetCDFDateUtil {
    
    public static Interval toIntervalUTC(GridDatatype gridDatatype) {
        GridCoordSystem gridCoordSystem = gridDatatype.getCoordinateSystem();
        CoordinateAxis1DTime timeAxis = gridCoordSystem.getTimeAxis1D();
        return timeAxis == null ?
                null :
                toIntervalUTC(timeAxis.getDateRange());
    }
    
    public static Interval toIntervalUTC(DateRange dateRange) {
        return new Interval(
                toDateTimeUTC(dateRange.getStart().getDate()),
                toDateTimeUTC(dateRange.getEnd().getDate()));
    }
    
    public static DateTime toDateTimeUTC(Date date) {
        return new DateTime(date, DateTimeZone.UTC);
    }
    
}
