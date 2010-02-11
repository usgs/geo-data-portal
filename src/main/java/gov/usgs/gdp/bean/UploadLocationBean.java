package gov.usgs.gdp.bean;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("file-location")
public class UploadLocationBean implements XmlBean {

	@XStreamAlias("link")
	private String uploadLocation;

	public UploadLocationBean(String uploadLocation) {
		this.uploadLocation = uploadLocation;
	}

	@Override
	public String toXml() {
		XStream xstream = new XStream();
		xstream.processAnnotations(UploadLocationBean.class);
		StringBuffer sb = new StringBuffer();
		String result = "";
		sb.append(xstream.toXML(this));
		result = sb.toString();
		return result;
	}
	
	public void setUploadlocation(String uploadlocation) {
		this.uploadLocation = uploadlocation;
	}

	public String getUploadlocation() {
		return uploadLocation;
	}
	
	
}
