package gov.usgs.derivative;

/**
 *
 * @author tkunicki
 */
public abstract class RunAboveThresholdVisitor extends RunAgainstThresholdVisitor {

    @Override
    protected RunAboveThresholdKernel createKernel(int yxCount, float[] thresholds) {
        return new RunAboveThresholdKernel(yxCount, thresholds);
    }

    protected class RunAboveThresholdKernel extends RunAgainstThresholdKernel {

        public RunAboveThresholdKernel(int yxCount, float[] thresholds) {
            super(yxCount, thresholds);
        }

        @Override
        public final boolean k_includeValue(float threshold, float value) {
            return value > threshold;
        }
    }
    
}
