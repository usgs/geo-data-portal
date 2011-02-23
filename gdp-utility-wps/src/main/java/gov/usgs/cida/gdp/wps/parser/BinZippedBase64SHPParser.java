package gov.usgs.cida.gdp.wps.parser;

import java.io.InputStream;
import org.apache.commons.codec.binary.Base64InputStream;
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
     * @see org.n52.wps.io.IParser#parse(java.io.InputStream)
     */
    @Override
    public IData parse(InputStream input, String mimeType) {
        return new GenericFileDataBinding(new GenericFileData(new Base64InputStream(input), mimeType));
    }
}
