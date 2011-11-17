package gov.usgs.cida.derivative;

/**
 *
 * @author tkunicki
 */
public abstract class RunAboveThresholdVisitor extends RunAgainstThresholdVisitor {

    @Override
    public boolean includeValue(double threshold, double value) {
        return value >= threshold;
    }
    
}
