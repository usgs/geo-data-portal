package gov.usgs.cida.gdp.wps.generator;

import gov.usgs.cida.gdp.wps.binding.XMLBinding;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.datahandler.generator.AbstractGenerator;

/**
 *
 * @author isuftin
 */
public class XMLDataTypeCollectionGenerator extends AbstractGenerator {

	public XMLDataTypeCollectionGenerator() {
		super();
		supportedIDataTypes.add(XMLBinding.class);
	}

	@Override
	public InputStream generateStream(IData idata, String mimeType, String schema) throws IOException {
		String xml = null;
		try {
			xml = (String) idata.getPayload();
		} catch (Exception ex) {
			throw new IOException("The data passed from the algorithm to the generator is not a String");
		}
		return new ByteArrayInputStream(xml.getBytes("UTF-8"));
	}
}
