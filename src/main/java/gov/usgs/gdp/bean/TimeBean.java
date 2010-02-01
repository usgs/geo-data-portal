package gov.usgs.gdp.bean;

import gov.usgs.gdp.analysis.NetCDFUtility;

import java.io.IOException;
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
public class TimeBean implements XmlBean{

	@XStreamAlias("times")
	@XStreamImplicit(itemFieldName="time")
	private List<String> time;
	private TimeBreakdown start_time;
	private TimeBreakdown end_time;
	public TimeBean() {}
	public TimeBean(GridDataset geoGrid, String gridSelection) {
		List<String> result = new ArrayList<String>();
		GeoGrid grid = geoGrid.findGridByName(gridSelection);
		for (NamedObject time : grid.getTimes()) {
			result.add(time.getName());
			this.time = result;
		}
	}

	public static TimeBean getTimeBean(String location, String gridSelection) throws IOException, ParseException {
		List<String> dateRange = NetCDFUtility.getDateRange(location, gridSelection);
		TimeBean result = new TimeBean(dateRange);
		return result;
	}
	
	public TimeBean(List<String> dateRange) throws ParseException {
		this.time = dateRange;
		this.start_time = new TimeBreakdown(dateRange.get(0));
		this.end_time = new TimeBreakdown(dateRange.get(1));
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

	static class TimeBreakdown implements XmlBean{

		private int month;
		private int day;
		private int year;
		private int timezone;
		private int hour;
		private int minute;
		private int second;
		
		// Working on 0001-01-11 00:00:00Z
		public TimeBreakdown() {}
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
			
			
		}
		
		public TimeBreakdown(Calendar cal) {
			this.month = cal.get(Calendar.MONTH);
			this.day = cal.get(Calendar.DAY_OF_MONTH);
			this.year  = cal.get(Calendar.YEAR);
			this.hour  = cal.get(Calendar.HOUR_OF_DAY);
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
