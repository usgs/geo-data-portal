package gov.usgs.gdp.bean;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("upload-file-check")
public class UploadFileCheckBean implements XmlBean{

	private boolean exists;
	private String file;
	
	public UploadFileCheckBean() {
		this.exists = false;
	}
	
	public UploadFileCheckBean(boolean exists) {
		new UploadFileCheckBean(null, exists);
	}
	
	
	public UploadFileCheckBean(String file, boolean doesDirectoryOrFileExist) {
		this.file = file;
		this.exists = doesDirectoryOrFileExist;
	}

	public String toXml() {
		XStream xstream = new XStream();
		xstream.processAnnotations(FilesBean.class);
		return xstream.toXML(this);
	}


	public void setExists(boolean exists) {
		this.exists = exists;
	}


	public boolean isExists() {
		return exists;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public String getFile() {
		return file;
	}
}
