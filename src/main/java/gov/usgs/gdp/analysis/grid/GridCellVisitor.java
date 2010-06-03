package gov.usgs.gdp.analysis.grid;

/**
 *
 * @author tkunicki
 */
public abstract class GridCellVisitor {

    public void traverseStart() {}
    public void traverseEnd() {}

    public void tStart(int tIndex) {}
    public void tEnd(int tIndex) {}

    public void zStart(int zIndex) {}
    public void zEnd(int zIndex) {}

    public void yxStart() {}
    public void yxEnd() {}

    public abstract void processGridCell(int xCellIndex, int yCellIndex, double value);

}
