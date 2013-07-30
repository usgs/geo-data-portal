package gov.usgs.derivative;

/**
 *
 * @author tkunicki
 */
public abstract class DaysAboveThresholdVisitor extends DaysAgainstThresholdVisitor {
    
    public DaysAboveThresholdVisitor(String outputDir) {
        super(outputDir);
    }

    @Override
    protected DaysAboveThresholdKernel createKernel(int yxCount, float[] thresholds) {
        return new DaysAboveThresholdKernel(yxCount, thresholds);
    }

    protected class DaysAboveThresholdKernel extends DaysAgainstThresholdKernel {

        public DaysAboveThresholdKernel(int yxCount, float[] thresholds) {
            super(yxCount, thresholds);
        }

        @Override
        public final boolean k_includeValue(float threshold, float value) {
            return value > threshold;
        }
    }
  
}
