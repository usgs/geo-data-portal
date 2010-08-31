package gov.usgs.cida.gdp.dataintrospection.bean;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import gov.usgs.cida.gdp.utilities.bean.XmlResponse;

@XStreamAlias("attributes")
public class Attribute implements XmlResponse {
	
	@XStreamAlias("fileSetName")
	private String filesetName;
	
	@XStreamAlias("attributes")
	@XStreamImplicit(itemFieldName="attribute")
	private List<String> attributes;

	public Attribute(List<String> attributeList) {
		this.attributes = attributeList;
	}

	public void setAttribute(List<String> attribute) {
		this.attributes = attribute;
	}

	public List<String> getAttribute() {
		return attributes;
	}

	public void setFilesetName(String filesetName) {
		this.filesetName = filesetName;
	}

	public String getFilesetName() {
		return filesetName;
	}
	
}
