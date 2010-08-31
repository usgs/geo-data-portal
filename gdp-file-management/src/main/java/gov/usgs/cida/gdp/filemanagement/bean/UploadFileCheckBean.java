package gov.usgs.cida.gdp.filemanagement.bean;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import gov.usgs.cida.gdp.utilities.bean.XmlResponse;

@XStreamAlias("uploadFileCheck")
public class UploadFileCheckBean implements XmlResponse {

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
