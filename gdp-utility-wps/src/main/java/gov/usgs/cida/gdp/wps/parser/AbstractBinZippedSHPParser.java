package gov.usgs.cida.gdp.wps.parser;

import org.n52.wps.io.data.binding.complex.GenericFileDataBinding;
import org.n52.wps.io.datahandler.binary.AbstractBinaryParser;

/**
 *
 * @author isuftin
 */
public abstract class AbstractBinZippedSHPParser extends AbstractBinaryParser {

    @Override
	public Class<?>[] getSupportedInternalOutputDataType() {
		Class[] supportedClasses = {GenericFileDataBinding.class};
		return supportedClasses;
	}

}
