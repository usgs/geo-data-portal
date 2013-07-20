package gov.usgs.cida.gdp.utilities.bean;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;
import com.thoughtworks.xstream.io.json.JsonWriter;
import com.thoughtworks.xstream.io.json.JsonWriter.Format;
import java.io.Writer;

/**
 *
 * @author isuftin
 */
public class Response {

	/*
	 * Keeping the non-namespaced XML here for legacy applications
	 * that depend on it
	 */
	public String toXMLFragment() {
		XStream xstream = new XStream();
		xstream.autodetectAnnotations(true);
		return xstream.toXML(this);
	}
	
	public String toXML() {
		XStream stream = new XStream();
		stream.autodetectAnnotations(true);
		return stream.toXML(this);
	}

	public String toJSON() {
		XStream xstream = new XStream(new JettisonMappedXmlDriver());
		xstream.autodetectAnnotations(true);
		xstream.setMode(XStream.NO_REFERENCES);
		return xstream.toXML(this);
	}
}
