package gov.usgs.derivative;

/**
 *
 * @author tkunicki
 */
public abstract class DaysBelowThresholdVisitor extends DaysAgainstThresholdVisitor {

    public DaysBelowThresholdVisitor(String outputDir) {
        super(outputDir);
    }
    
    @Override
    protected DaysBelowThresholdKernel createKernel(int yxCount, float[] thresholds) {
        return new DaysBelowThresholdKernel(yxCount, thresholds);
    }

    protected class DaysBelowThresholdKernel extends DaysAgainstThresholdKernel {

        public DaysBelowThresholdKernel(int yxCount, float[] thresholds) {
            super(yxCount, thresholds);
        }

        @Override
        public final boolean k_includeValue(float threshold, float value) {
            return value < threshold;
        }
    }
    
}
