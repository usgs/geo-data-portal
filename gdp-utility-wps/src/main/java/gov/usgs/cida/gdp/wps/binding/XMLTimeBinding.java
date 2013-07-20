package gov.usgs.cida.gdp.wps.binding;

import org.n52.wps.io.data.IComplexData;

/**
 *
 * @author isuftin
 */
public class XMLTimeBinding implements IComplexData {
	protected String xml;
	protected String mimeType;
		
	public XMLTimeBinding(String xml) {
		this.xml = xml;
		this.mimeType = "text/xml";
	}

	public String getMimeType() {
		return this.mimeType;
	}
	
	@Override
	public String getPayload() {
		return xml;
	}

	@Override
	public Class getSupportedClass() {
		return String.class;
	}

	@Override
	public void dispose() {
	}
	
}
