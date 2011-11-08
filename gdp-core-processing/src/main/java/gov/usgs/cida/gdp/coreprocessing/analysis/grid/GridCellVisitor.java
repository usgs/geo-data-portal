package gov.usgs.cida.gdp.coreprocessing.analysis.grid;

import ucar.nc2.dt.GridDatatype;

/**
 *
 * @author tkunicki
 */
public abstract class GridCellVisitor {

    public void traverseStart(GridDatatype gridDatatype) {}
    public void traverseEnd() {}

    public boolean tStart(int tIndex) { return true; }
    public void tEnd(int tIndex) {}

    public boolean zStart(int zIndex) { return true; }
    public void zEnd(int zIndex) {}

    public void yxStart() {}
    public void yxEnd() {}

    public abstract void processGridCell(int xCellIndex, int yCellIndex, double value);

}
