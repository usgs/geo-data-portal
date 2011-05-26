package gov.usgs.cida.gdp.wps.algorithm;

import gov.usgs.cida.gdp.coreprocessing.analysis.grid.GridUtility;
import gov.usgs.cida.gdp.wps.util.WCSUtil;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.Formatter;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
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
    public final static String INPUT_REQUIRE_FULL_COVERAGE = "REQUIRE_FULL_COVERAGE";
    public final static String INPUT_TIME_START = "TIME_START";
    public final static String INPUT_TIME_END = "TIME_END";

    private GDPAlgorithmUtil() { }

    public static GridDataset generateGridDataSet(URI datasetURI) {
        try {
            FeatureDataset featureDataset = null;
            String featureDatasetScheme = datasetURI.getScheme();
            if ("dods".equals(featureDatasetScheme)) {
                featureDataset = FeatureDatasetFactoryManager.open(
                        FeatureType.GRID,
                        datasetURI.toString(),
                        null,
                        new Formatter(System.err));
                if (featureDataset instanceof GridDataset) {
                    return (GridDataset)featureDataset;
                } else {
                    throw new RuntimeException("Unable to open gridded dataset at " + datasetURI);
                }
            } else {
                throw new RuntimeException("Unable to open gridded dataset at " + datasetURI);
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public static GridDatatype generateGridDataType(URI datasetURI, String datasetId, ReferencedEnvelope featureBounds, boolean requireFullCoverage) {
        try {
            FeatureDataset featureDataset = null;
            String featureDatasetScheme = datasetURI.getScheme();
            if ("dods".equals(featureDatasetScheme)) {
                GridDataset gridDataSet = generateGridDataSet(datasetURI);
                GridDatatype gridDatatype =  gridDataSet.findGridDatatype(datasetId);
                if (gridDatatype == null) {
                    throw new RuntimeException("Unable to open dataset at " + datasetURI + " with identifier " + datasetId);
                }
                try {
                    Range[] ranges = GridUtility.getXYRangesFromBoundingBox(featureBounds, gridDatatype.getCoordinateSystem(), requireFullCoverage);
                    gridDatatype = gridDatatype.makeSubset(
                            null,       /* runtime */
                            null,       /* ensemble */
                            null,       /* time */
                            null,       /* z */
                            ranges[1]   /* y */ ,
                            ranges[0]   /* x */);
                } catch (InvalidRangeException ex) {
                    Logger.getLogger(GDPAlgorithmUtil.class.getName()).log(Level.SEVERE, null, ex);
                } catch (TransformException ex) {
                    Logger.getLogger(GDPAlgorithmUtil.class.getName()).log(Level.SEVERE, null, ex);
                } catch (FactoryException ex) {
                    Logger.getLogger(GDPAlgorithmUtil.class.getName()).log(Level.SEVERE, null, ex);
                }
                return gridDatatype;
            } else if ("http".equals(featureDatasetScheme)) {
                File tiffFile = WCSUtil.generateTIFFFile(datasetURI, datasetId, featureBounds, requireFullCoverage);
                featureDataset = FeatureDatasetFactoryManager.open(
                        FeatureType.GRID,
                        tiffFile.getCanonicalPath(),
                        null,
                        new Formatter(System.err));
                if (featureDataset instanceof GridDataset) {
                    GridDatatype gridDatatype = ((GridDataset)featureDataset).findGridDatatype("I0B0");
                    if (gridDatatype == null) {
                        throw new RuntimeException("Unable to open dataset at " + datasetURI + " with identifier " + datasetId);
                    }
                    return gridDatatype;
                } else {
                    throw new RuntimeException("Unable to open dataset at " + datasetURI + " with identifier " + datasetId);
                }
            }
            throw new RuntimeException("Unable to open dataset at " + datasetURI + " with identifier " + datasetId);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
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
