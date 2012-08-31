package gov.usgs.cida.gdp.wps.parser;

import gov.usgs.cida.gdp.io.data.ZippedGenericFileData;
import gov.usgs.cida.gdp.io.data.ZippedGenericFileDataBinding;
import java.io.InputStream;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.datahandler.parser.AbstractParser;

/**
 *
 * @author isuftin
 */
public class BinZippedBase64SHPParser extends AbstractParser {

    public BinZippedBase64SHPParser() {
        supportedIDataTypes.add(ZippedGenericFileDataBinding.class);
    }

    @Override
    public IData parse(InputStream input, String mimeType, String encoding) {
        return new ZippedGenericFileDataBinding(new ZippedGenericFileData(input, mimeType));
    }
}
