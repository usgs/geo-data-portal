package gov.usgs.gdp.bean;

import ucar.nc2.VariableSimpleIF;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("grid")
public class GridBean implements XmlBean {

	private String description;
	private String name;
	private int rank;
	private int[] shape;
	private String shortname;
	private String unitsstring;
	
	public GridBean() {	}
	public GridBean(VariableSimpleIF variableSimpleIF) {
		this.description = variableSimpleIF.getDescription();
		this.name = variableSimpleIF.getName();
		this.rank = variableSimpleIF.getRank();
		this.shape = variableSimpleIF.getShape();
		this.shortname = variableSimpleIF.getShortName();
		this.unitsstring = variableSimpleIF.getUnitsString();
	}
	
	@Override
	public String toXml() {
		XStream xstream = new XStream();
		xstream.processAnnotations(ErrorBean.class);
		StringBuffer sb = new StringBuffer();
		String result = "";
		sb.append(xstream.toXML(this));
		result = sb.toString();
		return result;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getRank() {
		return rank;
	}
	public void setRank(int rank) {
		this.rank = rank;
	}
	public int[] getShape() {
		return shape;
	}
	public void setShape(int[] shape) {
		this.shape = shape;
	}
	public String getShortname() {
		return shortname;
	}
	public void setShortname(String shortname) {
		this.shortname = shortname;
	}
	public String getUnitsstring() {
		return unitsstring;
	}
	public void setUnitsstring(String unitsstring) {
		this.unitsstring = unitsstring;
	}
	
	
	
}
