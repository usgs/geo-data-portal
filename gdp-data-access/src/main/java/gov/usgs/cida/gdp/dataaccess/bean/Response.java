package gov.usgs.cida.gdp.dataaccess.bean;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
import gov.usgs.cida.gdp.dataaccess.cache.ResponseCache.CacheIdentifier;
import java.io.Serializable;

/**
 *
 * @author isuftin
 */
public abstract class Response implements Serializable {
	private static final long serialVersionUID = 876876L;
	
	public static Response buildFromCache(CacheIdentifier ci) {
		throw new UnsupportedOperationException("This operation not supported on parent class");
	}
	
	public abstract boolean writeToCache(CacheIdentifier ci);
	
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
