package gov.usgs.cida.gdp.wps.algorithm;

import gov.usgs.cida.gdp.coreprocessing.Delimiter;
import gov.usgs.cida.gdp.coreprocessing.analysis.grid.FeatureCategoricalGridCoverage;
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
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.ft.FeatureDataset;

/**
 *
 * @author tkunicki
 */
public class FeatureCategoricalGridCoverageAlgorithmSD extends AbstractSelfDescribingAlgorithm {

    private final static String I_FEATURE_COLLECTION = "FEATURE_COLLECTION";
    private final static String I_FEATURE_ATTRIBUTE_NAME = "FEATURE_ATTRIBUTE_NAME";
    private final static String I_DATASET_URI = "DATASET_URI";
    private final static String I_DATASET_ID = "DATASET_ID";
    private final static String I_DELIMITER = "DELIMITER";
    private final static String O_OUTPUT = "OUTPUT";

    private final static AlgorithmDescriptor ALGORITHM_DESCRIPTOR;

    static {
        ALGORITHM_DESCRIPTOR = AlgorithmDescriptor.builder(FeatureCategoricalGridCoverageAlgorithmSD.class).
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
                    LiteralDataInputDescriptor.stringBuilder(I_DELIMITER).allowedValues(Delimiter.class)).
                addOutputDesciptor(ComplexDataOutputDescriptor.builder(CSVFileBinding.class, O_OUTPUT)).
                build();
    }

    @Override
    protected AlgorithmDescriptor getAlgorithmDescriptor() {
        return ALGORITHM_DESCRIPTOR;
    }

    public FeatureCategoricalGridCoverageAlgorithmSD() {
            super();
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

            URI datasetURI = GDPAlgorithmUtil.extractURI(input, I_DATASET_URI);
            String datasetID = GDPAlgorithmUtil.extractString(input, I_DATASET_ID);

            GridDatatype gridDatatype = GDPAlgorithmUtil.generateGridDataType(
                    datasetURI,
                    datasetID,
                    featureCollection.getBounds());


            String delimiterString = GDPAlgorithmUtil.extractString(input, I_DELIMITER);
                Delimiter delimiter = delimiterString == null ?
                    Delimiter.getDefault() :
                    Delimiter.valueOf(delimiterString);

            File file = File.createTempFile("gdp", "csv");
            writer = new BufferedWriter(new FileWriter(file));

            FeatureCategoricalGridCoverage.execute(
                    featureCollection,
                    featureAttributeName,
                    gridDatatype,
                    writer,
                    delimiter);

            writer.flush();
            writer.close();

            output.put(O_OUTPUT, new CSVFileBinding(file));

        } catch (InvalidRangeException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (FactoryException e) {
            throw new RuntimeException(e);
        } catch (TransformException e) {
            throw new RuntimeException(e);
        } catch (SchemaException e) {
            throw new RuntimeException(e);
        } finally {
            if (featureDataset != null) try { featureDataset.close(); } catch (IOException e) { }
            if (writer != null) try { writer.close(); } catch (IOException e) { }
        }
        return output;
    }

}
