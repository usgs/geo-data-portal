package gov.usgs.cida.gdp.wps.algorithm;

import gov.usgs.cida.gdp.wps.binding.NetCDFFileBinding;
import java.io.IOException;
import org.n52.wps.server.AbstractAnnotatedAlgorithm;
import org.n52.wps.algorithm.annotation.Algorithm;
import org.n52.wps.algorithm.annotation.ComplexDataInput;
import org.n52.wps.algorithm.annotation.ComplexDataOutput;
import org.n52.wps.algorithm.annotation.LiteralDataInput;
import org.n52.wps.algorithm.annotation.Process;
import java.io.File;
import java.net.URI;
import java.util.Date;
import java.util.List;
import org.geotools.feature.FeatureCollection;
import org.n52.wps.io.data.binding.complex.GTVectorDataBinding;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.dt.GridDataset;

/**
 *
 * @author tkunicki
 */
@Algorithm(
    version = "1.0.0",
    title = "Feature Coverage OPeNDAP Intersection",
    abstrakt="This service returns the subset of data that intersects a set of vector polygon features and time range, if specified. A NetCDF file will be returned.")
public class FeatureCoverageOPeNDAPIntersectionAlgorithm extends AbstractAnnotatedAlgorithm {

    private FeatureCollection featureCollection;
    private URI datasetURI;
    private List<String> datasetId;
    private Date dateTimeStart;
    private Date dateTimeEnd;

    private File output;

    @ComplexDataInput(identifier=GDPAlgorithmUtil.INPUT_FEATURE_COLLECTION, binding=GTVectorDataBinding.class)
    public void setFeatureCollection(FeatureCollection featureCollection) {
        this.featureCollection = featureCollection;
    }

    @LiteralDataInput(identifier=GDPAlgorithmUtil.INPUT_DATASET_URI)
    public void setDatasetURI(URI datasetURI) {
        this.datasetURI = datasetURI;
    }

    @LiteralDataInput(identifier=GDPAlgorithmUtil.INPUT_DATASET_ID, maxOccurs=Integer.MAX_VALUE)
    public void setDatasetId(List<String> datasetId) {
        this.datasetId = datasetId;
    }
    
    @LiteralDataInput(identifier=GDPAlgorithmUtil.INPUT_TIME_START, minOccurs=0)
    public void setDateTimeStart(Date dateTimeStart) {
        this.dateTimeStart = dateTimeStart;
    }
    
    @LiteralDataInput(identifier=GDPAlgorithmUtil.INPUT_TIME_END, minOccurs=0)
    public void setDateTimeEnd(Date dateTimeEnd) {
        this.dateTimeEnd = dateTimeEnd;
    }

    @ComplexDataOutput(identifier="OUTPUT", binding=NetCDFFileBinding.class)
    public File getOutput() {
        return output;
    }

    @Process
    public void process() {
        GridDataset gridDataSet = GDPAlgorithmUtil.generateGridDataSet(datasetURI);
        try { 
            output = File.createTempFile(getClass().getSimpleName(), ".nc");
            NetCDFGridSubSetWriter.makeFile(
                    output.getAbsolutePath(),
                    gridDataSet,
                    datasetId,
                    featureCollection,
                    dateTimeStart,
                    dateTimeEnd,
                    "Grid sub-setted by USGS/CIDA Geo Data Portal");
        } catch (IOException e) {
            throw new RuntimeException("Unable to generate NetCDF File");
        } catch (InvalidRangeException e) {
            throw new RuntimeException("Unable to generate NetCDF File, error sub setting grid");
        } catch (TransformException e) {
            throw new RuntimeException("Unable to generate NetCDF File, error transforming feature collection coordinates");
        } catch (FactoryException e) {
            throw new RuntimeException("Unable to generate NetCDF File, error intitializing coordinate transformation sub system");
        }
    }

}
