package gov.usgs.cida.gdp.wps.parser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.io.FileUtils;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.n52.wps.io.datahandler.parser.AbstractParser;

public class GMLStreamingParser extends AbstractParser {

	public GMLStreamingParser() {
		supportedIDataTypes.add(GTVectorDataBinding.class);
	}

    @Override
    public GTVectorDataBinding parse(InputStream input, String mimeType, String schema) {
        try {
            File tempFile = File.createTempFile(getClass().getSimpleName(), ".xml");
            FileUtils.copyInputStreamToFile(input, tempFile);
			return new GTVectorDataBinding(new GMLStreamingFeatureCollection(tempFile));
		} catch (IOException e) {
			throw new RuntimeException("Error creating temporary file", e);
		}
    }

}
