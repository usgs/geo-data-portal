package gov.usgs.gdp.bean;

import gov.usgs.cida.gdp.utilities.bean.XmlBean;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("fileLocation")
public class FileLocationBean implements XmlBean {

	@XStreamAlias("link")
	private String uploadLocation;
	
	@XStreamAlias("shapefile")
	private String shapefileLocation;

	public FileLocationBean(String uploadLocation) {
		this.uploadLocation = uploadLocation;
	}
	
	public FileLocationBean(String uploadLocation, String shapefileLocation) {
		this.uploadLocation = uploadLocation;
		this.shapefileLocation = shapefileLocation;
	}

	@Override
	public String toXml() {
		XStream xstream = new XStream();
		xstream.processAnnotations(FileLocationBean.class);
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
	
	public String getShapefilelocation() {
		return shapefileLocation;
	}
}
