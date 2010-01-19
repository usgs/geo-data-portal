package gov.usgs.gdp.bean;


import gov.usgs.gdp.helper.PropertyFactory;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import ucar.nc2.dt.grid.GeoGrid;


public class THREDDSInfoBean implements Serializable {
	private String THREDDSServer;
	private String dataSetUrlSelection;
	private String dataSetNameSelection;
	private String gridItemSelection;
	private String fromTime;
	private String toTime;
	private String fileLink;
	private List<String> openDapStandardURLNameList;
	private List<String> openDapDataSetNameList;
	private List<String> openDapGridItems;
	private List<String> openDapGridTimes;
	private List<String> statsSummary;
	private GeoGrid geoGrid;
	
	


	public int getFromYear() throws ParseException {
		int result = -1;
		
		String beginDate = getOpenDapGridTimes().get(0);
		String substrDate = beginDate.substring(0, beginDate.indexOf(' '));
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		Date parsedDate = null;
		parsedDate = df.parse(substrDate);
		Calendar calendar  = Calendar.getInstance();
		calendar.setTime(parsedDate);
		result = calendar.get(Calendar.YEAR);

		return result;
	}
	
	/**
	 * Returns a Map<String, String> object of the URLs from the property file
	 * 
	 * @return empty Map<String, String> if not found
	 */
	public static Map<String, String> getTHREDDSUrlMap() {
		Map<String, String> result = new TreeMap<String, String>();
		int urlCounter = 0;
		String urlProperty = "";
		do {
			urlProperty = PropertyFactory.getProperty("thredds.url." + urlCounter);
			urlCounter++;
			if (!"".equals(urlProperty)) {
				String key = urlProperty.substring(0,urlProperty.indexOf(";"));
				String property = urlProperty.substring(urlProperty.indexOf(";") + 1);
				result.put(key, property);
			}
		} while (!"".equals(urlProperty));
		
		return result;
		
	}
	
	public int getToYear() throws ParseException {
		int result = -1;
		
		String endDate = getOpenDapGridTimes().get(getOpenDapGridTimes().size() - 1);
		String substrDate = endDate.substring(0, endDate.indexOf(' '));
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		Date parsedDate = null;
		parsedDate = df.parse(substrDate);
		Calendar calendar  = Calendar.getInstance();
		calendar.setTime(parsedDate);
		result = calendar.get(Calendar.YEAR);

		return result;
	}
	
	public int getToDay() throws ParseException {
		int result = -1;
		
		String endDate = getOpenDapGridTimes().get(getOpenDapGridTimes().size() - 1);
		String substrDate = endDate.substring(0, endDate.indexOf(' '));
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		Date parsedDate = null;
		parsedDate = df.parse(substrDate);
		Calendar calendar  = Calendar.getInstance();
		calendar.setTime(parsedDate);
		result = calendar.get(Calendar.DAY_OF_MONTH);

		return result;
	}
	
	public int getFromDay() throws ParseException {
		int result = -1;
		
		String beginDate = getOpenDapGridTimes().get(0);
		String substrDate = beginDate.substring(0, beginDate.indexOf(' '));
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		Date parsedDate = null;
		parsedDate = df.parse(substrDate);
		Calendar calendar  = Calendar.getInstance();
		calendar.setTime(parsedDate);
		result = calendar.get(Calendar.DAY_OF_MONTH);

		return result;
	}
	
	
	public int getFromMonth() throws ParseException {
		int result = -1;
		
		String beginDate = getOpenDapGridTimes().get(0);
		String substrDate = beginDate.substring(0, beginDate.indexOf(' '));
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		Date parsedDate = null;
		parsedDate = df.parse(substrDate);
		Calendar calendar  = Calendar.getInstance();
		calendar.setTime(parsedDate);
		result = calendar.get(Calendar.MONTH);

		return result;
	}
	
	
	public int getToMonth() throws ParseException {
		int result = -1;
		
		String endDate = getOpenDapGridTimes().get(getOpenDapGridTimes().size() - 1);
		String substrDate = endDate.substring(0, endDate.indexOf(' '));
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		Date parsedDate = null;
		parsedDate = df.parse(substrDate);
		Calendar calendar  = Calendar.getInstance();
		calendar.setTime(parsedDate);
		result = calendar.get(Calendar.MONTH);

		return result;
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public String getTHREDDSServer() {
		return this.THREDDSServer;
	}
	public void setTHREDDSServer(String tHREDDSServer) {
		this.THREDDSServer = tHREDDSServer;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	public List<String> getOpenDapStandardURLNameList() {
		if (this.openDapStandardURLNameList == null) this.openDapStandardURLNameList = new ArrayList<String>(); 
		return this.openDapStandardURLNameList;
	}
	public void setOpenDapStandardURLNameList(List<String> localOpenDapStandardURLNameList) {
		this.openDapStandardURLNameList = localOpenDapStandardURLNameList;
	}
	public List<String> getOpenDapDataSetNameList() {
		if (this.openDapDataSetNameList == null) this.openDapDataSetNameList = new ArrayList<String>();
		return this.openDapDataSetNameList;
	}
	public void setOpenDapDataSetNameList(List<String> localOpenDapDataSetNameList) {
		this.openDapDataSetNameList = localOpenDapDataSetNameList;
	}
	public String getDataSetUrlSelection() {
		return this.dataSetUrlSelection;
	}
	public void setDataSetUrlSelection(String localDataSetUrlSelection) {
		this.dataSetUrlSelection = localDataSetUrlSelection;
	}
	public String getDataSetNameSelection() {
		return this.dataSetNameSelection;
	}
	public void setDataSetNameSelection(String localDataSetNameSelection) {
		this.dataSetNameSelection = localDataSetNameSelection;
	}
	public String getGridItemSelection() {
		return this.gridItemSelection;
	}
	public void setGridItemSelection(String localGridItemSelection) {
		this.gridItemSelection = localGridItemSelection;
	}
	public List<String> getOpenDapGridItems() {
		if (this.openDapGridTimes == null) this.openDapGridTimes = new ArrayList<String>();
		return this.openDapGridItems;
	}
	public void setOpenDapGridItems(List<String> localOpenDapGridItems) {
		this.openDapGridItems = localOpenDapGridItems;
	}
	public String getFromTime() {
		return this.fromTime;
	}
	public void setFromTime(String localFromTime) {
		this.fromTime = localFromTime;
	}
	public String getToTime() {
		return this.toTime;
	}
	public void setToTime(String localToTime) {
		this.toTime = localToTime;
	}
	public List<String> getOpenDapGridTimes() {
		if (this.openDapGridTimes == null) this.openDapGridTimes = new ArrayList<String>();
		return this.openDapGridTimes;
	}
	public void setOpenDapGridTimes(List<String> localOpenDapGridTimes) {
		this.openDapGridTimes = localOpenDapGridTimes;
	}

	public GeoGrid getGeoGrid() {
		return geoGrid;
	}

	public void setGeoGrid(GeoGrid geoGrid) {
		this.geoGrid = geoGrid;
	}

	public List<String> getStatsSummary() {
		return statsSummary;
	}

	public void setStatsSummary(List<String> statsSummary) {
		this.statsSummary = statsSummary;
	}

	public String getFileLink() {
		return fileLink;
	}

	public void setFileLink(String fileLink) {
		this.fileLink = fileLink;
	}

}
