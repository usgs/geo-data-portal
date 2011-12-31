package gov.usgs.derivative.grid;

import com.google.common.base.Preconditions;
import gov.usgs.cida.gdp.coreprocessing.analysis.grid.GridType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.CoordinateAxis1DTime;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDatatype;

/**
 *
 * @author tkunicki
 */
public class GridTraverser {

    private final static Logger LOGGER = LoggerFactory.getLogger(GridTraverser.class);

    // used for GridDataType.readDataSlice(...) calls.  The docs say that
    // if a dimension fails to exist the value is ignored and if it does
    // exist and the index is < 0 all data is returned.  For non-existant
    // indices we are going to pass in Integer.MAX_VALUE so that if the
    // dimension exists (i.e. we misread grid coordinate system) we force
    // an exception to be thrown...
    private final static int INVALID_INDEX = Integer.MAX_VALUE;
    private final static Range INVALID_RANGE;
    static {
        Range r = null;
        try {
            r= new Range(INVALID_INDEX, INVALID_INDEX);
        } catch (InvalidRangeException e) {
            // this really shouldn't be a checked exception
        }
        INVALID_RANGE = r;
    }
    
    private final List<GridDatatype> gridDatatypeList;

    final GridType gridType;

    private final Range tRange;
    private final Range zRange;
    
    public GridTraverser(GridDatatype gridDatatype) {
        this(Arrays.asList(new GridDatatype[] { gridDatatype }));
    }
    
    public GridTraverser(List<GridDatatype> gridDatatypeList) {
        
        Preconditions.checkNotNull(gridDatatypeList);
        Preconditions.checkArgument(gridDatatypeList.size() > 0);
        
        for (GridDatatype gridDatatype : gridDatatypeList) {
            Preconditions.checkNotNull(gridDatatype);
        }
        GridCoordSystem gridCoordSystem = gridDatatypeList.get(0).getCoordinateSystem();
        for (GridDatatype gridDatatype : gridDatatypeList) {
            gridDatatype.getCoordinateSystem().equals(gridCoordSystem);
        }
        
        this.gridDatatypeList = Collections.unmodifiableList(gridDatatypeList);
        

        gridType = GridType.findGridType(gridCoordSystem);
        
        if (gridType == GridType.OTHER) {
            throw new IllegalArgumentException("Unable to traverse this grid type.");
        }

        CoordinateAxis zAxis = gridCoordSystem.getVerticalAxis();
        CoordinateAxis1DTime tAxis = gridCoordSystem.getTimeAxis1D();

        zRange = zAxis == null ? INVALID_RANGE : zAxis.getRanges().get(0);
        tRange = tAxis == null ? INVALID_RANGE : tAxis.getRanges().get(0);
    }

    public void traverse(GridVisitor visitor) throws IOException {
        traverse(Arrays.asList(new GridVisitor[] { visitor }));
    }
    
    public void traverse(List<? extends GridVisitor> visitorList) throws IOException {
        if (gridType == GridType.OTHER) {
            throw new IllegalStateException("Unable to traverse this grid type.");
        }
        
        for (GridVisitor visitor : visitorList) {
            visitor.traverseStart(gridDatatypeList);
        }
        
        // NOTE:  the >= 0 test on indices is important.  We use INVALID_INDEX = Ineger.MAX_VALUE
        // adding 1 to this will cause a rollover, which is ok *if* we expect it.
        for (int tCellIndex = tRange.first(); tCellIndex >= 0 && tCellIndex <= tRange.last(); tCellIndex += tRange.stride()) {
            ArrayList<GridVisitor> tVisitorList = new ArrayList<GridVisitor>(visitorList);
            if (tCellIndex != INVALID_INDEX) {
                for (GridVisitor visitor : visitorList) {
                    if (!visitor.tStart(tCellIndex) || !visitor.traverseContinue()) {
                        tVisitorList.remove(visitor);
                    }
                }
            }
            if (!tVisitorList.isEmpty()) {
                for (int zCellIndex = zRange.first(); zCellIndex >= 0 && zCellIndex <= zRange.last(); zCellIndex += zRange.stride()) {
                    ArrayList<GridVisitor> tzVisitorList = new ArrayList<GridVisitor>(tVisitorList);
                    if (zCellIndex != INVALID_INDEX) {
                        for (GridVisitor tVisitor : tVisitorList) {
                            if (!tVisitor.zStart(zCellIndex) || !tVisitor.traverseContinue()) {
                                tzVisitorList.remove(tVisitor);
                            }
                        }
                    }
                    if (!tzVisitorList.isEmpty()) {
                        doTraverseXY(tzVisitorList, tCellIndex, zCellIndex);
                        for (GridVisitor tzVisitor : tzVisitorList) {
                            tzVisitor.zEnd(zCellIndex);
                        }
                    }
                }
                for (GridVisitor tVisitor : tVisitorList) {
                    tVisitor.tEnd(tCellIndex);
                }
            }
        }
        for (GridVisitor visitor : visitorList) {
            visitor.traverseEnd();
        }
    }

    protected Array readDataSlice(GridDatatype gridDatatype, int t_index, int z_index) throws java.io.IOException {
        int failures = 0;
        Array slice = null;
        while (slice == null) {
            try {
                slice = gridDatatype.readDataSlice(t_index, z_index, -1, -1);
            } catch (IOException e) {
                if (failures++ < 3) {
                    LOGGER.warn("Error reading slice [t={}, z={}] from {}: failure {}, reattempting.  Exception was {}",
                            new Object[] {t_index, z_index, gridDatatype.getDescription(), failures, e});
                } else {
                    LOGGER.error("Unable to read slice [t={}, z={}] from {} after {} failures. Exception was {}",
                            new Object[] {t_index, z_index, gridDatatype.getDescription(), failures, e});
                    throw e;
                }
            }
        }
        return slice;
    }
    
    protected void doTraverseXY(List<GridVisitor> visitors, int tCellIndex, int zCellIndex) throws IOException {
        List<float[]> yxSliceData = new ArrayList<float[]>(gridDatatypeList.size());
        for (GridDatatype gridDatatype : gridDatatypeList) {
            Array array = readDataSlice(gridDatatype, tCellIndex, zCellIndex);
            yxSliceData.add((float[])array.get1DJavaArray(float.class));
        }
        yxSliceData = Collections.unmodifiableList(yxSliceData);
        for (GridVisitor visitor : visitors) {
            visitor.yxStart(yxSliceData);
            visitor.yxEnd();
        }
    }
}
