package gov.usgs.cida.gdp.coreprocessing.analysis.statistics;

/**
 *
 * @author tkunicki
 */
public interface IStatistics1D {

    long getCount();

    double getSum();

    double getMean();

    double getSampleVariance();

    double getSampleStandardDeviation();

    double getPopulationVariance();

    double getPopulationStandardDeviation();

    double getMinimum();

    double getMaximum();

}
