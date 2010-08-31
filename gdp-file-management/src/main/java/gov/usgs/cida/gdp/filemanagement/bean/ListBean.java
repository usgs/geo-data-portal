package gov.usgs.cida.gdp.filemanagement.bean;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import gov.usgs.cida.gdp.utilities.bean.XmlResponse;

@XStreamAlias("list")
public class ListBean implements XmlResponse {
	
	@XStreamAlias("list")
	@XStreamImplicit(itemFieldName="element")
	private List<String> list;

	public ListBean(List<String> l) {
		this.list = l;
	}

	public void setList(List<String> l) {
		this.list = l;
	}

	public List<String> getList() {
		return list;
	}
}
