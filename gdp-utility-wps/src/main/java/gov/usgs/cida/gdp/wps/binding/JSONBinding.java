package gov.usgs.cida.gdp.wps.binding;

import org.n52.wps.io.data.IComplexData;

/**
 *
 * @author isuftin
 */
public class JSONBinding implements IComplexData {
	protected final String json;
	protected String mimeType;
		
	public JSONBinding(String json) {
		this.json = json;
		this.mimeType = "application/json";
	}

	public String getMimeType() {
		return this.mimeType;
	}
	
	@Override
	public String getPayload() {
		return json;
	}

	@Override
	public Class getSupportedClass() {
		return String.class;
	}

	@Override
	public void dispose() {
		// Nothing to do here?
	}
	
}
