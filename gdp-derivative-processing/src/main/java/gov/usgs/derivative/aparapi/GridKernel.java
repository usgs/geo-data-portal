package gov.usgs.derivative.aparapi;

import java.util.List;

/**
 *
 * @author tkunicki
 */
public interface GridKernel {

    void execute();

    int getYXCount();

    int getYXCountPadded();

    int getYXPadding();

    int getZCount();

    float[] getZYXOutputValues();

    void postExecute();

    void preExecute();
    
    void addYXInputValues(List<float[]> yxValues);
    
}
