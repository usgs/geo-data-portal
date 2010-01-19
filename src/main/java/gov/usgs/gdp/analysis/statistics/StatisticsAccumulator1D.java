package gov.usgs.gdp.analysis.statistics;

/**
 *
 * @author tkunicki
 */
public class StatisticsAccumulator1D {

    private long count;
    private double mean;
    private double m2;
    private double minimum;
    private double maximum;

    public StatisticsAccumulator1D() {

        count = 0;

        // initialize values as required by incremental and pairwise
        // mean and C2 algorithm specified by:
        //
        // B. P. Welford. ``Note on a Method for Calculating Corrected Sums of
        // Squares and Products''. Technometrics, Vol. 4, No. 3 (Aug., 1962),
        // p. 419-420.
        //
        // NOTE: C2 isn't used here as it's a term for covariance calculation
        //       on multidimenstional data..
        //
        mean = 1d;
        m2 = 0d;
        minimum = Double.MAX_VALUE;
        maximum = -Double.MAX_VALUE;
    }

    public void accumulate(double value) {

        // need to store prior mean for incremental mean, M2 and C2 calculations
        double mean_last = mean;

        // count must be updated for incremental mean, M2 and C2 calcs.
        ++count;

        // new mean must be calculated before incremental M2.
        mean = StatisticsAccumulatorUtililty.incrementalMean(mean_last, value, count);

        m2 = StatisticsAccumulatorUtililty.incrementalM2(m2, mean_last, value, mean);

        if(value < minimum) {
            minimum = value;
        }

        if(value > maximum) {
            maximum = value;
        }
    }

    public void accumulate(StatisticsAccumulator1D sa) {
        if(sa != null && count > 0 || sa.count > 0) {

            m2 = StatisticsAccumulatorUtililty.pairwiseM2(m2, mean, count, sa.m2, sa.mean, sa.count);

            // must do this after M2 and C2 pairwise operations as we need unaccumulated mean
            mean = StatisticsAccumulatorUtililty.pairwiseMean(mean, count, sa.mean, sa.count);

            // this needs to occur after all pairwise operations as they require unaccumulated count
            count += sa.count;

            if(sa.minimum < minimum) {
                minimum = sa.minimum;
            }
            
            if(sa.maximum > maximum) {
                maximum = sa.maximum;
            }
        }
    }

    public long getCount() {
        return count;
    }

    public double getMean() {
        return mean;
    }

    public double getM2() {
        return m2;
    }

    public double getVariance() {
        return m2 / (double) count;
    }

    public double getStandardDeviation() {
        return Math.sqrt(getVariance());
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
        sb.append("  variance           : ").append(getVariance()).append('\n');
        sb.append("  standard deviation : ").append(getStandardDeviation()).append('\n');
        return sb.toString();
    }

}
