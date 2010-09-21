package gov.usgs.cida.gdp.dataintrospection.bean;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import gov.usgs.cida.gdp.utilities.bean.XmlResponse;

@XStreamAlias("features")
public class Feature extends XmlResponse{

	@XStreamAlias("fileSetName")
	private String filesetName;
	
	@XStreamAlias("features")
	@XStreamImplicit(itemFieldName="feature")
	private List<String> features;

	public Feature(List<String> features) {
		this.features = features;
	}

	public void setFilesetName(String filesetName) {
		this.filesetName = filesetName;
	}

	public String getFilesetName() {
		return filesetName;
	}

	public void setFeatures(List<String> features) {
		this.features = features;
	}

	public List<String> getFeatures() {
		return features;
	}
	
	
}
