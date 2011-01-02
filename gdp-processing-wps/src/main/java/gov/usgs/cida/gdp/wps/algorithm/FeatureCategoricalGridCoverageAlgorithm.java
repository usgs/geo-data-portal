package gov.usgs.cida.gdp.wps.algorithm;

import gov.usgs.cida.gdp.coreprocessing.Delimiter;
import gov.usgs.cida.gdp.coreprocessing.analysis.grid.FeatureCategoricalGridCoverage;
import gov.usgs.cida.gdp.wps.binding.CSVDataBinding;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.SchemaException;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.dt.GridDataset;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.ft.FeatureDataset;

/**
 *
 * @author tkunicki
 */
public class FeatureCategoricalGridCoverageAlgorithm extends BaseAlgorithm {

    private final static String I_FEATURE_COLLECTION = "FEATURE_COLLECTION";
    private final static String I_FEATURE_ATTRIBUTE_NAME = "FEATURE_ATTRIBUTE_NAME";
    private final static String I_DATASET_URI = "DATASET_URI";
    private final static String I_DATASET_ID = "DATASET_ID";
    private final static String I_DELIMITER = "DELIMITER";
    private final static String O_OUTPUT = "OUTPUT";

    private final static Map<String, InputDescriptor> INPUT_DESCRIPTOR_MAP;
    private final static Map<String, OutputDescriptor> OUTPUT_DESCRIPTOR_MAP;
    private final static AlgorithmDescriptor ALGORITHM_DESCRIPTOR;

    static {
        ALGORITHM_DESCRIPTOR = AlgorithmDescriptor.builder(FeatureCategoricalGridCoverageAlgorithm.class).
                version("1.0.0").
                storeSupported(true).
                statusSupported(true).build();


        Map<String, InputDescriptor> imap = new LinkedHashMap<String, InputDescriptor>();
        imap.put(I_FEATURE_COLLECTION,
                ComplexDataInputDescriptor.builder(GTVectorDataBinding.class).build());
        imap.put(I_FEATURE_ATTRIBUTE_NAME,
               LiteralDataInputDescriptor.stringBuilder().build());
        imap.put(I_DATASET_URI,
               LiteralDataInputDescriptor.anyURIBuilder().build());
        imap.put(I_DATASET_ID,
                LiteralDataInputDescriptor.stringBuilder().build());
        imap.put(I_DELIMITER,
                LiteralDataInputDescriptor.stringBuilder().minOccurs(0).maxOccurs(1).allowedValues(Delimiter.class).build());
        INPUT_DESCRIPTOR_MAP = Collections.unmodifiableMap(imap);

        Map<String, OutputDescriptor> omap = new LinkedHashMap<String, OutputDescriptor>();
        omap.put(O_OUTPUT, ComplexDataOutputDescriptor.builder(CSVDataBinding.class).build());
        OUTPUT_DESCRIPTOR_MAP = Collections.unmodifiableMap(omap);
    }

    @Override
    protected AlgorithmDescriptor getAlgorithmDescriptor() {
        return ALGORITHM_DESCRIPTOR;
    }

    @Override
    protected Map<String, InputDescriptor> getInputDescriptorMap() {
        return INPUT_DESCRIPTOR_MAP;
    }

    @Override
    protected Map<String, OutputDescriptor> getOutputDescriptorMap() {
        return OUTPUT_DESCRIPTOR_MAP;
    }

    public FeatureCategoricalGridCoverageAlgorithm() {
            super();
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

            featureDataset = extractFeatureDataset(input, I_DATASET_URI);
            String featureDatasetID = extractString(input, I_DATASET_ID);

            String delimiterString = extractString(input, I_DELIMITER);
                Delimiter delimiter = delimiterString == null ?
                    Delimiter.getDefault() :
                    Delimiter.valueOf(delimiterString);

            GridDatatype gridDatatype = null;
            if (featureDataset instanceof GridDataset) {
                gridDatatype = ((GridDataset)featureDataset).findGridDatatype(featureDatasetID);

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

                output.put(O_OUTPUT, new CSVDataBinding(file));
            }

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
