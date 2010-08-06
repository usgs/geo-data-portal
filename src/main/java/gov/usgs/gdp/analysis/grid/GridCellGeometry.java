package gov.usgs.gdp.analysis.grid;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
import gov.usgs.gdp.analysis.grid.GridUtility.IndexToCoordinateBuilder;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dt.GridCoordSystem;

/**
 *
 * @author tkunicki
 */
public class GridCellGeometry {

    private final GridCoordSystem gridCoordSystem;

    private final int xCellCount;
    private final int yCellCount;
    private final int cellCount;

    private Geometry[] cellGeometry;

    public GridCellGeometry(GridCoordSystem gridCoordSystem) {

        this.gridCoordSystem = gridCoordSystem;

        CoordinateAxis xAxis = gridCoordSystem.getXHorizAxis();
        CoordinateAxis yAxis = gridCoordSystem.getYHorizAxis();

        xCellCount = xAxis.getShape(xAxis.getRank() - 1);
        yCellCount = yAxis.getShape(0);
        cellCount = xCellCount * yCellCount;

        generateCellGeometry();
    }

    public GridCoordSystem getGridCoordSystem() {
        return gridCoordSystem;
    }

    public int getCellCountX() {
        return xCellCount;
    }

    public int getCellCountY() {
        return yCellCount;
    }

    public int getCellCount() {
        return cellCount;
    }

    public Geometry getCellGeometry(int xIndex, int yIndex) {
        if (xIndex < 0 || xIndex >= xCellCount ||
            yIndex < 0 || yIndex >= yCellCount) {
            throw new IndexOutOfBoundsException();
        }
        return getCellGeometryQuick(xIndex, yIndex);
    }

    Geometry getCellGeometryQuick(int xIndex, int yIndex) {
        return cellGeometry[xIndex + yIndex * xCellCount];
    }

    Geometry getCellGeometryQuick(int yxIndex) {
        return cellGeometry[yxIndex];
    }

    private void generateCellGeometry() {

//        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);
        GeometryFactory geometryFactory = new GeometryFactory(
                new PrecisionModel(PrecisionModel.FLOATING),
                8307);

        IndexToCoordinateBuilder indexToCoordinateBuilder =
                GridUtility.generateIndexToCellEdgeCoordinateBuilder(gridCoordSystem);

        int xCellEdgeCount = indexToCoordinateBuilder.getXIndexCount();

        cellGeometry = new Geometry[cellCount];

        Coordinate[] lowerCoordinates = null;
        Coordinate[] upperCoordinates = new Coordinate[xCellEdgeCount];

        // prime data storage for algorithm below...
        for (int xCellIndex = 0; xCellIndex < xCellEdgeCount; ++xCellIndex) {
            upperCoordinates[xCellIndex] =
                    indexToCoordinateBuilder.getCoordinate(xCellIndex, 0);
        }

        for (int yIndexLower = 0; yIndexLower < yCellCount; ++yIndexLower) {

            int yOffset = xCellCount * yIndexLower;

            int yIndexUpper = yIndexLower + 1;

            lowerCoordinates = upperCoordinates;
            upperCoordinates = new Coordinate[xCellEdgeCount];

            upperCoordinates[0] =
                    indexToCoordinateBuilder.getCoordinate(0, yIndexUpper);
            
            for (int xIndexLower = 0; xIndexLower < xCellCount; ++xIndexLower) {
                int xIndexUpper = xIndexLower + 1;

                upperCoordinates[xIndexUpper] =
                        indexToCoordinateBuilder.getCoordinate(xIndexUpper, yIndexUpper);

                Coordinate[] ringCoordinates = new Coordinate[] {
                    lowerCoordinates[xIndexLower],
                    lowerCoordinates[xIndexUpper],
                    upperCoordinates[xIndexUpper],
                    upperCoordinates[xIndexLower],
                    // same as first entry, required for LinearRing
                    lowerCoordinates[xIndexLower]
                };

                cellGeometry[yOffset + xIndexLower] =
                        geometryFactory.createPolygon(
                            geometryFactory.createLinearRing(ringCoordinates),
                            null);
            }
        }
    }
    
}
