package gov.usgs.cida.gdp.wps.algorithm;

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
    private final static String O_OUTPUT = "OUTPUT";
    private final static Map<String, InputDescriptor> INPUT;
    private final static Map<String, Class> OUTPUT;

    static {
        Map<String, InputDescriptor> imap = new LinkedHashMap<String, InputDescriptor>();
        imap.put(I_FEATURE_COLLECTION,
                ComplexDataInputDescriptor.builder(GTVectorDataBinding.class).build());
        imap.put(I_FEATURE_ATTRIBUTE_NAME,
               LiteralDataInputDescriptor.stringBuilder().build());
        imap.put(I_DATASET_URI,
               LiteralDataInputDescriptor.anyURIBuilder().build());
        imap.put(I_DATASET_ID,
                LiteralDataInputDescriptor.stringBuilder().build());
        INPUT = Collections.unmodifiableMap(imap);

        Map<String, Class> omap = new LinkedHashMap<String, Class>();
        omap.put(O_OUTPUT, CSVDataBinding.class);
        OUTPUT = Collections.unmodifiableMap(omap);
    }

    @Override
    protected Map<String, InputDescriptor> getInputDescriptorMap() {
        return INPUT;
    }

    @Override
    protected Map<String, Class> getOutputDescriptorMap() {
        return OUTPUT;
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
                        ",");

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
