package gov.usgs.cida.gdp.tools.coreprocessing.analysis.grid;

import ucar.nc2.dt.GridCoordSystem;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.prep.PreparedGeometry;
import com.vividsolutions.jts.geom.prep.PreparedGeometryFactory;
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

        cellCoverageFraction = new double[cellCount];

        updateCoverage(geometry, geometryCRS, gridCellGeometry);
    }

    public double getCellCoverageFraction(int xIndex, int yIndex) {
        return cellCoverageFraction[xIndex + yIndex * xCellCount];
    }

    public void updateCoverage(Geometry geometry,
            CoordinateReferenceSystem geometryCRS,
            GridCellGeometry gridCellGeometry)
            throws FactoryException, TransformException {

        MathTransform transform = CRS.findMathTransform(geometryCRS, DefaultGeographicCRS.WGS84, true);
        Geometry transformGeometry = JTS.transform(geometry, transform);
        PreparedGeometry preparedGeometry = PreparedGeometryFactory.prepare(transformGeometry);

        for (int yIndex = 0; yIndex < yCellCount; ++yIndex) {
            int yOffset = yIndex * xCellCount;
            for (int xIndex = 0; xIndex < xCellCount; ++xIndex) {
                int yxIndex = yOffset + xIndex;
                Geometry cellGeometry = gridCellGeometry.getCellGeometryQuick(yxIndex);
                if (preparedGeometry.intersects(cellGeometry)) {
                    if (preparedGeometry.containsProperly(cellGeometry)) {
                        cellCoverageFraction[yxIndex] = 1d;
                    } else {
                        Geometry intersectGeometry = transformGeometry.intersection(cellGeometry);
                        double intersectArea = intersectGeometry.getArea();
                        cellCoverageFraction[yxIndex] += intersectArea / cellGeometry.getArea();
                    }
                }
            }
        }
    }

}