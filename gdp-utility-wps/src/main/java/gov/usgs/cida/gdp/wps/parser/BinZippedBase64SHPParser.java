package gov.usgs.cida.gdp.wps.parser;

import gov.usgs.cida.gdp.io.data.ZippedGenericFileData;
import gov.usgs.cida.gdp.io.data.ZippedGenericFileDataBinding;
import java.io.InputStream;
import org.apache.commons.codec.binary.Base64InputStream;
import org.n52.wps.io.data.IData;

/**
 *
 * @author isuftin
 */
public class BinZippedBase64SHPParser extends AbstractBinZippedSHPParser {

    public BinZippedBase64SHPParser() {
        super();
    }

    /**
     * @param input 
     * @param mimeType 
     * @return 
     * @see org.n52.wps.io.IParser#parse(java.io.InputStream)
     */
    @Override
    public IData parse(InputStream input, String mimeType) {
        return new ZippedGenericFileDataBinding(new ZippedGenericFileData(new Base64InputStream(input), mimeType));
    }
}
