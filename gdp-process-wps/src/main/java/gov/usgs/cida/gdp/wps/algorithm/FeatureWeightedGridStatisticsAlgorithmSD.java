package gov.usgs.cida.gdp.wps.algorithm;

import gov.usgs.cida.gdp.coreprocessing.Delimiter;
import gov.usgs.cida.gdp.coreprocessing.analysis.grid.FeatureCoverageWeightedGridStatistics;
import gov.usgs.cida.gdp.coreprocessing.analysis.grid.FeatureCoverageWeightedGridStatistics.GroupBy;
import gov.usgs.cida.gdp.coreprocessing.analysis.grid.FeatureCoverageWeightedGridStatisticsWriter.Statistic;
import gov.usgs.cida.gdp.wps.algorithm.descriptor.AlgorithmDescriptor;
import gov.usgs.cida.gdp.wps.algorithm.descriptor.ComplexDataInputDescriptor;
import gov.usgs.cida.gdp.wps.algorithm.descriptor.ComplexDataOutputDescriptor;
import gov.usgs.cida.gdp.wps.algorithm.descriptor.LiteralDataInputDescriptor;
import gov.usgs.cida.gdp.wps.binding.CSVFileBinding;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.SchemaException;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.ft.FeatureDataset;

/**
 *
 * @author tkunicki
 */
public class FeatureWeightedGridStatisticsAlgorithmSD extends AbstractSelfDescribingAlgorithm {

    private final static String I_FEATURE_COLLECTION = "FEATURE_COLLECTION";
    private final static String I_FEATURE_ATTRIBUTE_NAME = "FEATURE_ATTRIBUTE_NAME";
    private final static String I_DATASET_URI = "DATASET_URI";
    private final static String I_DATASET_ID = "DATASET_ID";
    private final static String I_TIME_START = "TIME_START";
    private final static String I_TIME_END = "TIME_END";
    private final static String I_STATISTICS = "STATISTICS";
    private final static String I_GROUP_BY = "GROUP_BY";
    private final static String I_DELIMITER = "DELIMITER";

    private final static String O_OUTPUT = "OUTPUT";

    private final static AlgorithmDescriptor ALGORITHM_DESCRIPTOR;

    static {
        ALGORITHM_DESCRIPTOR = AlgorithmDescriptor.builder(FeatureWeightedGridStatisticsAlgorithmSD.class).
                version("1.0.0").
                storeSupported(true).
                statusSupported(true).
                addInputDesciptor(
                    ComplexDataInputDescriptor.builder(GTVectorDataBinding.class, I_FEATURE_COLLECTION)).
                addInputDesciptor(
                    LiteralDataInputDescriptor.stringBuilder(I_FEATURE_ATTRIBUTE_NAME)).
                addInputDesciptor(
                    LiteralDataInputDescriptor.anyURIBuilder(I_DATASET_URI)).
                addInputDesciptor(
                    LiteralDataInputDescriptor.stringBuilder(I_DATASET_ID)).
                addInputDesciptor(
                    LiteralDataInputDescriptor.dateTimeBuilder(I_TIME_START).minOccurs(0)).
                addInputDesciptor(
                    LiteralDataInputDescriptor.dateTimeBuilder(I_TIME_END).minOccurs(0)).
                addInputDesciptor(
                    LiteralDataInputDescriptor.stringBuilder(I_STATISTICS).minOccurs(0).maxOccurs(Statistic.class).allowedValues(Statistic.class)).
                addInputDesciptor(
                    LiteralDataInputDescriptor.stringBuilder(I_GROUP_BY).allowedValues(GroupBy.class)).
                addInputDesciptor(
                    LiteralDataInputDescriptor.stringBuilder(I_DELIMITER).allowedValues(Delimiter.class)).
                addOutputDesciptor(ComplexDataOutputDescriptor.builder(CSVFileBinding.class, O_OUTPUT)).
                build();
    }

    public FeatureWeightedGridStatisticsAlgorithmSD() {
            super();
    }

    @Override
    protected AlgorithmDescriptor getAlgorithmDescriptor() {
        return ALGORITHM_DESCRIPTOR;
    }

    @Override
    public Map<String, IData> run(Map<String, List<IData>> input) {

        Map<String, IData> output = new HashMap<String, IData>();

        FeatureCollection featureCollection = null;
        FeatureDataset featureDataset = null;
        BufferedWriter writer = null;
        try {
            featureCollection = GDPAlgorithmUtil.extractFeatureCollection(input, I_FEATURE_COLLECTION);
            String featureAttributeName = GDPAlgorithmUtil.extractString(input, I_FEATURE_ATTRIBUTE_NAME);
            if (featureCollection.getSchema().getDescriptor(featureAttributeName) == null) {
                addError("Attribute " + featureAttributeName + " not found in feature collection");
                return null;
            }

            URI datasetURI = GDPAlgorithmUtil.extractURI(input, I_DATASET_URI);
            String datasetID = GDPAlgorithmUtil.extractString(input, I_DATASET_ID);

            GridDatatype gridDatatype = GDPAlgorithmUtil.generateGridDataType(
                    datasetURI,
                    datasetID,
                    featureCollection.getBounds());

            Range timeRange = GDPAlgorithmUtil.generateTimeRange(
                    gridDatatype,
                    GDPAlgorithmUtil.extractDate(input, I_TIME_START),
                    GDPAlgorithmUtil.extractDate(input, I_TIME_END));

            List<String> statisticStringList = GDPAlgorithmUtil.extractStringList(input, I_STATISTICS);
            List<Statistic> statisticList = statisticStringList.size() < 1 ?
            Arrays.asList(Statistic.values()) :
            GDPAlgorithmUtil.convertStringToEnumList(Statistic.class, statisticStringList);

            String delimiterString = GDPAlgorithmUtil.extractString(input, I_DELIMITER);
            Delimiter delimiter = delimiterString == null ?
                Delimiter.COMMA :
                Delimiter.valueOf(delimiterString);

            String groupByString = GDPAlgorithmUtil.extractString(input, I_GROUP_BY);
            GroupBy groupBy = groupByString == null ?
                GroupBy.STATISTIC :
                GroupBy.valueOf(groupByString);

            File file = File.createTempFile("gdp", ".csv");
            writer = new BufferedWriter(new FileWriter(file));

            FeatureCoverageWeightedGridStatistics.execute(
                    featureCollection,
                    featureAttributeName,
                    gridDatatype,
                    timeRange,
                    statisticList,
                    writer,
                    groupBy,
                    delimiter);

            writer.flush();
            writer.close();

            output.put(O_OUTPUT, new CSVFileBinding(file));

        } catch (InvalidRangeException e) {
            addError("Error subsetting gridded data :" + e.getMessage());
        } catch (IOException e) {
            addError("IO Error :" + e.getMessage());
        } catch (FactoryException e) {
            addError("Error initializing CRS factory:" + e.getMessage());
        } catch (TransformException e) {
            addError("Error attempting CRS transform:" + e.getMessage());
        } catch (SchemaException e) {
            addError("Error subsetting gridded data :" + e.getMessage());
        } catch (Exception e) {
            addError("General Error: " + e.getMessage());
        } finally {
            if (featureDataset != null) try { featureDataset.close(); } catch (IOException e) { }
            if (writer != null) try { writer.close(); } catch (IOException e) { }
        }
        return output;
    }
	
}
