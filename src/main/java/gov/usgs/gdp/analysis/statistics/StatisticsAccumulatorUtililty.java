package gov.usgs.gdp.analysis.statistics;

/**
 *
 * @author tkunicki
 */
public class StatisticsAccumulatorUtililty {

    public final static double pairwiseMean(
            double mean_1,
            double count_1,
            double mean_2,
            double count_2)
    {
        return mean_1 + count_2 * (mean_2 - mean_1) / (count_1 + count_2);
    }

    public final static double incrementalMean(
           double mean_last,
           double value_new,
           double count_new)
    {
        return mean_last + (value_new - mean_last) / (count_new);
    }

    public final static double pairwiseM2(
            double m2_1,
            double mean_1,
            double count_1,
            double m2_2,
            double mean_2,
            double count_2)
    {
        double mean_delta = mean_2 - mean_1;
        return m2_1 + m2_2 + mean_delta * mean_delta * count_1 * count_2 / (count_1 + count_2);
    }

    public final static double incrementalM2(
           double m2_last,
           double mean_last,
           double value_new,
           double mean_new)
    {
        return  m2_last + (value_new - mean_last) * (value_new - mean_new);
    }

    public final static double pairwiseC2(
            double c2_1,
            double mean1_1,
            double mean2_1,
            double count_1,
            double c2_2,
            double mean1_2,
            double mean2_2,
            double count_2)
    {
        return c2_1 + c2_2 + count_1 * count_2 / (count_1 + count_2) * (mean1_2 - mean1_1) * (mean2_2 - mean2_1);
    }

    public final static double incrementalC2(
            double c2_last,
            double mean1_last,
            double mean2_last,
            double value1_new,
            double value2_new,
            double count_new)
    {
        return c2_last + (count_new - 1d) / (count_new) * (value1_new - mean1_last) * (value2_new - mean2_last);
    }

}
