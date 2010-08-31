/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usgs.cida.gdp.dataaccess;

import gov.usgs.cida.gdp.dataaccess.bean.WCSCoverageInfo;
import java.io.File;
import java.io.IOException;
import org.geotools.data.FeatureSource;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.geometry.BoundingBox;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.operation.TransformException;
import org.slf4j.LoggerFactory;

/**
 *
 * @author admin
 */
public class WCSCoverageInfoHelper {
	private static org.slf4j.Logger log = LoggerFactory.getLogger(WCSCoverageInfoHelper.class);
    private static final int MAX_COVERAGE_SIZE = 64 << 20; // 64 MB

    public static WCSCoverageInfo calculateWCSCoverageInfo(
                String shapefilePath, double x1, double y1, double x2,
                double y2, String crs, String gridOffsets, String dataTypeString
            ) throws IOException, NoSuchAuthorityCodeException, FactoryException, TransformException {

        FileDataStore shapeFileDataStore;
        FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = null;

        shapeFileDataStore = FileDataStoreFinder.getDataStore(new File(shapefilePath));
        featureSource = shapeFileDataStore.getFeatureSource();

        ReferencedEnvelope gridBounds, featureXBounds;
        CoordinateReferenceSystem gridCRS;

        gridCRS = CRS.decode(crs);
        AxisDirection ad0 = gridCRS.getCoordinateSystem().getAxis(0).getDirection();
        AxisDirection ad1 = gridCRS.getCoordinateSystem().getAxis(1).getDirection();
        boolean swapXY =
                (ad0 == AxisDirection.NORTH || ad0 == AxisDirection.SOUTH)
                && (ad1 == AxisDirection.EAST || ad1 == AxisDirection.WEST);

        gridBounds = swapXY
                ? new ReferencedEnvelope(y1, y2, x1, x2, gridCRS)
                : new ReferencedEnvelope(x1, x2, y1, y2, gridCRS);

        featureXBounds =
                featureSource.getBounds().transform(gridCRS, true);


        boolean fullyCovers;
        int minResamplingFactor;
        String units, boundingBox;

        // Explicitly cast to BoundingBox because there are
        // ambiguous 'contains' methods
        if (!gridBounds.contains((BoundingBox) featureXBounds)) {
            fullyCovers = false;
        } else {
            fullyCovers = true;
        }


        /////// Estimate size of coverage request /////////

        String gridOffsetNums[] = gridOffsets.split(" ");
        double xOffset = Math.abs(Double.parseDouble(gridOffsetNums[0]));
        double yOffset = Math.abs(Double.parseDouble(gridOffsetNums[1]));

        // Size of data type in bytes
        int dataTypeSize;

        // We can't find the spec for what possible data types exist, so...
        // we have to check, and default to the max size (8) if we come
        // across an unrecognized type
        CoverageMetaData.DataType dataType = CoverageMetaData.findCoverageDataType(dataTypeString);
        if (dataType == CoverageMetaData.UnknownDataType) {
            log.info("Unrecognized wcs data type: " + dataType);
            dataTypeSize = 8;
        } else {
            dataTypeSize = dataType.getSizeBytes();
        }

        double size = (featureXBounds.getHeight() / yOffset)
                * (featureXBounds.getWidth() / xOffset)
                * dataTypeSize;

        if (size > MAX_COVERAGE_SIZE) {
            float factor = (float) size / MAX_COVERAGE_SIZE;

            minResamplingFactor = (int) Math.round(Math.ceil(factor));
        } else {
            minResamplingFactor = 1; // Coverage size is ok as is
        }

        units = "blah";
        boundingBox = Double.toString(featureXBounds.getMinX()) + ","
                + Double.toString(featureXBounds.getMinY()) + ","
                + Double.toString(featureXBounds.getMaxX()) + ","
                + Double.toString(featureXBounds.getMaxY());


        WCSCoverageInfo bean = new WCSCoverageInfo(minResamplingFactor,
                fullyCovers, units, boundingBox);

        return bean;
    }
}
