package gov.usgs.gdp.bean;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ucar.nc2.dt.grid.GeoGrid;
import ucar.nc2.dt.grid.GridDataset;
import ucar.nc2.ft.FeatureDataset;
import ucar.nc2.util.NamedObject;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("availabletimes")
public class TimeBean implements XmlBean{

	@XStreamAlias("times")
	@XStreamImplicit(itemFieldName="time")
	private List<String> time;
	
	public TimeBean() {}
	public TimeBean(GridDataset geoGrid, String gridSelection) {
		List<String> result = new ArrayList<String>();
		GeoGrid grid = geoGrid.findGridByName(gridSelection);
		for (NamedObject time : grid.getTimes()) {
			result.add(time.getName());
			this.time = result;
		}
	}
	
	public static TimeBean getTimeBean(FeatureDataset geoGrid, String gridSelection) {
		if (geoGrid instanceof GridDataset) {
			return new TimeBean((GridDataset) geoGrid, gridSelection);
		}
		return null;
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

}
