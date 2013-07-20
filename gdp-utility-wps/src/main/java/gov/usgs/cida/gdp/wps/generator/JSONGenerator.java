package gov.usgs.cida.gdp.wps.generator;

import gov.usgs.cida.gdp.wps.binding.JSONBinding;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.n52.wps.FormatDocument;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.PlainStringBinding;
import org.n52.wps.io.datahandler.generator.AbstractGenerator;

/**
 *
 * @author isuftin
 */
public class JSONGenerator extends AbstractGenerator {

	public JSONGenerator() {
		super();
		supportedIDataTypes.add(JSONBinding.class);
	}

	@Override
	public InputStream generateStream(IData idata, String mimeType, String schema) throws IOException {
		String json = null;
		try {
			json = (String)idata.getPayload();
		} catch (Exception ex) {
			throw new IOException("The data passed from the algorithm to the generator is not a String");
		}
		return new ByteArrayInputStream(json.getBytes());
	}
}
