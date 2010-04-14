package gov.usgs.gdp.analysis;

import gov.usgs.gdp.analysis.statistics.WeightedStatisticsAccumulator1D;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.Formatter;
import java.util.List;
import java.util.Map;

public class GridStatisticsCSVWriter implements GridStatisticsWriter {

    public enum Statistic {

        mean("%.7g", "%s"),
        minimum("%.7g", "%s"),
        maximum("%.7g", "%s"),
        variance("%.7g", "%s^2"),
        std_dev("%.7g", "%s"),
        weight_sum("%.7g"),
        count("%d");

        private final String valueFormat;
        private final String unitFormat;

        Statistic(String format) {
            this(format, null);
        }
        Statistic(String format, String unitFormat) {
            this.valueFormat = format;
            this.unitFormat = unitFormat;
        }

        public String getValueFormat() {
            return valueFormat;
        }

        public String getUnitFormat() {
            return unitFormat;
        }

        public boolean getNeedsUnits() {
            return unitFormat != null;
        }

        public Number getValue(WeightedStatisticsAccumulator1D wsa) {
            switch (this) {
                case mean:
                    return wsa.getMean();
                case minimum:
                    return wsa.getMinimum();
                case maximum:
                    return wsa.getMaximum();
                case variance:
                    return wsa.getSampleVariance();
                case std_dev:
                    return wsa.getSampleStandardDeviation();
                case weight_sum:
                    return wsa.getWeightSum();
                case count:
                default:
                    return wsa.getCount();
            }
        }
    }

    private GridStatistics gridStatistics;
    private List<Statistic> statisticList;
    private boolean groupByStatistic;
    private String delimiter;

    public GridStatisticsCSVWriter(GridStatistics gridStatistics) {
        this(gridStatistics, Arrays.asList(Statistic.values()), false, ",");
    }

    public GridStatisticsCSVWriter(GridStatistics gridStatistics, List<Statistic> statisticList, boolean groupByStatistic, String delimiter) {
        this.gridStatistics = gridStatistics;
        this.statisticList = statisticList;
        this.groupByStatistic = groupByStatistic;
        this.delimiter = delimiter;
    }

    @Override
    public void write(BufferedWriter writer) throws IOException {
        writerHeader(writer);
        writerBody(writer);
        writerFooter(writer);
    }

    private void writerHeader(BufferedWriter writer) throws IOException {
        String units = gridStatistics.getVariableUnits();

        int sCount = statisticList.size();
        String[] statisticLabel = new String[sCount];
        if (units != null && units.length() > 0) {
            for(int sIndex = 0; sIndex < sCount; ++sIndex) {
                Statistic field = statisticList.get(sIndex);
                statisticLabel[sIndex] = field.getNeedsUnits() ?
                    field.name() + "(" + String.format(field.getUnitFormat(), units) + ")" :
                    field.name();
            }
        } else {
            for(int sIndex = 0; sIndex < sCount; ++sIndex) {
                statisticLabel[sIndex] = statisticList.get(sIndex).name();
            }
        }

        int aCount = gridStatistics.getAttributeValues().size();
        String attributeLabel[] = new String[aCount];
        for (int aIndex = 0; aIndex < aCount; ++aIndex) {
            attributeLabel[aIndex] = gridStatistics.getAttributeValues().get(aIndex).toString();
        }

        StringBuilder lineSB = new StringBuilder();
        if (groupByStatistic) {
            for (int sIndex = 0; sIndex < sCount; ++sIndex) {
                for (int aIndex = 0; aIndex < aCount; ++aIndex ) {
                    lineSB.append(delimiter).append(attributeLabel[aIndex]);
                }
                lineSB.append(delimiter).append("ALL");
            }
        } else {
            for (int aIndex = 0; aIndex < aCount; ++aIndex )
                for (int sIndex = 0; sIndex < sCount; ++sIndex) {{
                    lineSB.append(delimiter).append(attributeLabel[aIndex]);
                }
            }
            for (int sIndex = 0; sIndex < statisticLabel.length; ++sIndex) {
                lineSB.append(delimiter).append("ALL");
            }
        }
        writer.write(lineSB.toString());
        writer.newLine();

        lineSB.setLength(0);
        lineSB.append("timestep");
        if (groupByStatistic) {
            for (int sIndex = 0; sIndex < sCount; ++sIndex) {
                // +1 for ALL
                for (int aIndex = 0; aIndex < aCount + 1; ++ aIndex) {
                    lineSB.append(delimiter).append(statisticLabel[sIndex]);
                }
            }
        } else {
            // +1 for ALL
            for (int aIndex = 0; aIndex < aCount + 1; ++ aIndex) {
                for (int sIndex = 0; sIndex < sCount; ++sIndex) {
                    lineSB.append(delimiter).append(statisticLabel[sIndex]);
                }
            }
        }
        writer.write(lineSB.toString());
        writer.newLine();
    }

    @SuppressWarnings("deprecation")
    private void writerBody(BufferedWriter writer) throws IOException {

        StringBuilder lineSB = new StringBuilder();
        Formatter formatter = new Formatter(lineSB);

        for (Map.Entry<Date, Map<Object, WeightedStatisticsAccumulator1D>> timesetEntry : gridStatistics.getPerTimestepPerAttributeValueStatistics().entrySet()) {

            Date timestep = timesetEntry.getKey();
            Map<Object, WeightedStatisticsAccumulator1D> timestepMap = timesetEntry.getValue();
            WeightedStatisticsAccumulator1D timestepSummary = gridStatistics.getPerTimestepAllAttributeValueStatistics().get(timestep);

            lineSB.setLength(0);
            lineSB.append(timestep.toGMTString()); // deprecated, figure out better way later.

            if (groupByStatistic) {
                for (Statistic field : statisticList) {
                    for (Map.Entry<Object, WeightedStatisticsAccumulator1D> attributeEntry : timestepMap.entrySet()) {
                        lineSB.append(delimiter);
                        formatter.format(field.getValueFormat(), field.getValue(attributeEntry.getValue()));
                    }
                    // value for ALL features across timestep
                    lineSB.append(delimiter);
                    formatter.format(field.getValueFormat(), field.getValue(timestepSummary));
                }
            } else {
                for (Map.Entry<Object, WeightedStatisticsAccumulator1D> attributeEntry : timestepMap.entrySet()) {
                    for (Statistic field : statisticList) {
                        lineSB.append(delimiter);
                        formatter.format(field.getValueFormat(), field.getValue(attributeEntry.getValue()));
                    }
                }
                // value for ALL features across timestep
                for (Statistic field : statisticList) {
                    lineSB.append(delimiter);
                    formatter.format(field.getValueFormat(), field.getValue(timestepSummary));
                }
            }

            writer.write(lineSB.toString());
            writer.newLine();
        }
    }

    private void writerFooter(BufferedWriter writer) throws IOException {
        // SUMMARY STUFF for ALL timesteps
        StringBuilder lineSB = new StringBuilder();
        Formatter formatter = new Formatter(lineSB);

        WeightedStatisticsAccumulator1D allSummary = gridStatistics.getAllTimestepAllAttributeValueStatistics();

        lineSB.append("ALL");
        if (groupByStatistic) {
            for (Statistic field : statisticList) {
                for (Object attributeValue : gridStatistics.getAttributeValues()) {
                    WeightedStatisticsAccumulator1D attributeSummary = gridStatistics.getPerAttributeValueAllTimestepStatistics().get(attributeValue);
                    lineSB.append(delimiter);
                    formatter.format(field.getValueFormat(), field.getValue(attributeSummary));
                }
                lineSB.append(delimiter);
                formatter.format(field.getValueFormat(), field.getValue(allSummary));
            }
        } else {
            for (Object attributeValue : gridStatistics.getAttributeValues()) {
                WeightedStatisticsAccumulator1D attributeSummary = gridStatistics.getPerAttributeValueAllTimestepStatistics().get(attributeValue);
                for (Statistic field : statisticList) {
                    lineSB.append(delimiter);
                    formatter.format(field.getValueFormat(), field.getValue(attributeSummary));
                }
            }
            for (Statistic field : statisticList) {
                lineSB.append(delimiter);
                formatter.format(field.getValueFormat(), field.getValue(allSummary));
            }
        }

        writer.write(lineSB.toString());
        writer.newLine();

    }

}
