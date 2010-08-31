package gov.usgs.cida.gdp.coreprocessing.bean;

import gov.usgs.cida.gdp.utilities.bean.XmlResponse;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("fileLocation")
public class FileLocation implements XmlResponse {

	@XStreamAlias("link")
	private String uploadLocation;
	
	@XStreamAlias("shapefile")
	private String shapefileLocation;

	public FileLocation(String uploadLocation) {
		this.uploadLocation = uploadLocation;
	}
	
	public FileLocation(String uploadLocation, String shapefileLocation) {
		this.uploadLocation = uploadLocation;
		this.shapefileLocation = shapefileLocation;
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
