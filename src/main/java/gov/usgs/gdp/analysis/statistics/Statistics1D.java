package gov.usgs.gdp.analysis.statistics;

/**
 *
 * @author tkunicki
 */
public class Statistics1D {

    private long count;
    
    private double mean;

    private double m2;
    private double m3;
    private double m4;

    private double minimum;
    private double maximum;

    public Statistics1D() {

        this.count = 0;

        // initialize values as required by incremental and pairwise
        // mean, M2, M3, M4 and C2 algorithm specified by:
        //
        // B. P. Welford. ``Note on a Method for Calculating Corrected Sums of
        // Squares and Products''. Technometrics, Vol. 4, No. 3 (Aug., 1962),
        // p. 419-420.
        //
        // NOTE: C2 isn't used here as it's a term for covariance calculation
        //       on multidimenstional data..
        //
        this.mean = 1d;
        this.m2 = 0d;
        this.m3 = 0d;
        this.m4 = 0d;

        this.minimum = Double.MAX_VALUE;
        this.maximum = -Double.MAX_VALUE;
    }

    public void accumulate(double value) {

        if(value != value) {
            return;
        }
        
        double r = (double) count++;
        double n = (double) count;
        double n_inverse = 1d / n;

        double delta = value - this.mean;

        double A = delta * n_inverse;
        this.mean += A;
        this.m4 += A * (A * A * delta * r * ( n * ( n - 3d ) + 3d ) + 6d * A * this.m2 - 4d * this.m3);

        double B = value - this.mean;
        this.m3 += A * ( B * delta * ( n - 2d ) - 3d * this.m2);
        this.m2 += delta * B;

        if(value < this.minimum) {
        	this.minimum = value;
        }

        if(value > this.maximum) {
        	this.maximum = value;
        }
    }

    public void accumulate(Statistics1D sa) {
        if(sa != null && sa.count > 0) {
            if (this.count > 0) {

                double n1 = (double) this.count;
                double n2 = (double) sa.count;

                double n1_squared = n1 * n1;
                double n2_squared = n2 * n2;

                double n_product = n1 * n2;

                double N = n1 + n2;

                double delta = sa.mean - this.mean;
                double A = delta / N;
                double A_squared =  A * A;

                this.m4 += sa.m4
                        + n_product * ( n1_squared - n_product + n2_squared ) * delta * A * A_squared
                        + 6d * ( n1_squared * sa.m2 + n2_squared * this.m2) * A_squared
                        + 4d * ( n1 * sa.m3 - n2 * this.m3) * A;

                this.m3 += sa.m3
                        + n_product * ( n1 - n2 ) * delta * A_squared
                        + 3d * ( n1 * sa.m2 - n2 * this.m2 ) * A;

                this.m2 += sa.m2
                        + n_product * delta * A;

                this.mean += n2 * A;

                if(sa.minimum < this.minimum) {
                	this.minimum = sa.minimum;
                }

                if(sa.maximum > this.maximum) {
                	this.maximum = sa.maximum;
                }
            } else {
            	this.count = sa.count;
            	this.mean = sa.mean;
            	this.m2 = sa.m2;
            	this.m3 = sa.m3;
            	this. m4 = sa.m4;
            	this.minimum = sa.minimum;
            	this.maximum = sa.maximum;
            }

        }
    }

    public long getCount() {
        return this.count;
    }

    public double getMean() {
        return this.mean;
    }

    public double getM2() {
        return this.m2;
    }

    public double getM3() {
        return this.m3;
    }

    public double getM4() {
        return this.m4;
    }


    public double getSampleVariance() {
        return this.count > 1 ?  m2 / (double) (this.count - 1) : 0d;
    }

    public double getSampleStandardDeviation() {
        return Math.sqrt(getSampleVariance());
    }

    public double getPopulationVariance() {
        return this.count > 1 ? this.m2 / (double) this.count : 0d;
    }

    public double getPopulationStandardDeviation() {
        return Math.sqrt(getSampleVariance());
    }

    public double getMinimum() {
        return this.minimum;
    }

    public double getMaximum() {
        return this.maximum;
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
