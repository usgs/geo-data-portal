package gov.usgs.gdp.bean;

import gov.usgs.cida.gdp.utilities.bean.XmlBean;
import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("outputfiletypes")
public class OutputFileTypeBean implements XmlBean{
	@XStreamImplicit(itemFieldName="type")
	private List<String> types;

	public OutputFileTypeBean() {
		this.types = new ArrayList<String>();
	}
	
	public OutputFileTypeBean(List<String> types) {
		this.types = types;
	}
	
	public OutputFileTypeBean(String... types) {
		this.types = new ArrayList<String>();
		for (String type : types) {
			this.types.add(type);
		}
	}
	
	@Override
	public String toXml() {
		XStream xstream = new XStream();
		xstream.processAnnotations(OutputFileTypeBean.class);
		StringBuffer sb = new StringBuffer();
		String result = "";
		sb.append(xstream.toXML(this));
		result = sb.toString();
		return result;
	}
	
	public void setTypes(List<String> types) {
		this.types = types;
	}

	public List<String> getTypes() {
		return types;
	}

	
}
