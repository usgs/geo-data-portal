package gov.usgs.cida.gdp.wps.generator;

import gov.usgs.cida.gdp.wps.binding.ZipFileBinding;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.datahandler.generator.AbstractGenerator;

/**
 *
 * @author tkunicki
 */
public class ZipGenerator extends AbstractGenerator {

    public ZipGenerator() {
        supportedIDataTypes.add(ZipFileBinding.class);
    }
    
    @Override
    public InputStream generateStream(IData data, String mimeType, String schema) throws IOException {
        if (data instanceof ZipFileBinding) {
            Object payload = data.getPayload();
            if (payload instanceof File) {
                File payloadFile = (File) payload;
                return new FileInputStream(payloadFile);
            }
        }
        return null;
    }

}
