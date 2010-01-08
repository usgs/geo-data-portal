package gov.usgs.gdp.bean;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

public class FilesBean {
	private Collection<File> files;
	

	public FilesBean() {
		this.files = new ArrayList<File>();
	}
	
	public FilesBean(Collection<File> files) {
		this.files = files;
	}
	
	public Collection<File> getFiles() {
		return files;
	}

	public void setFiles(Collection<File> files) {
		this.files = files;
	}
	
}
