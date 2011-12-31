package gov.usgs.derivative.grid;

import java.util.List;
import ucar.nc2.dt.GridDatatype;

/**
 *
 * @author tkunicki
 */
public abstract class GridVisitor {
    
    public void traverseStart(List<GridDatatype> gridDatatypeList) {}
    public boolean traverseContinue() { return true; }
    public void traverseEnd() {}

    public boolean tStart(int tIndex) { return true; }
    public void tEnd(int tIndex) {}

    public boolean zStart(int zIndex) { return true; }
    public void zEnd(int zIndex) {}

    public abstract void yxStart(List<float[]> xyValuesList);
    public void yxEnd() {}

}
