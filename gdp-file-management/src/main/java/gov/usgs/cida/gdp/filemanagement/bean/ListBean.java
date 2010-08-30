package gov.usgs.cida.gdp.filemanagement.bean;

import gov.usgs.cida.gdp.utilities.bean.XmlBean;
import java.util.List;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("list")
public class ListBean implements XmlBean {
	
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

	@Override
	public String toXml() {
		XStream xstream = new XStream();
		xstream.autodetectAnnotations(true);
		StringBuilder sb = new StringBuilder();
		String result = "";
		sb.append(xstream.toXML(this));
		result = sb.toString();
		return result;
	}
}
