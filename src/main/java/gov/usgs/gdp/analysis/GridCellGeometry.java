package gov.usgs.gdp.analysis;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import org.geotools.geometry.jts.JTSFactoryFinder;
import ucar.nc2.dataset.CoordinateAxis1D;
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

    private Geometry[] cellGeometrys;

    public GridCellGeometry(GridCoordSystem gridCoordSystem) {

        this.gridCoordSystem = gridCoordSystem;

        xCellCount = (int) gridCoordSystem.getXHorizAxis().getSize();
        yCellCount = (int) gridCoordSystem.getYHorizAxis().getSize();
        cellCount = xCellCount * yCellCount;

        cellGeometrys = buildGeometrys();
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
        return cellGeometrys[xIndex + yIndex * xCellCount];
    }

    private Geometry[] buildGeometrys() {

        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);

        CoordinateAxis1D xAxis = (CoordinateAxis1D) gridCoordSystem.getXHorizAxis();
        CoordinateAxis1D yAxis = (CoordinateAxis1D) gridCoordSystem.getYHorizAxis();

        double[] xCellEdges = xAxis.getCoordEdges();
        double[] yCellEdges = yAxis.getCoordEdges();

        int xCellEdgeCount = xCellEdges.length;
        int yCellEdgeCount = yCellEdges.length;

        Coordinate[] cellEdgeCoordindate = new Coordinate[xCellEdgeCount * yCellEdgeCount];
        Geometry[] cellGeometry = new Geometry[cellCount];

        final CoordinateBuilder coordinateBuilder = gridCoordSystem.isLatLon()
                ? new CoordinateBuilder()
                : new ProjectedCoordinateBuilder();

        for (int yCellEdgeIndex = 0; yCellEdgeIndex < yCellEdgeCount; ++yCellEdgeIndex) {
            int yCellEdgeOffset = yCellEdgeIndex * xCellCount;
            for (int xCellEdgeIndex = 0; xCellEdgeIndex < xCellEdgeCount; ++xCellEdgeIndex) {
                int yxCellEdgeIndex = yCellEdgeOffset + xCellEdgeIndex;
                cellEdgeCoordindate[yxCellEdgeIndex] =
                        coordinateBuilder.getCoordinate(
                            xCellEdges[xCellEdgeIndex],
                            yCellEdges[yCellEdgeIndex]);
            }
        }

        for (int yCellIndex = 0; yCellIndex < yCellCount; ++yCellIndex) {
            int yCellOffset0 = yCellIndex * xCellCount;
            int yCellOffset1 = (yCellIndex + 1) * xCellCount;
            for (int xCellIndex = 0; xCellIndex < xCellCount; ++xCellIndex) {
                Envelope cellEnvelope = new Envelope(
                        cellEdgeCoordindate[yCellOffset0 + xCellIndex],
                        cellEdgeCoordindate[yCellOffset1 + (xCellIndex + 1)]);
                cellGeometry[yCellOffset0 + xCellIndex] =
                        geometryFactory.toGeometry(cellEnvelope);
            }
        }

        return cellGeometry;
    }

    private class CoordinateBuilder {

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
}
