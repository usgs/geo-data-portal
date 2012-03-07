package gov.usgs.cida.gdp.coreprocessing.analysis.statistics;

/**
 *
 * @author tkunicki
 */
public class WeightedStatistics1D implements IStatistics1D {

    private long count;

    private double weightSum;
    
    private double mean;

    private double S;

    private double minimum;
    private double maximum;

    public WeightedStatistics1D() {

        count = 0;

        weightSum = 0;

        mean = 0d;

        S = 0d;

        minimum = Double.MAX_VALUE;
        maximum = -Double.MAX_VALUE;
    }

    public void accumulate(double value, double weight) {

        // make sure we don't have NaN and weight > 0
        if( value != value || weight <= 0) {
            return;
        }

        doIncrementalUpdate(value, weight);
        ++count;

        if(value < minimum) {
            minimum = value;
        }

        if(value > maximum) {
            maximum = value;
        }
    }

    private void doIncrementalUpdate(double value, double weight) {
        double temp = weightSum + weight;
        double Q = value - mean;
        double R = Q * weight / temp;
        S +=  weightSum * Q * R;
        mean += R;
        weightSum = temp;
    }

    public void accumulate(WeightedStatistics1D wsa) {
        if (wsa != null && wsa.count > 0) {
            if (count > 0) {
                // need to derive this...
                throw new UnsupportedOperationException();
            } else {
                count = wsa.count;
                weightSum = wsa.weightSum;
                mean = wsa.mean;
                S = wsa.S;
                minimum = wsa.minimum;
                maximum = wsa.maximum;
            }
        }
    }

    @Override
    public long getCount() {
        return count;
    }

    @Override
    public double getWeightSum() {
        return weightSum;
    }

    @Override
    public double getMean() {
        return count > 0 ? mean : Double.NaN;
    }

    @Override
    public double getSampleVariance() {
        return count > 1 ? S * (double) count / ((double) (count - 1) * weightSum) : Double.NaN;
    }

    @Override
    public double getSampleStandardDeviation() {
        return Math.sqrt(getSampleVariance());
    }

    @Override
    public double getPopulationVariance() {
        return weightSum > 0d ? S / weightSum : Double.NaN;
    }

    @Override
    public double getPopulationStandardDeviation() {
        return Math.sqrt(getPopulationVariance());
    }

    @Override
    public double getMinimum() {
        return count > 0 ? minimum : Double.NaN;
    }

    @Override
    public double getMaximum() {
        return count > 0 ? maximum : Double.NaN;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Statistics Accumulator1D ").append(System.identityHashCode(this)).append('\n');
        sb.append("  count              : ").append(getCount()).append('\n');
        sb.append("  mean               : ").append(getMean()).append('\n');
        sb.append("  minimum            : ").append(getMinimum()).append('\n');
        sb.append("  maximum            : ").append(getMaximum()).append('\n');
        sb.append("  variance           : ").append(getSampleVariance()).append('\n');
        sb.append("  standard deviation : ").append(getSampleStandardDeviation()).append('\n');
        return sb.toString();
    }

}
