package gov.usgs.cida.derivative;

/**
 *
 * @author tkunicki
 */
public abstract class DaysBelowThresholdVisitor extends DaysAgainstThresholdVisitor {

    @Override
    public final boolean includeValue(double threshold, double value) {
        return value < threshold;
    }
    
}
