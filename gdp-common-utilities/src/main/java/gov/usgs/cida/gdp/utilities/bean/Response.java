package gov.usgs.cida.gdp.utilities.bean;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;
import com.thoughtworks.xstream.io.json.JsonWriter;
import java.io.Writer;

/**
 *
 * @author isuftin
 */
public class Response {

	public String toXML() {
		XStream stream = new XStream();
		stream.autodetectAnnotations(true);
		return stream.toXML(this);
	}

	public String toJSON() {
		XStream xstream = new XStream(new JsonHierarchicalStreamDriver() {
			@Override
			public HierarchicalStreamWriter createWriter(Writer writer) {
				return new JsonWriter(writer, JsonWriter.DROP_ROOT_MODE);
			}
		});
		xstream.autodetectAnnotations(true);
		xstream.setMode(XStream.NO_REFERENCES);
		return xstream.toXML(this);
	}
}
