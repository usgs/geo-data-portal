package gov.usgs.cida.gdp.wps.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.n52.wps.io.IOUtils;
import org.n52.wps.io.data.GenericFileData;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GenericFileDataBinding;

/**
 *
 * @author isuftin
 */
public class BinZippedBase64SHPParser extends AbstractBinZippedSHPParser {

    public BinZippedBase64SHPParser() {
        super();
    }

    /**
     * @throws RuntimeException
     *             if an error occurs while writing the stream to disk or
     *             unzipping the written file
     * @see org.n52.wps.io.IParser#parse(java.io.InputStream)
     */
    @Override
    public IData parse(InputStream input, String mimeType) throws RuntimeException {
        try {
            File zipped = IOUtils.writeBase64ToFile(input, "zip");
            return new GenericFileDataBinding(new GenericFileData(new FileInputStream(zipped), mimeType));
        } catch (IOException e) {
            throw new RuntimeException(
                    "An error has occurred while accessing provided data", e);
        }
    }
}
