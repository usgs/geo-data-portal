package gov.usgs.cida.derivative.time;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tkunicki
 */
public class CalendarUtil {
    
    private final static Logger LOGGER = LoggerFactory.getLogger(CalendarUtil.class); 
    
    private final static String FORMAT_ISO8601_UTC = "yyyy-MM-dd'T'HH:mm:ss.S'Z'";
    private final static String FORMAT_CF_UTC = "yyyy-MM-dd HH:mm:ss 'UTC'";
    
    public final static TimeZone TZ_UTC = TimeZone.getTimeZone("UTC");
    
    public final static long SECOND_DURATION_MILLIS = 1000;
    public final static long MINUTE_DURATION_MILLIS = SECOND_DURATION_MILLIS * 60;
    public final static long HOUR_DURATION_MILLIS = MINUTE_DURATION_MILLIS * 60;
    public final static long DAY_DURATION_MILLIS = HOUR_DURATION_MILLIS * 24;
    
    public static Calendar convertToCalendarUTC(Date date) {
        Calendar calendar = Calendar.getInstance(TZ_UTC);
        calendar.setTime(date);
        return calendar;
    }
    
    public static String formatISO8601_UTC(Date date) {
        DateFormat dateFormat = new SimpleDateFormat(FORMAT_ISO8601_UTC);
        dateFormat.setTimeZone(TZ_UTC);
        return dateFormat.format(date);
    }
    
    public static String formatISO8601_UTC(Calendar calendar) {
        return formatISO8601_UTC(calendar.getTime());
    }
    
    public static String formatCF_UTC(Date date) {
        DateFormat dateFormat = new SimpleDateFormat(FORMAT_CF_UTC);
        dateFormat.setTimeZone(TZ_UTC);
        return dateFormat.format(date);
    }
    
    public static String formatCF_UTC(Calendar calendar) {
        return formatCF_UTC(calendar.getTime());
    }
    
    public static void clearBelow(Calendar calendar, int field) {
        LOGGER.trace("before clear {}", formatISO8601_UTC(calendar));
        // NOTE:  don't use Calendar.clear(int field)!  
        switch (field) {
            case Calendar.YEAR:
                calendar.set(Calendar.MONTH, 0);
            case Calendar.MONTH:
                calendar.set(Calendar.DATE, 1);
            case Calendar.DATE:
                calendar.set(Calendar.HOUR_OF_DAY, 0);
            case Calendar.HOUR_OF_DAY:
                calendar.set(Calendar.MINUTE, 0);
            case Calendar.MINUTE:
                calendar.set(Calendar.SECOND, 0);
            case Calendar.SECOND:
                calendar.set(Calendar.MILLISECOND, 0);
                break;
            default:
                throw new IllegalArgumentException("Unsupported Calendar Field.");
        }
        LOGGER.trace("after clear {}", formatISO8601_UTC(calendar));
    }
}
