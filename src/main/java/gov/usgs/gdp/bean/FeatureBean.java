package gov.usgs.gdp.bean;

import java.util.List;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("features")
public class FeatureBean implements XmlBean{

	@XStreamAlias("fileset-name")
	private String filesetName;
	
	@XStreamAlias("features")
	@XStreamImplicit(itemFieldName="feature")
	private List<String> features;

	public FeatureBean(List<String> features) {
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

	@Override
	public String toXml() {
		XStream xstream = new XStream();
		xstream.processAnnotations(FeatureBean.class);
		StringBuffer sb = new StringBuffer();
		String result = "";
		sb.append(xstream.toXML(this));
		result = sb.toString();
		return result;
	}
	
	
}
