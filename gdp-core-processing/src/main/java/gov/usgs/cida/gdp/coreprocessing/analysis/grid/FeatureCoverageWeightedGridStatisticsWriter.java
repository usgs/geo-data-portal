package gov.usgs.cida.gdp.coreprocessing.analysis.grid;

import gov.usgs.cida.gdp.coreprocessing.analysis.statistics.WeightedStatistics1D;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class FeatureCoverageWeightedGridStatisticsWriter {
    
    public final static String ALL_ATTRIBUTES_LABEL = "ALL ATTRIBUTES";
    public final static String ALL_TIMESTEPS_LABEL = "ALL TIMESTEPS";
    public final static String TIMESTEPS_LABEL = "TIMESTEP";

    public enum Statistic {

        MEAN("%f", "%s")
        { @Override public Number getValue(WeightedStatistics1D wsa) { return (float)wsa.getMean(); } },
        MINIMUM("%f", "%s")
        { @Override public Number getValue(WeightedStatistics1D wsa) { return (float)wsa.getMinimum(); } },
        MAXIMUM("%f", "%s")
        { @Override public Number getValue(WeightedStatistics1D wsa) { return (float)wsa.getMaximum(); } },
        VARIANCE("%f", "%s^2")
        { @Override public Number getValue(WeightedStatistics1D wsa) { return (float)wsa.getSampleVariance(); } },
        STD_DEV("%f", "%s")
        { @Override public Number getValue(WeightedStatistics1D wsa) { return (float)wsa.getSampleStandardDeviation(); } },
        WEIGHT_SUM("%f")
        { @Override public Number getValue(WeightedStatistics1D wsa) { return (float)wsa.getWeightSum(); } },
        COUNT("%d")
        { @Override public Number getValue(WeightedStatistics1D wsa) { return wsa.getCount(); } };

        private final String valueFormat;
        private final String unitFormat;

        Statistic(String format) { this(format, null); }
        Statistic(String format, String unitFormat) {
            this.valueFormat = format;
            this.unitFormat = unitFormat;
        }
        public String getValueFormat() { return valueFormat; }
        public String getUnitFormat() {  return unitFormat; }
        public boolean getNeedsUnits() { return unitFormat != null && unitFormat.length() > 0; }
        public abstract Number getValue(WeightedStatistics1D wsa);
    }

    private final List<Object> attributeList;
    private final String variableName;
    private final String variableUnits;
    private final List<Statistic> statisticList;
    private final boolean groupByStatistic;
    private final String delimiter;
    private final boolean summarizeTimeStep;
    private final boolean summarizeFeatureAttribute;
    private final BufferedWriter writer;

    private final StringBuilder lineSB;

    public FeatureCoverageWeightedGridStatisticsWriter(
            List<Object> attributeList,
            String variableName,
            String variableUnits,
            List<Statistic> statisticList,
            boolean groupByStatistic,
            String delimiter,
            BufferedWriter writer) {
        this(attributeList,variableName,variableUnits,statisticList,groupByStatistic,delimiter, false, false, writer);
    }
    
    public FeatureCoverageWeightedGridStatisticsWriter(
            List<Object> attributeList,
            String variableName,
            String variableUnits,
            List<Statistic> statisticList,
            boolean groupByStatistic,
            String delimiter,
            boolean summarizeTimeStep,
            boolean summarizeFeatureAttribute,
            BufferedWriter writer) {

        this.attributeList = Collections.unmodifiableList(attributeList);
        this.variableName = variableName;
        this.variableUnits = variableUnits;
        this.statisticList = Collections.unmodifiableList(statisticList);
        this.groupByStatistic = groupByStatistic;
        this.delimiter = delimiter;
        this.summarizeTimeStep = summarizeTimeStep;
        this.summarizeFeatureAttribute = summarizeFeatureAttribute;
        this.writer = writer;

        lineSB = new StringBuilder();

    }
    
    public boolean isSummarizeTimeStep() {
        return summarizeTimeStep;
    }
    
    public boolean isSummarizeFeatureAttribute() {
        return summarizeFeatureAttribute;
    }

    public void writerHeader(Collection<String> rowLabels) throws IOException {

        int sCount = statisticList.size();
        String[] statisticLabel = new String[sCount];
        if (variableUnits != null && variableUnits.length() > 0) {
            for(int sIndex = 0; sIndex < sCount; ++sIndex) {
                Statistic field = statisticList.get(sIndex);
                statisticLabel[sIndex] = field.getNeedsUnits() ?
                    field.name() + "(" + String.format(field.getUnitFormat(), variableUnits) + ")" :
                    field.name();
            }
        } else {
            for(int sIndex = 0; sIndex < sCount; ++sIndex) {
                statisticLabel[sIndex] = statisticList.get(sIndex).name();
            }
        }

        int aCount = attributeList.size();
        String attributeLabel[] = new String[aCount];
        for (int aIndex = 0; aIndex < aCount; ++aIndex) {
            attributeLabel[aIndex] = attributeList.get(aIndex).toString();
        }

        lineSB.setLength(0);
        if (rowLabels != null && rowLabels.size() > 0) {
            // pad with delimieters to align headers.
            int rowLabelCount = rowLabels.size();
            for (int rowLabelIndex = 1; rowLabelIndex < rowLabelCount; ++rowLabelIndex ) {
                lineSB.append(delimiter);
            }
        }
        if (groupByStatistic) {
            for (int sIndex = 0; sIndex < sCount; ++sIndex) {
                for (int aIndex = 0; aIndex < aCount; ++aIndex ) {
                    lineSB.append(delimiter).append(attributeLabel[aIndex]);
                }
                if (summarizeTimeStep) { 
                    lineSB.append(delimiter).append(ALL_ATTRIBUTES_LABEL);
                }
            }
        } else {
            for (int aIndex = 0; aIndex < aCount; ++aIndex )
                for (int sIndex = 0; sIndex < sCount; ++sIndex) {{
                    lineSB.append(delimiter).append(attributeLabel[aIndex]);
                }
            }
            if (summarizeTimeStep) {
                for (int sIndex = 0; sIndex < statisticLabel.length; ++sIndex) {
                    lineSB.append(delimiter).append(ALL_ATTRIBUTES_LABEL);
                }
            }
        }
        writer.write(lineSB.toString());
        writer.newLine();

        lineSB.setLength(0);
        if (rowLabels != null && rowLabels.size() > 0) {
            Iterator<String> rowLabelIterator = rowLabels.iterator();
            lineSB.append(rowLabelIterator.next());
            while (rowLabelIterator.hasNext()) {
                lineSB.append(delimiter).append(rowLabelIterator.next());
            }
        }
        
        if (groupByStatistic) {
            for (int sIndex = 0; sIndex < sCount; ++sIndex) {
                int aCountX = summarizeTimeStep ? aCount + 1 : aCount; 
                for (int aIndex = 0; aIndex < aCountX; ++ aIndex) {
                    lineSB.append(delimiter).append(statisticLabel[sIndex]);
                }
            }
        } else {
            int aCountX = summarizeTimeStep ? aCount + 1 : aCount;
            for (int aIndex = 0; aIndex < aCountX; ++ aIndex) {
                for (int sIndex = 0; sIndex < sCount; ++sIndex) {
                    lineSB.append(delimiter).append(statisticLabel[sIndex]);
                }
            }
        }
        writer.write(lineSB.toString());
        writer.newLine();
    }

    public void writeRow(
            Collection<String> rowLabels,
            Collection<WeightedStatistics1D> rowValues,
            WeightedStatistics1D rowSummary)
            throws IOException
    {
        lineSB.setLength(0);
        if (rowLabels != null && rowLabels.size() > 0) {
            Iterator<String> rowLabelIterator = rowLabels.iterator();
            lineSB.append(rowLabelIterator.next());
            while (rowLabelIterator.hasNext()) {
                lineSB.append(delimiter).append(rowLabelIterator.next());
            }
        }

        if (groupByStatistic) {
            for (Statistic field : statisticList) {
                for (WeightedStatistics1D rowValue : rowValues) {
                    lineSB.append(delimiter).append(field.getValue(rowValue));
                }
                if (summarizeTimeStep) {
                    // value for ALL features across timestep
                    lineSB.append(delimiter).append(field.getValue(rowSummary));
                }
            }
        } else {
            for (WeightedStatistics1D rowValue : rowValues) {
                for (Statistic field : statisticList) {
                    lineSB.append(delimiter).append(field.getValue(rowValue));
                }
            }
            if (summarizeTimeStep) {
                // value for ALL features across timestep
                for (Statistic field : statisticList) {
                    lineSB.append(delimiter).append(field.getValue(rowSummary));
                }
            }
        }
        
        writer.write(lineSB.toString());
        writer.newLine();
    }

}
