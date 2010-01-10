package gov.usgs.gdp.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class THREDDSInfoBean implements Serializable {
	private String THREDDSServer;
	private String dataSetUrlSelection;
	private String dataSetNameSelection;
	private String gridItemSelection;
	private String fromTime;
	private String toTime;
	private List<String> openDapStandardURLNameList;
	private List<String> openDapDataSetNameList;
	private List<String> openDapGridItems;
	private List<String> openDapGridTimes;
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public String getTHREDDSServer() {
		return THREDDSServer;
	}
	public void setTHREDDSServer(String tHREDDSServer) {
		THREDDSServer = tHREDDSServer;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	public List<String> getOpenDapStandardURLNameList() {
		if (this.openDapStandardURLNameList == null) this.openDapStandardURLNameList = new ArrayList<String>(); 
		return openDapStandardURLNameList;
	}
	public void setOpenDapStandardURLNameList(List<String> openDapStandardURLNameList) {
		this.openDapStandardURLNameList = openDapStandardURLNameList;
	}
	public List<String> getOpenDapDataSetNameList() {
		if (this.openDapDataSetNameList == null) this.openDapDataSetNameList = new ArrayList<String>();
		return openDapDataSetNameList;
	}
	public void setOpenDapDataSetNameList(List<String> openDapDataSetNameList) {
		this.openDapDataSetNameList = openDapDataSetNameList;
	}
	public String getDataSetUrlSelection() {
		return dataSetUrlSelection;
	}
	public void setDataSetUrlSelection(String dataSetUrlSelection) {
		this.dataSetUrlSelection = dataSetUrlSelection;
	}
	public String getDataSetNameSelection() {
		return dataSetNameSelection;
	}
	public void setDataSetNameSelection(String dataSetNameSelection) {
		this.dataSetNameSelection = dataSetNameSelection;
	}
	public String getGridItemSelection() {
		return gridItemSelection;
	}
	public void setGridItemSelection(String gridItemSelection) {
		this.gridItemSelection = gridItemSelection;
	}
	public List<String> getOpenDapGridItems() {
		return openDapGridItems;
	}
	public void setOpenDapGridItems(List<String> openDapGridItems) {
		this.openDapGridItems = openDapGridItems;
	}
	public String getFromTime() {
		return fromTime;
	}
	public void setFromTime(String fromTime) {
		this.fromTime = fromTime;
	}
	public String getToTime() {
		return toTime;
	}
	public void setToTime(String toTime) {
		this.toTime = toTime;
	}
	public List<String> getOpenDapGridTimes() {
		if (this.openDapGridTimes == null) this.openDapGridTimes = new ArrayList<String>();
		return openDapGridTimes;
	}
	public void setOpenDapGridTimes(List<String> openDapGridTimes) {
		this.openDapGridTimes = openDapGridTimes;
	}


}
