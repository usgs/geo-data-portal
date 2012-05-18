package gov.usgs.derivative;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tkunicki
 */
public abstract class DaysAgainstThresholdVisitor extends AnnualDerivativeVisitor {
    
    private final static Logger LOGGER = LoggerFactory.getLogger(DaysAgainstThresholdVisitor.class);

    @Override
    protected final int getInputGridCount() {
        return 1;
    }
    
    protected abstract class DaysAgainstThresholdKernel extends AnnualDerivativeKernel {
        
        public DaysAgainstThresholdKernel(int yxCount, float[] thresholds) {
            super(getInputGridCount(), yxCount, thresholds);
        }

        @Override
        public void run() {
            float value = k_getTYXInputValue(0);
            int zyxOutputIndex = k_getZYXOutputIndex();
            if (value == value) {
                if (k_includeValue(k_getZValue(), value)) {
                    k_zyxOutputValues[zyxOutputIndex] = k_zyxOutputValues[zyxOutputIndex] + 1;
                }
            }
        }
        
        public abstract boolean k_includeValue(float threshold, float value);
    }
        
}
