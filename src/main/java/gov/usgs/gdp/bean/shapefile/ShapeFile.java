package gov.usgs.gdp.bean.shapefile;

import java.io.File;

public abstract class ShapeFile {
	File file;
	
	public String getFileName() {
		return this.file.getName();
	}
	
	public File getFile() {
		return this.file;
	}

	public void setFile(File file) {
		this.file = file;
	}
	
}
