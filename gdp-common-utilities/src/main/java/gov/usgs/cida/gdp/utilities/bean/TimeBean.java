package gov.usgs.cida.gdp.utilities.bean;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ucar.nc2.dt.grid.GeoGrid;
import ucar.nc2.dt.grid.GridDataset;
import ucar.nc2.util.NamedObject;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("availabletimes")
public class TimeBean implements XmlBean {

    @XStreamAlias("times")
    @XStreamImplicit(itemFieldName = "time")
    private List<String> time;
    private TimeBreakdown starttime;
    private TimeBreakdown endtime;

    public TimeBean() {
        this.time = new ArrayList<String>();
        this.setStarttime(new TimeBreakdown());
        this.setEndtime(new TimeBreakdown());
    }

    public TimeBean(GridDataset geoGrid, String gridSelection) {
        List<String> result = new ArrayList<String>();
        GeoGrid grid = geoGrid.findGridByName(gridSelection);
        for (NamedObject time : grid.getTimes()) {
            result.add(time.getName());
            this.time = result;
        }
    }

    public TimeBean(List<String> dateRange) throws ParseException {
        this.time = dateRange;

        if (this.time.isEmpty()) {
            this.setStarttime(new TimeBreakdown());
            this.setEndtime(new TimeBreakdown());
        } else {
            this.setStarttime(new TimeBreakdown(dateRange.get(0)));
            this.setEndtime(new TimeBreakdown(dateRange.get(1)));
        }
    }

    @Override
    public String toXml() {
        XStream xstream = new XStream();
        xstream.processAnnotations(TimeBean.class);
        StringBuffer sb = new StringBuffer();
        String result = "";
        sb.append(xstream.toXML(this));
        result = sb.toString();
        return result;
    }

    public void setTime(List<String> time) {
        this.time = time;
    }

    public List<String> getTime() {
        return time;
    }

    public void setStarttime(TimeBreakdown starttime) {
        this.starttime = starttime;
    }

    public TimeBreakdown getStarttime() {
        return starttime;
    }

    public void setEndtime(TimeBreakdown endtime) {
        this.endtime = endtime;
    }

    public TimeBreakdown getEndtime() {
        return endtime;
    }

    static class TimeBreakdown implements XmlBean {

        private int month;
        private int day;
        private int year;
        private int timezone;
        private int hour;
        private int minute;
        private int second;

        public TimeBreakdown() {
        }

        public TimeBreakdown(String dateInstance) throws ParseException {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date dateInstanceDate = null;
            try {
                dateInstanceDate = sdf.parse(dateInstance);
            } catch (ParseException pe) {
                throw pe;
            }

            Calendar cal = Calendar.getInstance();
            cal.setTime(dateInstanceDate);
            this.month = cal.get(Calendar.MONTH) + 1;
            this.day = cal.get(Calendar.DAY_OF_MONTH);
            this.year = cal.get(Calendar.YEAR);
            this.hour = cal.get(Calendar.HOUR_OF_DAY);
            this.minute = cal.get(Calendar.MINUTE);
            this.second = cal.get(Calendar.SECOND);

        }

        public TimeBreakdown(Calendar cal) {
            this.month = cal.get(Calendar.MONTH) + 1;
            this.day = cal.get(Calendar.DAY_OF_MONTH);
            this.year = cal.get(Calendar.YEAR);
            this.hour = cal.get(Calendar.HOUR_OF_DAY);
            this.minute = cal.get(Calendar.MINUTE);
            this.second = cal.get(Calendar.SECOND);
        }

        @Override
        public String toXml() {
            XStream xstream = new XStream();
            xstream.processAnnotations(TimeBreakdown.class);
            StringBuffer sb = new StringBuffer();
            String result = "";
            sb.append(xstream.toXML(this));
            result = sb.toString();
            return result;
        }

        public int getMonth() {
            return month;
        }

        public void setMonth(int month) {
            this.month = month;
        }

        public int getDay() {
            return day;
        }

        public void setDay(int day) {
            this.day = day;
        }

        public int getYear() {
            return year;
        }

        public void setYear(int year) {
            this.year = year;
        }

        public int getTimezone() {
            return timezone;
        }

        public void setTimezone(int timezone) {
            this.timezone = timezone;
        }

        public int getHour() {
            return hour;
        }

        public void setHour(int hour) {
            this.hour = hour;
        }

        public int getMinute() {
            return minute;
        }

        public void setMinute(int minute) {
            this.minute = minute;
        }

        public int getSecond() {
            return second;
        }

        public void setSecond(int second) {
            this.second = second;
        }
    }
}
