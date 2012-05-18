package gov.usgs.derivative;

import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tkunicki
 */
public abstract class RunAgainstThresholdVisitor extends AnnualDerivativeVisitor {
    
    private final static Logger LOGGER = LoggerFactory.getLogger(RunAgainstThresholdVisitor.class);
    
    @Override
    protected final int getInputGridCount() {
        return 1;
    }
        
    protected abstract class RunAgainstThresholdKernel extends AnnualDerivativeKernel {

        protected final float[] k_zyxTempValues;
        
        public RunAgainstThresholdKernel(int yxCount, float[] thresholds) {
            super(getInputGridCount(), yxCount, thresholds);
            
            k_zyxTempValues = new float[zyxOutputCount];
        }

        @Override
        public void preExecute() {
            super.preExecute();
            Arrays.fill(k_zyxTempValues, 0);
            put(k_zyxTempValues);
        }

        @Override
        public void run() {
            float value = k_getTYXInputValue(0);
            int zyxOutputIndex = k_getZYXOutputIndex();
            if (value == value) {
                if (k_includeValue(k_getZValue(), value)) {
                    float current = k_zyxTempValues[zyxOutputIndex] + 1;
                    float maximum = k_zyxOutputValues[zyxOutputIndex];
                    if (current > maximum) {
                        k_zyxOutputValues[zyxOutputIndex] = current;
                    }
                    k_zyxTempValues[zyxOutputIndex] = current;
                } else {
                    k_zyxTempValues[zyxOutputIndex] = 0;
                }
            }
        }
        
        public abstract boolean k_includeValue(float threshold, float value);
        
    }
    
}
