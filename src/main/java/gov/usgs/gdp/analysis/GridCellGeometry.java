package gov.usgs.gdp.analysis;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
import ucar.ma2.ArrayDouble;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.CoordinateAxis1D;
import ucar.nc2.dataset.CoordinateAxis2D;
import ucar.nc2.dt.GridCoordSystem;
import ucar.unidata.geoloc.LatLonPointImpl;
import ucar.unidata.geoloc.Projection;
import ucar.unidata.geoloc.ProjectionPointImpl;

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

        if (xAxis instanceof CoordinateAxis1D && yAxis instanceof CoordinateAxis1D) {
            xCellCount = (int) xAxis.getSize();
            yCellCount = (int) yAxis.getSize();
        } else if (xAxis instanceof CoordinateAxis2D && yAxis instanceof CoordinateAxis2D) {
            CoordinateAxis2D xAxis2D = (CoordinateAxis2D) xAxis;
            CoordinateAxis2D yAxis2D = (CoordinateAxis2D) yAxis;
            int[] xShape = xAxis2D.getShape();
            int[] yShape = xAxis2D.getShape();
            if (xShape[0] == yShape[0] && xShape[1] == yShape[1]) {
                xCellCount = xShape[1];
                yCellCount = xShape[0];
            } else {
                throw new IllegalStateException("Inconsistent coordinate axis definititions.");
            }
        } else {
            throw new IllegalStateException("Incompatible coordinate axes.");
        }
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

    public Geometry getCellGeometry(int yxIndex) {
        return cellGeometry[yxIndex];
    }

    public Geometry getCellGeometry(int xIndex, int yIndex) {
        return cellGeometry[xIndex + yIndex * xCellCount];
    }

    private void generateCellGeometry() {

//        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);
        GeometryFactory geometryFactory = new GeometryFactory(
                new PrecisionModel(PrecisionModel.FLOATING),
                8307);

        final CoordinateBuilder coordinateBuilder = generateCoordinateBuider();
        final GridCellEdgeProvider cellEdgeProviderX = generateGridCellEdgeProviderX();
        final GridCellEdgeProvider cellEdgeProviderY = generateGridCellEdgeProviderY();
        
        int xCellEdgeCount = cellEdgeProviderX.getCellEdgeCount();

        cellGeometry = new Geometry[cellCount];

        Coordinate[] lowerCoordinates = null;
        Coordinate[] upperCoordinates = new Coordinate[xCellEdgeCount];

        // prime data storage for algorithm below...
        for (int xCellIndex = 0; xCellIndex < xCellEdgeCount; ++xCellIndex) {
            upperCoordinates[xCellIndex] = coordinateBuilder.getCoordinate(
                    cellEdgeProviderX.getCellEdge(xCellIndex, 0),
                    cellEdgeProviderY.getCellEdge(xCellIndex, 0));
        }

        for (int yIndexLower = 0; yIndexLower < yCellCount; ++yIndexLower) {

            int yOffset = xCellCount * yIndexLower;

            int yIndexUpper = yIndexLower + 1;

            lowerCoordinates = upperCoordinates;
            upperCoordinates = new Coordinate[xCellEdgeCount];

            upperCoordinates[0] = coordinateBuilder.getCoordinate(
                    cellEdgeProviderX.getCellEdge(0, yIndexUpper),
                    cellEdgeProviderY.getCellEdge(0, yIndexUpper));
            
            for (int xIndexLower = 0; xIndexLower < xCellCount; ++xIndexLower) {
                int xIndexUpper = xIndexLower + 1;

                upperCoordinates[xIndexUpper] = coordinateBuilder.getCoordinate(
                        cellEdgeProviderX.getCellEdge(xIndexUpper, yIndexUpper),
                        cellEdgeProviderY.getCellEdge(xIndexUpper, yIndexUpper));

                Coordinate[] ringCoordinates = new Coordinate[] {
                    lowerCoordinates[xIndexLower],
                    lowerCoordinates[xIndexUpper],
                    upperCoordinates[xIndexUpper],
                    upperCoordinates[xIndexLower],
                    lowerCoordinates[xIndexLower]
                };

                cellGeometry[yOffset + xIndexLower] =
                        geometryFactory.createPolygon(
                            geometryFactory.createLinearRing(ringCoordinates),
                            null);
            }
        }
    }

    protected CoordinateBuilder generateCoordinateBuider() {
        return gridCoordSystem.isLatLon()
                ? new CoordinateBuilder()
                : new ProjectedCoordinateBuilder();
    }

    protected GridCellEdgeProvider generateGridCellEdgeProviderX() {
        CoordinateAxis axis = gridCoordSystem.getXHorizAxis();
        if (axis instanceof CoordinateAxis1D) {
            return new GridCellEdgeProviderAxis1DX((CoordinateAxis1D)axis);
        } else if (axis instanceof CoordinateAxis2D) {
            return new GridCellEdgeProviderAxis2DX((CoordinateAxis2D)axis);
        } else {
            throw new IllegalStateException("Unknown coordinate axis type");
        }
    }

    protected GridCellEdgeProvider generateGridCellEdgeProviderY() {
        CoordinateAxis axis = gridCoordSystem.getYHorizAxis();
        if (axis instanceof CoordinateAxis1D) {
            return new GridCellEdgeProviderAxis1DY((CoordinateAxis1D)axis);
        } else if (axis instanceof CoordinateAxis2D) {
            return new GridCellEdgeProviderAxis2DY((CoordinateAxis2D)axis);
        } else {
            throw new IllegalStateException("Unknown coordinate axis type");
        }
    }

    protected class CoordinateBuilder {

        public Coordinate getCoordinate(double x, double y) {
            return new Coordinate(x, y);
        }
    }

    private class ProjectedCoordinateBuilder extends CoordinateBuilder {

        private Projection projection;
        private ProjectionPointImpl projectionPoint;
        private LatLonPointImpl latLonPoint;

        public ProjectedCoordinateBuilder() {
            projection = gridCoordSystem.getProjection();
            projectionPoint = new ProjectionPointImpl();
            latLonPoint = new LatLonPointImpl();
        }

        @Override
        public Coordinate getCoordinate(double x, double y) {
            projectionPoint.setLocation(x, y);
            projection.projToLatLon(projectionPoint, latLonPoint);
            return super.getCoordinate(latLonPoint.getLongitude(), latLonPoint.getLatitude());
        }
    }

    protected interface GridCellEdgeProvider {
        public int getCellEdgeCount();
        public double getCellEdge(int xCellIndex, int yCellIndex);
    }

    private class GridCellEdgeProviderAxis1DX implements GridCellEdgeProvider {

        private double[] cellEdges;

        public GridCellEdgeProviderAxis1DX(CoordinateAxis1D axis1D) {
            cellEdges = axis1D.getCoordEdges();
        }

        @Override
        public int getCellEdgeCount() {
           return cellEdges.length; 
        }

        @Override
        public double getCellEdge(int xCellIndex, int yCellIndex) {
            return cellEdges[xCellIndex];
        }
    }

    private class GridCellEdgeProviderAxis1DY implements GridCellEdgeProvider {

        private double[] cellEdges;

        public GridCellEdgeProviderAxis1DY(CoordinateAxis1D axis1D) {
            cellEdges = axis1D.getCoordEdges();
        }

        @Override
        public int getCellEdgeCount() {
           return cellEdges.length;
        }

        @Override
        public double getCellEdge(int xCellIndex, int yCellIndex) {
            return cellEdges[yCellIndex];
        }
    }

    private class GridCellEdgeProviderAxis2DX implements GridCellEdgeProvider {

        private ArrayDouble.D2 cellEdges;

        public GridCellEdgeProviderAxis2DX(CoordinateAxis2D axis2D) {
            cellEdges = CoordinateAxis2D.makeXEdges(axis2D.getMidpoints());
        }

        @Override
        public int getCellEdgeCount() {
           return cellEdges.getShape()[1];
        }

        @Override
        public double getCellEdge(int xCellEdgeIndex, int yCellEdgeIndex) {
            // NOTE: argument order swap...
            return cellEdges.get(yCellEdgeIndex, xCellEdgeIndex);
        }
    }

    private class GridCellEdgeProviderAxis2DY implements GridCellEdgeProvider {

        private ArrayDouble.D2 cellEdges;

        public GridCellEdgeProviderAxis2DY(CoordinateAxis2D axis2D) {
            cellEdges = CoordinateAxis2D.makeYEdges(axis2D.getMidpoints());
        }

        @Override
        public int getCellEdgeCount() {
           return cellEdges.getShape()[0];
        }

        @Override
        public double getCellEdge(int xCellEdgeIndex, int yCellEdgeIndex) {
            // NOTE: argument order swap...
            return cellEdges.get(yCellEdgeIndex, xCellEdgeIndex);
        }
    }
}
