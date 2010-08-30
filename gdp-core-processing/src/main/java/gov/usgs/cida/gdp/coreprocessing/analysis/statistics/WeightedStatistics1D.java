package gov.usgs.cida.gdp.coreprocessing.analysis.statistics;

/**
 *
 * @author tkunicki
 */
public class WeightedStatistics1D {

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

    public long getCount() {
        return count;
    }
    
    public double getWeightSum() {
        return weightSum;
    }

    public double getMean() {
        return mean;
    }


    public double getSampleVariance() {
        return count > 1 ? S * (double) count / ( (double) (count - 1) * weightSum) : 0d;
    }

    public double getSampleStandardDeviation() {
        return Math.sqrt(getSampleVariance());
    }

    public double getPopulationVariance() {
        return weightSum > 0d ? S / weightSum : 0d;
    }

    public double getPopulationStandardDeviation() {
        return Math.sqrt(getPopulationVariance());
    }

    public double getMinimum() {
        return minimum;
    }

    public double getMaximum() {
        return maximum;
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
