package gov.usgs.cida.gdp.coreprocessing.analysis.grid;

import com.vividsolutions.jts.geom.Coordinate;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.geometry.BoundingBox;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;
import ucar.ma2.ArrayDouble;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.CoordinateAxis1D;
import ucar.nc2.dataset.CoordinateAxis2D;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDatatype;
import ucar.unidata.geoloc.LatLonPointImpl;
import ucar.unidata.geoloc.LatLonRect;
import ucar.unidata.geoloc.Projection;
import ucar.unidata.geoloc.ProjectionPointImpl;
import ucar.unidata.geoloc.ProjectionRect;

/**
 *
 * @author tkunicki
 */
public abstract class GridUtility {

    private GridUtility() {}

	public static BoundingBox getBoundingBox(GridDatatype gdt) {
		return getBoundingBox(gdt.getCoordinateSystem());
	}

	public static BoundingBox getBoundingBox(GridCoordSystem gcs) {
		CoordinateReferenceSystem gridCRS = CRSUtility.getCRSFromGridCoordSystem(gcs);
		ProjectionRect rect = gcs.getBoundingBox();
		return new ReferencedEnvelope(
				rect.getMinX(),
				rect.getMaxX(),
				rect.getMinY(),
				rect.getMaxY(),
				gridCRS);
	}

    public static Range[] getXYRangesFromBoundingBox(BoundingBox bounds, GridCoordSystem gcs)
            throws InvalidRangeException, TransformException, FactoryException {
        return getXYRangesFromBoundingBox(bounds, gcs, true);
    }
    
	public static Range[] getXYRangesFromBoundingBox(BoundingBox bounds, GridCoordSystem gcs, boolean requireFullCoverage)
			throws InvalidRangeException, TransformException, FactoryException {

        CoordinateReferenceSystem gridCRS = CRSUtility.getCRSFromGridCoordSystem(gcs);

        bounds = bounds.toBounds(gridCRS);

        double[][] coords = {
            { bounds.getMinX(), bounds.getMinY() },
            { bounds.getMinX(), bounds.getMaxY() },
            { bounds.getMaxX(), bounds.getMaxY() },
            { bounds.getMaxX(), bounds.getMinY() },
        };
        int[] currentIndices = new int[2];
        int lowerX = Integer.MAX_VALUE;
        int upperX = Integer.MIN_VALUE;
        int lowerY = Integer.MAX_VALUE;
        int upperY = Integer.MIN_VALUE;

        for (int i = 0; i < coords.length; ++i) {
            gcs.findXYindexFromCoord(coords[i][0], coords[i][1], currentIndices);
            if (currentIndices[0] < lowerX) { lowerX = currentIndices[0] ; }
            if (currentIndices[0] > upperX) { upperX = currentIndices[0] ; }
            if (currentIndices[1] < lowerY) { lowerY = currentIndices[1] ; }
            if (currentIndices[1] > upperY) { upperY = currentIndices[1] ; }
        }

        if ((lowerX < 0 && upperX < 0) || (lowerY < 0 && upperY < 0)) {
            throw new InvalidRangeException("Grid doesn't intersect bounding box.");
        }
        
        if (requireFullCoverage) {
            if (lowerX < 0 || upperX < 0 || lowerY < 0 || upperY < 0) {
                throw new InvalidRangeException("Grid doesn't cover bounding box.");
            }
        } else {
            if (lowerX < 0) lowerX = 0;
            if (upperX < 0) upperX = gcs.getXHorizAxis().getShape(gcs.getXHorizAxis().getRank() - 1) - 1;
            if (lowerY < 0) lowerY = 0;
            if (upperY < 0) upperY = gcs.getYHorizAxis().getShape(0) - 1;
        }
       
        return bufferXYRanges(gcs, new Range[] {
            new Range(lowerX, upperX),
            new Range(lowerY, upperY),
        } );
    }

    // Handle bug in NetCDF 4.1 for X and Y CoordinateAxis2D with
    // shapefile bound (LatLonRect) that doesn't interect any grid center
    // (aka midpoint)
	@Deprecated
    public static Range[] getXYRangesFromLatLonRect(LatLonRect llr, GridCoordSystem gcs)
            throws InvalidRangeException
    {

		double[][] coords = {
			{ llr.getLatMin(), llr.getLonMin() },
			{ llr.getLatMin(), llr.getLonMax() },
			{ llr.getLatMax(), llr.getLonMax() },
			{ llr.getLatMax(), llr.getLonMin() },
		};
		int[] currentIndices = new int[2];
		int lowerX = Integer.MAX_VALUE;
		int upperX = Integer.MIN_VALUE;
		int lowerY = Integer.MAX_VALUE;
		int upperY = Integer.MIN_VALUE;
		for (int i = 0; i < coords.length; ++i) {
			// seem to need this for CoordinateAxis2D instances
			gcs.findXYindexFromLatLon(coords[i][0], coords[i][1], currentIndices);
			if (currentIndices[0] < lowerX) { lowerX = currentIndices[0] ; }
			if (currentIndices[0] > upperX) { upperX = currentIndices[0] ; }
			if (currentIndices[1] < lowerY) { lowerY = currentIndices[1] ; }
			if (currentIndices[1] > upperY) { upperY = currentIndices[1] ; }
		}

		return bufferXYRanges(gcs, new Range[] {
            new Range(lowerX, upperX),
            new Range(lowerY, upperY),
        } );
    }

    // Handle bugs in NetCDF 4.1 for X or Y CoordinateAxis with < 3 grid cells 
    // in any dimension, Edges being were incorrectly calculated without 
    // addition of buffer cells.
	public static Range[] bufferXYRanges(GridCoordSystem gcs, Range[] ranges) throws InvalidRangeException {
		int lowerX = ranges[0].first();
		int upperX = ranges[0].last();
		int lowerY = ranges[1].first();
		int upperY = ranges[1].last();

		// Buffer X dimension, need minimum source width of 3, otherwise grid cell width calc fails.
        // NOTE: NetCDF ranges are upper edge inclusive
        int deltaX = upperX - lowerX;
        CoordinateAxis xAxis = gcs.getXHorizAxis();
        int maxX = xAxis.getShape(xAxis.getRank() - 1) - 1; // inclusive
        if (maxX < 2) {
            throw new InvalidRangeException("Source grid too small");
        }
        if (deltaX == 0) {
            if (lowerX > 0 && upperX < maxX) {
                --lowerX;
                ++upperX;
            } else {
                // we're on an border cell
                if (lowerX == 0) {
                    upperX = 2;
                } else {
                    lowerX = maxX - 2;
                }
            }
        } else {
            if (lowerX > 0) { --lowerX; }
            if (upperX < maxX) { ++upperX; }
        }

        // Buffer Y dimension, need minimum source width of 3, otherwise grid cell width calc fails.
        // NOTE: NetCDF ranges are upper edge inclusive
        int deltaY = upperY - lowerY;
        CoordinateAxis yAxis = gcs.getYHorizAxis();
        int maxY = yAxis.getShape(0)  - 1 ; // inclusive
        if (maxY < 2) {
            throw new InvalidRangeException("Source grid too small");
        }
        if (deltaY == 0) {
            if (lowerY > 0 && upperY < maxY) {
                --lowerY;
                ++upperY;
            } else {
                // we're on an border cell
                if (lowerY == 0) {
                    upperY = 2;
                } else {
                    lowerY = maxY - 2;
                }
            }
        } else {
            if (lowerY > 0) { --lowerY; }
            if (upperY < maxY) { ++upperY; }
        }

        return new Range[] {
            new Range(lowerX, upperX),
            new Range(lowerY, upperY),
        };

	}

    @Deprecated
    public static CoordinateBuilder generateCoordinateBuilder(GridCoordSystem gridCoordSystem) {
        return gridCoordSystem.isLatLon()
                ? new CoordinateBuilder()
                : new ProjectedCoordinateBuilder(gridCoordSystem);
    }

    public static GridCellCenterAdapter generateGridCellCenterAdapterX(GridCoordSystem gridCoordSystem) {
        CoordinateAxis axis = gridCoordSystem.getXHorizAxis();
        if (axis instanceof CoordinateAxis1D) {
            return new GridCellCenterAdapterAxis1DX((CoordinateAxis1D)axis);
        } else if (axis instanceof CoordinateAxis2D) {
            return new GridCellCenterAdapterAxis2DX((CoordinateAxis2D)axis);
        } else {
            throw new IllegalStateException("Unknown coordinate axis type");
        }
    }

    public static GridCellCenterAdapter generateGridCellCenterAdapterY(GridCoordSystem gridCoordSystem) {
        CoordinateAxis axis = gridCoordSystem.getYHorizAxis();
        if (axis instanceof CoordinateAxis1D) {
            return new GridCellCenterAdapterAxis1DY((CoordinateAxis1D)axis);
        } else if (axis instanceof CoordinateAxis2D) {
            return new GridCellCenterAdapterAxis2DY((CoordinateAxis2D)axis);
        } else {
            throw new IllegalStateException("Unknown coordinate axis type");
        }
    }

    public static GridCellEdgeAdapter generateGridCellEdgeAdapterX(GridCoordSystem gridCoordSystem) {
        CoordinateAxis axis = gridCoordSystem.getXHorizAxis();
        if (axis instanceof CoordinateAxis1D) {
            return new GridCellEdgeAdapterAxis1DX((CoordinateAxis1D)axis);
        } else if (axis instanceof CoordinateAxis2D) {
            return new GridCellEdgeAdapterAxis2DX((CoordinateAxis2D)axis);
        } else {
            throw new IllegalStateException("Unknown coordinate axis type");
        }
    }

    public static GridCellEdgeAdapter generateGridCellEdgeAdapterY(GridCoordSystem gridCoordSystem) {
        CoordinateAxis axis = gridCoordSystem.getYHorizAxis();
        if (axis instanceof CoordinateAxis1D) {
            return new GridCellEdgeAdapterAxis1DY((CoordinateAxis1D)axis);
        } else if (axis instanceof CoordinateAxis2D) {
            return new GridCellEdgeAdapterAxis2DY((CoordinateAxis2D)axis);
        } else {
            throw new IllegalStateException("Unknown coordinate axis type");
        }
    }

    @Deprecated
    public static IndexToCoordinateBuilder generateIndexToCellCenterLatLonCoordinateBuilder(GridCoordSystem gridCoordSystem) {
        return new IndexToCoordinateBuilder(
                generateCoordinateBuilder(gridCoordSystem),
                generateGridCellCenterAdapterX(gridCoordSystem),
                generateGridCellCenterAdapterY(gridCoordSystem));
    }

    @Deprecated
    public static IndexToCoordinateBuilder generateIndexToCellEdgeLatLonCoordinateBuilder(GridCoordSystem gridCoordSystem) {
        return new IndexToCoordinateBuilder(
                generateCoordinateBuilder(gridCoordSystem),
                generateGridCellEdgeAdapterX(gridCoordSystem),
                generateGridCellEdgeAdapterY(gridCoordSystem));
    }

	public static IndexToCoordinateBuilder generateIndexToCellCenterCoordinateBuilder(GridCoordSystem gridCoordSystem) {
		return new IndexToCoordinateBuilder(
                new CoordinateBuilder(),
                generateGridCellEdgeAdapterX(gridCoordSystem),
                generateGridCellEdgeAdapterY(gridCoordSystem));
	}

	public static IndexToCoordinateBuilder generateIndexToCellEdgeCoordinateBuilder(GridCoordSystem gridCoordSystem) {
		return new IndexToCoordinateBuilder(
                new CoordinateBuilder(),
                generateGridCellEdgeAdapterX(gridCoordSystem),
                generateGridCellEdgeAdapterY(gridCoordSystem));
	}

    public static class IndexToCoordinateBuilder {

        protected final CoordinateBuilder coordinateBuilder;
        protected final XYIndexToAxisValueAdapter xValueAdapter;
        protected final XYIndexToAxisValueAdapter yValueAdapter;

        public IndexToCoordinateBuilder(CoordinateBuilder coordinateBuilder,
                XYIndexToAxisValueAdapter xValueAdapter,
                XYIndexToAxisValueAdapter yValueAdapter) {
            this.coordinateBuilder = coordinateBuilder;
            this.xValueAdapter = xValueAdapter;
            this.yValueAdapter = yValueAdapter;
        }

        public Coordinate getCoordinate(int xIndex, int yIndex) {
            return coordinateBuilder.getCoordinate(
                    xValueAdapter.getValue(xIndex, yIndex),
                    yValueAdapter.getValue(xIndex, yIndex));
        }

        public int getXIndexCount() {
            return xValueAdapter.getValueCount();
        }

        public int getYIndexCount() {
            return yValueAdapter.getValueCount();
        }
    }

    public static class CoordinateBuilder {

        public Coordinate getCoordinate(double x, double y) {
            return new Coordinate(x, y);
        }
    }

    @Deprecated
    public static class ProjectedCoordinateBuilder extends CoordinateBuilder {

        private final Projection projection;
        private final ProjectionPointImpl projectionPoint;
        private final LatLonPointImpl latLonPoint;

        public ProjectedCoordinateBuilder(GridCoordSystem gridCoordSystem) {
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

    public interface XYIndexToAxisValueAdapter {
        public int getValueCount();
        public double getValue(int xIndex, int yIndex);
    }

    public interface GridCellCenterAdapter extends XYIndexToAxisValueAdapter { }

    public interface GridCellEdgeAdapter extends XYIndexToAxisValueAdapter { }

    public abstract static class GridCellCenterAdapterAxis1D implements GridCellCenterAdapter {

        protected final double[] cellCenters;

        public GridCellCenterAdapterAxis1D(CoordinateAxis1D axis1D) {
            cellCenters = axis1D.getCoordValues();
        }

        @Override
        public int getValueCount() {
            return cellCenters.length;
        }
    }

    public static class GridCellCenterAdapterAxis1DX extends GridCellCenterAdapterAxis1D {

        public GridCellCenterAdapterAxis1DX(CoordinateAxis1D axis1D) {
            super(axis1D);
        }

        @Override
        public double getValue(int xCellIndex, int yCellIndex) {
            return cellCenters[xCellIndex];
        }
    }

    public static class GridCellCenterAdapterAxis1DY extends GridCellCenterAdapterAxis1D {

        public GridCellCenterAdapterAxis1DY(CoordinateAxis1D axis1D) {
            super(axis1D);
        }

        @Override
        public double getValue(int xCellIndex, int yCellIndex) {
            return cellCenters[yCellIndex];
        }
    }

    public abstract static class GridCellCenterAdapterAxis2D implements GridCellCenterAdapter {

        protected final ArrayDouble.D2 cellCenters;

        public GridCellCenterAdapterAxis2D(CoordinateAxis2D axis2D) {
            cellCenters = axis2D.getMidpoints();
        }

        @Override
        public double getValue(int xCellIndex, int yCellIndex) {
            // NOTE: argument order swap...
            return cellCenters.get(yCellIndex, xCellIndex);
        }
    }

    public static class GridCellCenterAdapterAxis2DX extends GridCellCenterAdapterAxis2D {

        public GridCellCenterAdapterAxis2DX(CoordinateAxis2D axis2D) {
            super(axis2D);
        }

        @Override
        public int getValueCount() {
            return cellCenters.getShape()[1];
        }
    }

    public static class GridCellCenterAdapterAxis2DY extends GridCellCenterAdapterAxis2D {

        public GridCellCenterAdapterAxis2DY(CoordinateAxis2D axis2D) {
            super(axis2D);
        }

        @Override
        public int getValueCount() {
            return cellCenters.getShape()[0];
        }
    }

    public abstract static class GridCellEdgeAdapterAxis1D implements GridCellEdgeAdapter {

        protected final double[] cellEdges;

        public GridCellEdgeAdapterAxis1D(CoordinateAxis1D axis1D) {
            cellEdges = axis1D.getCoordEdges();
        }

        @Override
        public int getValueCount() {
           return cellEdges.length;
        }
    }

    public static class GridCellEdgeAdapterAxis1DX extends GridCellEdgeAdapterAxis1D {

        public GridCellEdgeAdapterAxis1DX(CoordinateAxis1D axis1D) {
            super(axis1D);
        }

        @Override
        public double getValue(int xCellEdgeIndex, int yCellEdgeIndex) {
            return cellEdges[xCellEdgeIndex];
        }
    }

    public static class GridCellEdgeAdapterAxis1DY extends GridCellEdgeAdapterAxis1D {

        public GridCellEdgeAdapterAxis1DY(CoordinateAxis1D axis1D) {
            super(axis1D);
        }

        @Override
        public double getValue(int xCellEdgeIndex, int yCellEdgeIndex) {
            return cellEdges[yCellEdgeIndex];
        }
    }

    public abstract static class GridCellEdgeAdapterAxis2D implements GridCellEdgeAdapter {

        protected final ArrayDouble.D2 cellEdges;

        public GridCellEdgeAdapterAxis2D(CoordinateAxis2D axis2D) {
            cellEdges = CoordinateAxis2D.makeXEdges(axis2D.getMidpoints());
        }

        @Override
        public double getValue(int xCellEdgeIndex, int yCellEdgeIndex) {
            // NOTE: argument order swap...
            return cellEdges.get(yCellEdgeIndex, xCellEdgeIndex);
        }
    }

    public static class GridCellEdgeAdapterAxis2DX extends GridCellEdgeAdapterAxis2D {

        public GridCellEdgeAdapterAxis2DX(CoordinateAxis2D axis2D) {
            super(axis2D);
        }

        @Override
        public int getValueCount() {
           return cellEdges.getShape()[1];
        }
    }

    public static class GridCellEdgeAdapterAxis2DY extends GridCellEdgeAdapterAxis2D {

        public GridCellEdgeAdapterAxis2DY(CoordinateAxis2D axis2D) {
            super(axis2D);
        }

        @Override
        public int getValueCount() {
           return cellEdges.getShape()[0];
        }
    }

}
