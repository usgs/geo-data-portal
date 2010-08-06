package gov.usgs.gdp.analysis.grid;

import ucar.nc2.dt.GridCoordSystem;

/**
 *
 * @author tkunicki
 */
public abstract class GridCellVisitor {

    public void traverseStart(GridCoordSystem gridCoordSystem) {}
    public void traverseEnd() {}

    public void tStart(int tIndex) {}
    public void tEnd(int tIndex) {}

    public void zStart(int zIndex) {}
    public void zEnd(int zIndex) {}

    public void yxStart() {}
    public void yxEnd() {}

    public abstract void processGridCell(int xCellIndex, int yCellIndex, double value);

}
