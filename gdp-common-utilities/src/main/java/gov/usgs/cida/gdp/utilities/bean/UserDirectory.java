package gov.usgs.cida.gdp.utilities.bean;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 *
 * @author isuftin
 */
@XStreamAlias("userDirectory")
public class UserDirectory extends XmlResponse {

    private String directory;

    /**
     * @return the directory
     */
    public String getDirectory() {
        return directory;
    }

    /**
     * @param directory the directory to set
     */
    public void setDirectory(String directory) {
        this.directory = directory;
    }
}
