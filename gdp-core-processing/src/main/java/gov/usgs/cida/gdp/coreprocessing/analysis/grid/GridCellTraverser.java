/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.usgs.cida.gdp.coreprocessing.analysis.grid;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.CoordinateAxis1DTime;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDatatype;

/**
 *
 * @author tkunicki
 */
public class GridCellTraverser {

    private final static Logger LOGGER = LoggerFactory.getLogger(GridCellTraverser.class);

    // used for GridDataType.readDataSlice(...) calls.  The docs say that
    // if a dimension fails to exist the value is ignored and if it does
    // exist and the index is < 0 all data is returned.  For non-existant
    // indices we are going to pass in Integer.MAX_VALUE so that if the
    // dimension exists (i.e. we misread grid coordinate system) we force
    // an exception to be thrown...
    private final static int INVALID_INDEX = Integer.MAX_VALUE;
    
    private final GridDatatype gridDataType;

    private final GridType gridType;

    protected final int xCellCount;
    protected final int yCellCount;
    protected final int zCellCount;
    protected final int tCellCount;
    
    public GridCellTraverser(GridDatatype gridDatatype) {

        this.gridDataType = gridDatatype;

        GridCoordSystem gridCoordSystem = gridDatatype.getCoordinateSystem();

        gridType = GridType.findGridType(gridCoordSystem);
        
        if (gridType == GridType.OTHER) {
            throw new IllegalArgumentException("Unable to traverse this grid type.");
        }

        CoordinateAxis xAxis = gridCoordSystem.getXHorizAxis();
        CoordinateAxis yAxis = gridCoordSystem.getYHorizAxis();
        CoordinateAxis zAxis = gridCoordSystem.getVerticalAxis();
        CoordinateAxis1DTime tAxis = gridCoordSystem.getTimeAxis1D();

        // will handle both CoordinateAxis1D or CoordinateAxis2D for x and y
        xCellCount = xAxis.getShape(xAxis.getRank() - 1);
        yCellCount = yAxis.getShape(0);
        zCellCount = zAxis == null ? 0 : zAxis.getShape(0);
        tCellCount = tAxis == null ? 0 : tAxis.getShape(0);
    }

    public void traverse(GridCellVisitor visitor) throws IOException {

        if (gridType == GridType.OTHER) {
            throw new IllegalStateException("Unable to traverse this grid type.");
        }

        visitor.traverseStart(gridDataType.getCoordinateSystem());
        if (gridType == GridType.YX) {
            Array array = readDataSlice(INVALID_INDEX, INVALID_INDEX);
            doTraverseXY(visitor, array);
        } else if (gridType == GridType.ZYX) {
            for (int zCellIndex = 0; zCellIndex < zCellCount; ++zCellIndex) {
                visitor.zStart(zCellIndex);
                Array array = readDataSlice(INVALID_INDEX, zCellIndex);
                doTraverseXY(visitor, array);
                visitor.zEnd(zCellIndex);
            }
        } else if (gridType == GridType.TYX) {
            for (int tCellIndex = 0; tCellIndex < tCellCount; ++tCellIndex) {
                visitor.tStart(tCellIndex);
                Array array = readDataSlice(tCellIndex, INVALID_INDEX);
                doTraverseXY(visitor, array);
                visitor.tEnd(tCellIndex);
            }
        } else if (gridType == GridType.TZYX) {
            for (int tCellIndex = 0; tCellIndex < tCellCount; ++tCellIndex) {
                visitor.tStart(tCellIndex);
                for (int zCellIndex = 0; zCellIndex < zCellCount; ++zCellIndex) {
                    visitor.zStart(zCellIndex);
                    Array array = readDataSlice(tCellIndex, zCellIndex);
                    doTraverseXY(visitor, array);
                    visitor.zEnd(zCellIndex);
                }
                visitor.tEnd(tCellIndex);
            }
        }
        visitor.traverseEnd();

    }

    protected Array readDataSlice(int t_index, int z_index) throws java.io.IOException {
        int failures = 0;
        Array slice = null;
        while (slice == null) {
            try {
                slice = gridDataType.readDataSlice(t_index, z_index, -1, -1);
            } catch (IOException e) {
                if (failures++ < 3) {
                    LOGGER.warn("Error reading slice [t={}, z={}] from {}: failure {}, reattempting.  Exception was {}",
                            new Object[] {t_index, z_index, gridDataType.getDescription(), failures, e});
                } else {
                    LOGGER.error("Unable to read slice [t={}, z={}] from {} after {} failures. Exception was {}",
                            new Object[] {t_index, z_index, gridDataType.getDescription(), failures, e});
                    throw e;
                }
            }
        }
        return slice;
    }
    
    protected void doTraverseXY(GridCellVisitor visitor, Array array) {
        Index arrayIndex = array.getIndex();
        visitor.yxStart();
        for (int yCellIndex = 0; yCellIndex < yCellCount; ++yCellIndex) {
            for (int xCellIndex = 0; xCellIndex < xCellCount; ++xCellIndex) {
                visitor.processGridCell(
                        xCellIndex,
                        yCellIndex,
                        array.getDouble(arrayIndex.set(yCellIndex, xCellIndex)));
            }
        }
        visitor.yxEnd();
    }

}
