package gov.usgs.gdp.analysis;

import com.vividsolutions.jts.geom.Envelope;
import ucar.nc2.dt.GridCoordSystem;

import com.vividsolutions.jts.geom.Geometry;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

/**
 *
 * @author tkunicki
 */
public class GridCellCoverage {

    private double[] cellCoverageFraction;
    private double[] featureCoverageFraction;
    private double[] featureCoverage;

    final private int xCellCount;
    final private int yCellCount;
    final private int cellCount;


    public GridCellCoverage(Geometry geometry, CoordinateReferenceSystem geometryCRS, GridCoordSystem gridCoordSystem)
            throws FactoryException, TransformException {
        this(geometry, geometryCRS, new GridCellGeometry(gridCoordSystem));
    }

    public GridCellCoverage(Geometry geometry, CoordinateReferenceSystem geometryCRS, GridCellGeometry gridCellGeometry)
            throws FactoryException, TransformException {

        xCellCount = gridCellGeometry.getCellCountX();
        yCellCount = gridCellGeometry.getCellCountY();
        cellCount = xCellCount * yCellCount;

        generateCoverage(geometry, geometryCRS, gridCellGeometry);
    }

    public double getCellCoverageFraction(int yxIndex) {
        return cellCoverageFraction[yxIndex];
    }

    public double getCellCoverageFraction(int xIndex, int yIndex) {
        return cellCoverageFraction[xIndex + yIndex * xCellCount];
    }

    public double getFeatureCoverage(int yxIndex) {
        return featureCoverage[yxIndex];
    }

    public double getFeatureCoverage(int xIndex, int yIndex) {
        return featureCoverage[xIndex + yIndex * xCellCount];
    }

    public double getFeatureCoverageFraction(int yxIndex) {
        return featureCoverageFraction[yxIndex];
    }

    public double getFeatureCoverageFraction(int xIndex, int yIndex) {
        return featureCoverageFraction[xIndex + yIndex * xCellCount];
    }

    private void generateCoverage(
            Geometry geometry,
            CoordinateReferenceSystem geometryCRS,
            GridCellGeometry gridCellGeometry)
            throws FactoryException, TransformException {

        MathTransform latLonTransform = CRS.findMathTransform(geometryCRS, DefaultGeographicCRS.WGS84, true);
        Geometry latLonGeom = JTS.transform(geometry, latLonTransform);

        cellCoverageFraction = new double[cellCount];
        featureCoverage = new double[cellCount];
        featureCoverageFraction = new double[cellCount];

        double featureArea = latLonGeom.getArea();

        Envelope latLonEnvelope = latLonGeom.getEnvelopeInternal();

        for (int yIndex = 0; yIndex < yCellCount; ++yIndex) {
            int yOffset = yIndex * xCellCount;
            for (int xIndex = 0; xIndex < xCellCount; ++xIndex) {
                int yxIndex = yOffset + xIndex;

                Geometry cellGeometry = gridCellGeometry.getCellGeometry(xIndex, yIndex);
                Envelope cellEnvelope = cellGeometry.getEnvelopeInternal();
                // check envelopes first (very low cost) if they overlap then
                // spend the CPU cycles to calculate intersection.
                if (latLonEnvelope.intersects(cellEnvelope)) {
                    Geometry intersectGeometry = cellGeometry.intersection(latLonGeom);
                    cellCoverageFraction[yxIndex] = intersectGeometry.getArea() / cellGeometry.getArea();
                    featureCoverage[yxIndex] = intersectGeometry.getArea();
                    featureCoverageFraction[yxIndex] = featureCoverage[yxIndex] / featureArea;
                }
            }
        }
    }
}
