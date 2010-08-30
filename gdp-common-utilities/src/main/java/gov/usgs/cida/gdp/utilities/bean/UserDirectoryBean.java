/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usgs.cida.gdp.utilities.bean;

import gov.usgs.cida.gdp.utilities.bean.XmlBean;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 *
 * @author isuftin
 */
@XStreamAlias("userDirectory")
public class UserDirectoryBean implements XmlBean {

    private String directory;

    @Override
    public String toXml() {
        XStream xstream = new XStream();
        xstream.processAnnotations(UserDirectoryBean.class);
        StringBuffer sb = new StringBuffer();
        String result = "";
        sb.append(xstream.toXML(this));
        result = sb.toString();
        return result;
    }

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
