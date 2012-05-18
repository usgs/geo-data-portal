package gov.usgs.derivative;

import com.google.common.base.Joiner;
import java.util.List;
import ucar.nc2.dt.GridDatatype;

/**
 *
 * @author tkunicki
 */
public abstract class AnnualDegreeDayVisitor extends AnnualDerivativeVisitor {

    @Override
    protected String generateDerivativeOutputVariableName(List<GridDatatype> gridDatatypeList, String derivative) {
        // NOTE:  assumes input grid variable naming convention:
        String gridName = gridDatatypeList.get(0).getName();
        gridName = gridName.substring(0, gridName.lastIndexOf("_"));
        return Joiner.on("-").join(
                gridName,
                derivative);
    }
    
    @Override
    protected final int getInputGridCount() {
        return 2;
    }
    
    protected abstract class AnnualDegreeDayKernel extends AnnualDerivativeKernel {
        
        public AnnualDegreeDayKernel(int yxCount, float[] thresholds) {
            super(getInputGridCount(), yxCount, thresholds);
        }

        @Override
        public void run() {
            float tempMin = k_getTYXInputValue(0);
            float tempMax = k_getTYXInputValue(1);
            float threshold = k_getZValue();
            // HACK, need to filter bogus data with some Hayhoe data
            if (tempMin == tempMin && tempMax == tempMax) {
                float zyxDegreeDays = k_degreeDays(threshold, tempMin, tempMax);
                int zyxOutputIndex = k_getZYXOutputIndex();
                k_zyxOutputValues[zyxOutputIndex] = k_zyxOutputValues[zyxOutputIndex] + zyxDegreeDays;
            }
        }
        
        public abstract float k_degreeDays(float threshold, float tempMin, float tempMax);
        
    }
    
}
