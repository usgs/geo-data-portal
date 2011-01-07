package gov.usgs.cida.gdp.wps.algorithm;

import gov.usgs.cida.gdp.coreprocessing.Delimiter;
import gov.usgs.cida.gdp.coreprocessing.analysis.grid.FeatureCoverageWeightedGridStatistics;
import gov.usgs.cida.gdp.coreprocessing.analysis.grid.FeatureCoverageWeightedGridStatistics.GroupBy;
import gov.usgs.cida.gdp.coreprocessing.analysis.grid.FeatureCoverageWeightedGridStatisticsWriter.Statistic;
import gov.usgs.cida.gdp.wps.algorithm.descriptor.AlgorithmDescriptor;
import gov.usgs.cida.gdp.wps.algorithm.descriptor.ComplexDataInputDescriptor;
import gov.usgs.cida.gdp.wps.algorithm.descriptor.ComplexDataOutputDescriptor;
import gov.usgs.cida.gdp.wps.algorithm.descriptor.LiteralDataInputDescriptor;
import gov.usgs.cida.gdp.wps.binding.CSVDataBinding;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
import ucar.nc2.dt.GridDataset;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.ft.FeatureDataset;

/**
 *
 * @author tkunicki
 */
public class FeatureWeightedGridStatisticsAlgorithm extends BaseAlgorithm {

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
        ALGORITHM_DESCRIPTOR = AlgorithmDescriptor.builder(FeatureWeightedGridStatisticsAlgorithm.class).
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
                addOutputDesciptor(ComplexDataOutputDescriptor.builder(CSVDataBinding.class, O_OUTPUT)).
                build();
    }

    public FeatureWeightedGridStatisticsAlgorithm() {
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
            featureCollection = extractFeatureCollection(input, I_FEATURE_COLLECTION);
            String featureAttributeName = extractString(input, I_FEATURE_ATTRIBUTE_NAME);
            if (featureCollection.getSchema().getDescriptor(featureAttributeName) == null) {
                addError("Attribute " + featureAttributeName + " not found in feature collection");
                return null;
            }

            featureDataset = extractFeatureDataset(input, I_DATASET_URI);
            String featureDatasetID = extractString(input, I_DATASET_ID);

            GridDatatype gridDatatype = null;
            if (featureDataset instanceof GridDataset) {
                gridDatatype = ((GridDataset)featureDataset).findGridDatatype(featureDatasetID);
            } else {
                addError(I_DATASET_URI + " does not resolve to gridded dataset");
                return null;
            }

            if (gridDatatype == null) {
                addError("Variable " +I_DATASET_ID + " not found in " + I_DATASET_URI);
                return null;
            }

            Range timeRange = generateTimeRange(
                    gridDatatype,
                    extractDate(input, I_TIME_START),
                    extractDate(input, I_TIME_END));

            List<String> statisticStringList = extractStringList(input, I_STATISTICS);
            List<Statistic> statisticList = statisticStringList.size() < 1 ?
            Arrays.asList(Statistic.values()) :
            convertStringToEnumList(statisticStringList, Statistic.class);

            String delimiterString = extractString(input, I_DELIMITER);
            Delimiter delimiter = delimiterString == null ?
                Delimiter.COMMA :
                Delimiter.valueOf(delimiterString);

            String groupByString = extractString(input, I_GROUP_BY);
            GroupBy groupBy = groupByString == null ?
                GroupBy.STATISTIC :
                GroupBy.valueOf(groupByString);

            File file = File.createTempFile("gdp", "csv");
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

            output.put(O_OUTPUT, new CSVDataBinding(file));

        } catch (InvalidRangeException e) {
            addError("Error subsetting gridded data :" + e.getMessage());
//            throw new RuntimeException(e);
        } catch (IOException e) {
            addError("IO Error :" + e.getMessage());
//            throw new RuntimeException(e);
        } catch (FactoryException e) {
            addError("Error initializing CRS factory:" + e.getMessage());
//            throw new RuntimeException(e);
        } catch (TransformException e) {
            addError("Error attempting CRS transform:" + e.getMessage());
//            throw new RuntimeException(e);
        } catch (SchemaException e) {
            addError("Error subsetting gridded data :" + e.getMessage());
//            throw new RuntimeException(e);
        } catch (Exception e) {
            addError("General Error: " + e.getMessage());
//            throw new RuntimeException(e);
        } finally {
            if (featureDataset != null) try { featureDataset.close(); } catch (IOException e) { }
            if (writer != null) try { writer.close(); } catch (IOException e) { }
        }
        return output;
    }
	
}
