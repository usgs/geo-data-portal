package gov.usgs.cida.gdp.utilities.bean;

import com.thoughtworks.xstream.XStream;

/**
 *
 * @author isuftin
 */
public class XmlResponse {

    public String toXML() {
        XStream stream = new XStream();
        stream.autodetectAnnotations(true);
        return stream.toXML(this);
    }

}
