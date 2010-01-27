package gov.usgs.gdp.bean;

import java.util.List;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("attributes")
public class AttributeBean implements XmlBean {
	
	@XStreamAlias("fileset-name")
	private String filesetName;
	
	@XStreamAlias("attributes")
	@XStreamImplicit(itemFieldName="attribute")
	private List<String> attributes;

	public AttributeBean(List<String> attributeList) {
		this.attributes = attributeList;
	}

	public void setAttribute(List<String> attribute) {
		this.attributes = attribute;
	}

	public List<String> getAttribute() {
		return attributes;
	}

	@Override
	public String toXml() {
		XStream xstream = new XStream();
		xstream.autodetectAnnotations(true);
		StringBuffer sb = new StringBuffer();
		String result = "";
		sb.append(xstream.toXML(this));
		result = sb.toString();
		return result;
	}

	public void setFilesetName(String filesetName) {
		this.filesetName = filesetName;
	}

	public String getFilesetName() {
		return filesetName;
	}
	
}
