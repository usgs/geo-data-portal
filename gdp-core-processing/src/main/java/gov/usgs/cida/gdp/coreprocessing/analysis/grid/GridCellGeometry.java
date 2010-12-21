package gov.usgs.cida.gdp.coreprocessing.analysis.grid;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
import gov.usgs.cida.gdp.coreprocessing.analysis.grid.GridUtility.IndexToCoordinateBuilder;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dt.GridCoordSystem;

/**
 *
 * @author tkunicki
 */
public class GridCellGeometry {

    private final GridCoordSystem gridCoordSystem;

	private final CoordinateReferenceSystem gridCRS;

    private final int xCellCount;
    private final int yCellCount;
    private final int cellCount;

    private final GeometryFactory geometryFactory;
	private final IndexToCoordinateBuilder coordinateBuilder;

    public GridCellGeometry(GridCoordSystem gridCoordSystem) {

        this.gridCoordSystem = gridCoordSystem;

		this.gridCRS = CRSUtility.getCRSFromGridCoordSystem(gridCoordSystem);

        CoordinateAxis xAxis = gridCoordSystem.getXHorizAxis();
        CoordinateAxis yAxis = gridCoordSystem.getYHorizAxis();

        xCellCount = xAxis.getShape(xAxis.getRank() - 1);
        yCellCount = yAxis.getShape(0);
        cellCount = xCellCount * yCellCount;

		geometryFactory = new GeometryFactory(
                new PrecisionModel(PrecisionModel.FLOATING));

		coordinateBuilder =
                GridUtility.generateIndexToCellEdgeCoordinateBuilder(gridCoordSystem);
    }

    public GridCoordSystem getGridCoordSystem() {
        return gridCoordSystem;
    }

	public CoordinateReferenceSystem getGridCRS() {
		return gridCRS;
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
        return generateCellGeometry(xIndex, yIndex);
    }

    Geometry getCellGeometryQuick(int yxIndex) {
        return generateCellGeometry(yxIndex % xCellCount, yxIndex / xCellCount);
    }

    private Geometry generateCellGeometry(int xIndex, int yIndex) {
		Coordinate coordinate = coordinateBuilder.getCoordinate(xIndex, yIndex);
		Coordinate[] coordinates = new Coordinate[] {
			coordinate,
			coordinateBuilder.getCoordinate(xIndex + 1, yIndex),
			coordinateBuilder.getCoordinate(xIndex + 1, yIndex + 1),
			coordinateBuilder.getCoordinate(xIndex, yIndex + 1),
			// same as first entry, required for LinearRing
			coordinate
		};
        return geometryFactory.createPolygon(
			geometryFactory.createLinearRing(coordinates),
			null);
    }

	public final int calculateYXIndex(int xIndex, int yIndex) {
		return xIndex + yIndex * xCellCount;
	}
    
}
