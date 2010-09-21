package gov.usgs.cida.gdp.filemanagement.bean;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import gov.usgs.cida.gdp.utilities.bean.XmlResponse;

@XStreamAlias("uploadFileCheck")
public class UploadFileCheck extends XmlResponse {

    private boolean exists;
    private String file;

    public UploadFileCheck() {
        this.exists = false;
    }

    public UploadFileCheck(boolean exists) {
        this(null, exists);
    }

    public UploadFileCheck(String file, boolean doesDirectoryOrFileExist) {
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
