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
    protected final int requiredInputGridCount() {
        return 1;
    }
        
    protected abstract class RunAgainstThresholdKernel extends AnnualDerivativeKernel {

        protected final float[] zyxTempValues;
        
        public RunAgainstThresholdKernel(int yxCount, float[] thresholds) {
            super(requiredInputGridCount(), yxCount, thresholds);
            
            zyxTempValues = new float[zyxOutputCount];
        }

        @Override
        public void preExecute() {
            super.preExecute();
            Arrays.fill(zyxTempValues, 0);
            put(zyxTempValues);
        }

        @Override
        public void run() {
            float value = k_getTYXInputValue(0);
            int zyxOutputIndex = k_getZYXOutputIndex();
            if (value == value) {
                if (k_includeValue(k_getZValue(), value)) {
                    float current = zyxTempValues[zyxOutputIndex] + 1;
                    float maximum = zyxOutputValues[zyxOutputIndex];
                    if (current > maximum) {
                        zyxOutputValues[zyxOutputIndex] = current;
                    }
                    zyxTempValues[zyxOutputIndex] = current;
                } else {
                    zyxTempValues[zyxOutputIndex] = 0;
                }
            }
        }
        
        public abstract boolean k_includeValue(float threshold, float value);
        
    }
    
}
