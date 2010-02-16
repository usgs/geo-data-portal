package gov.usgs.gdp.analysis;

import gov.usgs.gdp.analysis.statistics.WeightedStatisticsAccumulator1D;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Formatter;
import java.util.Map;

public class GridStatisticsCSVWriter implements GridStatisticsWriter {

    private GridStatistics gridStatistics;
    
    public GridStatisticsCSVWriter(GridStatistics gridStatistics) {
        this.gridStatistics = gridStatistics;
    }
    
    public void write(BufferedWriter writer)  throws IOException  {
        writerHeader(writer);
        writerBody(writer);
        writerFooter(writer);
    }
    
    private void writerHeader(BufferedWriter writer)  throws IOException {
        String[] labels = new String[] { "mean", "minimum", "maximum", "variance", "std_dev","weight_sum", "count" };
        StringBuilder lineSB = new StringBuilder();
        for (Object attributeValue : gridStatistics.getAttributeValues()) {
            for(int i = 0; i < labels.length; ++i) {
                String attributeValueString = attributeValue.toString();
                lineSB.append(',').append(attributeValueString);
            }
        }
        for(int i = 0; i < labels.length; ++i) {
            lineSB.append(',').append("ALL");
        }
        writer.write(lineSB.toString());
        writer.newLine();
        
        StringBuilder entryLabelSB = new StringBuilder();
        for(String label : labels) {
            entryLabelSB.append(',').append(label);
        }
        lineSB.setLength(0);
        lineSB.append("timestep");
        for (Object attributeValue : gridStatistics.getAttributeValues()) {
            lineSB.append(entryLabelSB);
        }
        lineSB.append(entryLabelSB);
        writer.write(lineSB.toString());
        writer.newLine();
    }


    
    private void writerBody(BufferedWriter writer)  throws IOException {
        StringBuilder lineSB = new StringBuilder();
        for (Map.Entry<Date, Map<Object, WeightedStatisticsAccumulator1D>> entry0 : gridStatistics.getPerTimestepPerAttributeValueStatistics().entrySet()) {
            Date timestep = entry0.getKey();
            lineSB.setLength(0);
            lineSB.append(timestep.toGMTString()); // deprecated, figure out better way later.
            for (Map.Entry<Object, WeightedStatisticsAccumulator1D> entry1 : entry0.getValue().entrySet()) {
                // if you are going to output more values that just mean you'll need to tweak the header
                lineSB.append(writeStatistics(entry1.getValue()));
            }
            // value for ALL features across timestep
            lineSB.append(writeStatistics(gridStatistics.getPerTimestepAllAttributeValueStatistics().get(entry0.getKey())));
            writer.write(lineSB.toString());
            writer.newLine();
        }
    }
    
    private void writerFooter(BufferedWriter writer) throws IOException {
        // SUMMARY STUFF for ALL timesteps
        StringBuilder lineSB = new StringBuilder();
        lineSB.append("ALL");
        for (Object attributeValue : gridStatistics.getAttributeValues()) {
            lineSB.append(writeStatistics(gridStatistics.getPerAttributeValueAllTimestepStatistics().get(attributeValue)));
        }
        lineSB.append(writeStatistics(gridStatistics.allTimestepAllAttributeValueStatistics()));
        writer.write(lineSB.toString());
        writer.newLine();
    }
    
    private Appendable writeStatistics(WeightedStatisticsAccumulator1D wsa) {
        Formatter f = new Formatter();
        f.format(",%.7g,%.7g,%.7g,%.7g,%.7g,%.7g,%d",
                wsa.getMean(),
                wsa.getMinimum(),
                wsa.getMaximum(),
                wsa.getSampleVariance(),
                wsa.getSampleStandardDeviation(),
                wsa.getWeightSum(),
                wsa.getCount());
        return f.out();
    }
}
