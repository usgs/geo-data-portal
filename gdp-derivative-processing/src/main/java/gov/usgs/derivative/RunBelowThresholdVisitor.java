package gov.usgs.derivative;

/**
 *
 * @author tkunicki
 */
public abstract class RunBelowThresholdVisitor extends RunAgainstThresholdVisitor {

    @Override
    protected RunBelowThresholdKernel createKernel(int yxCount, float[] thresholds) {
        return new RunBelowThresholdKernel(yxCount, thresholds);
    }

    protected class RunBelowThresholdKernel extends RunAgainstThresholdKernel {

        public RunBelowThresholdKernel(int yxCount, float[] thresholds) {
            super(yxCount, thresholds);
        }

        @Override
        public final boolean k_includeValue(float threshold, float value) {
            return value < threshold;
        }
    }
    
}
