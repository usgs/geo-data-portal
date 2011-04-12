package gov.usgs.cida.gdp.wps.algorithm;

import gov.usgs.cida.gdp.wps.util.WCSUtil;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.Formatter;
import org.geotools.geometry.jts.ReferencedEnvelope;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.dataset.CoordinateAxis1DTime;
import ucar.nc2.dt.GridDataset;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.ft.FeatureDataset;
import ucar.nc2.ft.FeatureDatasetFactoryManager;

/**
 *
 * @author tkunicki
 */
public abstract class GDPAlgorithmUtil {

    public final static String INPUT_FEATURE_COLLECTION = "FEATURE_COLLECTION";
    public final static String INPUT_FEATURE_ATTRIBUTE_NAME = "FEATURE_ATTRIBUTE_NAME";
    public final static String INPUT_DATASET_URI = "DATASET_URI";
    public final static String INPUT_DATASET_ID = "DATASET_ID";

    private GDPAlgorithmUtil() { }

    public static GridDatatype generateGridDataType(URI datasetURI, String datasetId, ReferencedEnvelope featureBounds) {
        GridDatatype gridDatatype = null;
        try {
            FeatureDataset featureDataset = null;
            String featureDatasetScheme = datasetURI.getScheme();
            if ("dods".equals(datasetURI.getScheme())) {
                featureDataset = FeatureDatasetFactoryManager.open(
                        FeatureType.GRID,
                        datasetURI.toString(),
                        null,
                        new Formatter(System.err));
                if (featureDataset instanceof GridDataset) {
                    gridDatatype =  ((GridDataset)featureDataset).findGridDatatype(datasetId);
                } else {
                    throw new RuntimeException("Unable to open dataset at " + datasetURI + " with identifier " + datasetId);
                }
            } else if ("http".equals(featureDatasetScheme)) {
                File tiffFile = WCSUtil.generateTIFFFile(datasetURI, datasetId, featureBounds);
                featureDataset = FeatureDatasetFactoryManager.open(
                        FeatureType.GRID,
                        tiffFile.getCanonicalPath(),
                        null,
                        new Formatter(System.err));
                if (featureDataset instanceof GridDataset) {
                    gridDatatype = ((GridDataset)featureDataset).findGridDatatype("I0B0");
                } else {
                    throw new RuntimeException("Unable to open dataset at " + datasetURI + " with identifier " + datasetId);
                }
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return gridDatatype;
    }

    public static Range generateTimeRange(GridDatatype GridDatatype, Date timeStart, Date timeEnd) {
        CoordinateAxis1DTime timeAxis = GridDatatype.getCoordinateSystem().getTimeAxis1D();
        Range timeRange = null;
        if (timeAxis != null) {
            int timeStartIndex = timeStart != null ?
                timeAxis.findTimeIndexFromDate(timeStart) :
                0;
            int timeEndIndex = timeEnd != null ?
                timeAxis.findTimeIndexFromDate(timeEnd) :
                timeAxis.getShape(0) - 1;
            try {
                timeRange = new Range(timeStartIndex, timeEndIndex);
            } catch (InvalidRangeException e) {
                throw new RuntimeException("Unable to generate time range.", e);
            }
        }
        return timeRange;
    }

}
