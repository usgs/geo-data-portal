package gov.usgs.cida.gdp.io.data;

import org.n52.wps.io.data.GenericFileData;
import org.n52.wps.io.data.binding.complex.GenericFileDataBinding;

/**
 * @author isuftin
 */
public class ZippedGenericFileDataBinding extends GenericFileDataBinding {

    public ZippedGenericFileDataBinding(ZippedGenericFileData fileData) {
        super(fileData);
    }

    /**
     * 
     * @return
     */
    @Override
    public GenericFileData getPayload() {
        return  super.getPayload();
    }
}
