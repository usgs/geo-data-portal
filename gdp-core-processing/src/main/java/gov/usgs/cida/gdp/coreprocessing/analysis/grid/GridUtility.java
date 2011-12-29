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
    
    public static int getXAxisLength(GridCoordSystem gcs) {
        if (!checkXYAxisIsValid(gcs.getXHorizAxis())) {
            throw new IllegalArgumentException("Grid Coordinate System does not contain valid X axis");
        }
        return getXAxisLengthQuick(gcs);
    }
    
    public static int getYAxisLength(GridCoordSystem gcs) {
        if (!checkXYAxisIsValid(gcs.getYHorizAxis())) {
            throw new IllegalArgumentException("Grid Coordinate System does not contain valid X axis");
        }
        return getYAxisLengthQuick(gcs);
    }
    
    public static Range getXAxisRange(GridCoordSystem gcs) {
        if (!checkXYAxisIsValid(gcs.getXHorizAxis())) {
            throw new IllegalArgumentException("Grid Coordinate System does not contain valid X axis");
        }
        return getXAxisRangeQuick(gcs);
    }
    
    public static Range getYAxisRange(GridCoordSystem gcs) {
        if (!checkXYAxisIsValid(gcs.getYHorizAxis())) {
            throw new IllegalArgumentException("Grid Coordinate System does not contain valid X axis");
        }
        return getYAxisRangeQuick(gcs);
    }
    
    private static boolean checkXYAxisIsValid(CoordinateAxis axis) {
        return (axis instanceof CoordinateAxis1D || axis instanceof CoordinateAxis2D);
    }
    
    public static int getXAxisLengthQuick(GridCoordSystem gcs) {
        return gcs.getXHorizAxis().getShape(gcs.getXHorizAxis().getRank() - 1);
    }
    
    public static int getYAxisLengthQuick(GridCoordSystem gcs) {
        return gcs.getYHorizAxis().getShape(0);
    }
    
    public static Range getXAxisRangeQuick(GridCoordSystem gcs) {
        return gcs.getXHorizAxis().getRanges().get(gcs.getXHorizAxis().getRank() - 1);
    }
    
    public static Range getYAxisRangeQuick(GridCoordSystem gcs) {
        return gcs.getYHorizAxis().getRanges().get(0);
    }

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

        ProjectionRect gcsProjectionRect = gcs.getBoundingBox();
        if (!gcsProjectionRect.intersects(bounds.getMinX(), bounds.getMinY(), bounds.getWidth(), bounds.getHeight())) {
            throw new InvalidRangeException("Grid doesn't intersect bounding box.");
        }
        
        if (requireFullCoverage && !gcsProjectionRect.contains(bounds.getMinX(), bounds.getMinY(), bounds.getWidth(), bounds.getHeight())) {
            throw new InvalidRangeException("Grid doesn't cover bounding box.");
        }
        
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
            if (requireFullCoverage) {
                // If a value is outside the gridded data set a value of -1 is returned.
                // This should have been caught by the tests above but we double check
                // for this state below.
                gcs.findXYindexFromCoord(coords[i][0], coords[i][1], currentIndices);
            } else {
                // This will snap any value outside the grid to the nearest grid cell index.
                // We don't do this ourselves as we need to know the sign of the grid cell deltas
                // for a 1D coordinate axis.  For a 2D coordinate axis this is even more difficult
                // as deltas are most likely vector quantities (and in some case the representation
                // of a sampled function where the vector direction is not constant).  We have
                // to trust NetCDF-Java here...
                gcs.findXYindexFromCoordBounded(coords[i][0], coords[i][1], currentIndices);
            }
            if (currentIndices[0] < lowerX) { lowerX = currentIndices[0] ; }
            if (currentIndices[0] > upperX) { upperX = currentIndices[0] ; }
            if (currentIndices[1] < lowerY) { lowerY = currentIndices[1] ; }
            if (currentIndices[1] > upperY) { upperY = currentIndices[1] ; }
        }

        // redunandant, but keep as double check
        if (requireFullCoverage && (lowerX < 0 || upperX < 0 || lowerY < 0 || upperY < 0)) {
            throw new InvalidRangeException("Grid doesn't cover bounding box.");
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
    // in any dimension, Edges being were inconsistently calculated without 
    // addition of buffer cells.
	public static Range[] bufferXYRanges(GridCoordSystem gcs, Range[] ranges) throws InvalidRangeException {
		int lowerX = ranges[0].first();
		int upperX = ranges[0].last();
		int lowerY = ranges[1].first();
		int upperY = ranges[1].last();

		// Buffer X dimension, need minimum source width of 3, otherwise grid cell width calc fails.
        // NOTE: NetCDF ranges are upper edge inclusive
        int deltaX = upperX - lowerX;
        int maxX = getXAxisLengthQuick(gcs) - 1; // inclusive
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
        int maxY = getYAxisLengthQuick(gcs) - 1 ; // inclusive
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

        private final CoordinateBuilder coordinateBuilder;
        private final XYIndexToAxisValueAdapter xValueAdapter;
        private final XYIndexToAxisValueAdapter yValueAdapter;

        private IndexToCoordinateBuilder(CoordinateBuilder coordinateBuilder,
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
    
    @Deprecated
    private static CoordinateBuilder generateCoordinateBuilder(GridCoordSystem gridCoordSystem) {
        return gridCoordSystem.isLatLon()
                ? new CoordinateBuilder()
                : new ProjectedCoordinateBuilder(gridCoordSystem);
    }

    private static GridCellCenterAdapter generateGridCellCenterAdapterX(GridCoordSystem gridCoordSystem) {
        CoordinateAxis axis = gridCoordSystem.getXHorizAxis();
        if (axis instanceof CoordinateAxis1D) {
            return new GridCellCenterAdapterAxis1DX((CoordinateAxis1D)axis);
        } else if (axis instanceof CoordinateAxis2D) {
            return new GridCellCenterAdapterAxis2DX((CoordinateAxis2D)axis);
        } else {
            throw new IllegalStateException("Unknown coordinate axis type");
        }
    }

    private static GridCellCenterAdapter generateGridCellCenterAdapterY(GridCoordSystem gridCoordSystem) {
        CoordinateAxis axis = gridCoordSystem.getYHorizAxis();
        if (axis instanceof CoordinateAxis1D) {
            return new GridCellCenterAdapterAxis1DY((CoordinateAxis1D)axis);
        } else if (axis instanceof CoordinateAxis2D) {
            return new GridCellCenterAdapterAxis2DY((CoordinateAxis2D)axis);
        } else {
            throw new IllegalStateException("Unknown coordinate axis type");
        }
    }

    private static GridCellEdgeAdapter generateGridCellEdgeAdapterX(GridCoordSystem gridCoordSystem) {
        CoordinateAxis axis = gridCoordSystem.getXHorizAxis();
        if (axis instanceof CoordinateAxis1D) {
            return new GridCellEdgeAdapterAxis1DX((CoordinateAxis1D)axis);
        } else if (axis instanceof CoordinateAxis2D) {
            return new GridCellEdgeAdapterAxis2DX((CoordinateAxis2D)axis);
        } else {
            throw new IllegalStateException("Unknown coordinate axis type");
        }
    }

    private static GridCellEdgeAdapter generateGridCellEdgeAdapterY(GridCoordSystem gridCoordSystem) {
        CoordinateAxis axis = gridCoordSystem.getYHorizAxis();
        if (axis instanceof CoordinateAxis1D) {
            return new GridCellEdgeAdapterAxis1DY((CoordinateAxis1D)axis);
        } else if (axis instanceof CoordinateAxis2D) {
            return new GridCellEdgeAdapterAxis2DY((CoordinateAxis2D)axis);
        } else {
            throw new IllegalStateException("Unknown coordinate axis type");
        }
    }

    private static class CoordinateBuilder {

        public Coordinate getCoordinate(double x, double y) {
            return new Coordinate(x, y);
        }
    }

    @Deprecated
    private static class ProjectedCoordinateBuilder extends CoordinateBuilder {

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

    private interface XYIndexToAxisValueAdapter {
        public int getValueCount();
        public double getValue(int xIndex, int yIndex);
    }

    private interface GridCellCenterAdapter extends XYIndexToAxisValueAdapter { }

    private interface GridCellEdgeAdapter extends XYIndexToAxisValueAdapter { }

    private abstract static class GridCellCenterAdapterAxis1D implements GridCellCenterAdapter {

        protected final double[] cellCenters;

        public GridCellCenterAdapterAxis1D(CoordinateAxis1D axis1D) {
            cellCenters = axis1D.getCoordValues();
        }

        @Override
        public int getValueCount() {
            return cellCenters.length;
        }
    }

    private static class GridCellCenterAdapterAxis1DX extends GridCellCenterAdapterAxis1D {

        public GridCellCenterAdapterAxis1DX(CoordinateAxis1D axis1D) {
            super(axis1D);
        }

        @Override
        public double getValue(int xCellIndex, int yCellIndex) {
            return cellCenters[xCellIndex];
        }
    }

    private static class GridCellCenterAdapterAxis1DY extends GridCellCenterAdapterAxis1D {

        public GridCellCenterAdapterAxis1DY(CoordinateAxis1D axis1D) {
            super(axis1D);
        }

        @Override
        public double getValue(int xCellIndex, int yCellIndex) {
            return cellCenters[yCellIndex];
        }
    }

    private abstract static class GridCellCenterAdapterAxis2D implements GridCellCenterAdapter {

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

    private static class GridCellCenterAdapterAxis2DX extends GridCellCenterAdapterAxis2D {

        public GridCellCenterAdapterAxis2DX(CoordinateAxis2D axis2D) {
            super(axis2D);
        }

        @Override
        public int getValueCount() {
            return cellCenters.getShape()[1];
        }
    }

    private static class GridCellCenterAdapterAxis2DY extends GridCellCenterAdapterAxis2D {

        public GridCellCenterAdapterAxis2DY(CoordinateAxis2D axis2D) {
            super(axis2D);
        }

        @Override
        public int getValueCount() {
            return cellCenters.getShape()[0];
        }
    }

    private abstract static class GridCellEdgeAdapterAxis1D implements GridCellEdgeAdapter {

        protected final double[] cellEdges;

        public GridCellEdgeAdapterAxis1D(CoordinateAxis1D axis1D) {
            cellEdges = axis1D.getCoordEdges();
        }

        @Override
        public int getValueCount() {
           return cellEdges.length;
        }
    }

    private static class GridCellEdgeAdapterAxis1DX extends GridCellEdgeAdapterAxis1D {

        public GridCellEdgeAdapterAxis1DX(CoordinateAxis1D axis1D) {
            super(axis1D);
        }

        @Override
        public double getValue(int xCellEdgeIndex, int yCellEdgeIndex) {
            return cellEdges[xCellEdgeIndex];
        }
    }

    private static class GridCellEdgeAdapterAxis1DY extends GridCellEdgeAdapterAxis1D {

        public GridCellEdgeAdapterAxis1DY(CoordinateAxis1D axis1D) {
            super(axis1D);
        }

        @Override
        public double getValue(int xCellEdgeIndex, int yCellEdgeIndex) {
            return cellEdges[yCellEdgeIndex];
        }
    }

    private abstract static class GridCellEdgeAdapterAxis2D implements GridCellEdgeAdapter {

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

    private static class GridCellEdgeAdapterAxis2DX extends GridCellEdgeAdapterAxis2D {

        public GridCellEdgeAdapterAxis2DX(CoordinateAxis2D axis2D) {
            super(axis2D);
        }

        @Override
        public int getValueCount() {
           return cellEdges.getShape()[1];
        }
    }

    private static class GridCellEdgeAdapterAxis2DY extends GridCellEdgeAdapterAxis2D {

        public GridCellEdgeAdapterAxis2DY(CoordinateAxis2D axis2D) {
            super(axis2D);
        }

        @Override
        public int getValueCount() {
           return cellEdges.getShape()[0];
        }
    }

}
