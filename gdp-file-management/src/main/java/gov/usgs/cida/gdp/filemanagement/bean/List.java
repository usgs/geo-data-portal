package gov.usgs.cida.gdp.filemanagement.bean;


import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import gov.usgs.cida.gdp.utilities.bean.XmlResponse;

@XStreamAlias("list")
public class List implements XmlResponse {
	
	@XStreamAlias("list")
	@XStreamImplicit(itemFieldName="element")
	private java.util.List<String> list;

	public List(java.util.List<String> l) {
		this.list = l;
	}

	public void setList(java.util.List<String> l) {
		this.list = l;
	}

	public java.util.List<String> getList() {
		return list;
	}
}
