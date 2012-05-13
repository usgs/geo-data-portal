package gov.usgs.derivative.aparapi;

import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tkunicki
 */
public abstract class GridInputTZYXKernel extends AbstractGridKernel implements GridKernel {
    
    private final static Logger LOGGER = LoggerFactory.getLogger(GridInputTZYXKernel.class);
    
    protected float[] gtzyxInputValues;
    
    private int tzExecuteCount;
        
    public GridInputTZYXKernel(int gInputCount, int tInputCount, int zInputCount, int yxCount) {
        super(gInputCount, tInputCount, zInputCount, yxCount);
        
        this.gtzyxInputValues = new float[gInputCount * tInputCount * zInputCount * yxCountPadded];
        
        tzExecuteCount = 0;
                
        LOGGER.debug("Initialized: input storage: {} MiB", new Object[] {
            (gtzyxInputValues.length * (Float.SIZE/Byte.SIZE)) / (1 << 20),
        });
    }

    @Override
    public void preExecute() {
        if (tzExecuteCount > 0) {
            super.preExecute();
            int tExecuteCount = tzExecuteCount / zCount;
            // if tExecuteCount < tInputCount we didn't get a enough data to fill buffer. Set missing timesteps to NaN (missing value)
            if (tExecuteCount < tInputCount) {
                for (int gIndex = 0; gIndex < gInputCount; ++gIndex) {
                    for (int tIndex = tExecuteCount; tIndex < tInputCount; ++tIndex) {
                        for (int zIndex = 0; zIndex < zCount; ++zIndex) {
                            int gtzOffset =
                                gIndex * tInputCount * zCount * yxCountPadded +
                                tIndex * zCount * yxCountPadded + 
                                zIndex * yxCountPadded;
                            Arrays.fill(gtzyxInputValues, gtzOffset, gtzOffset + yxCountPadded, Float.NaN);
                        }
                    }
                } 
            }
            put(gtzyxInputValues);
        }
    }

    @Override
    public void execute() {
        if (tzExecuteCount > 0) {
            super.execute();
        } else {
            Arrays.fill(zyxOutputValues, Float.NaN);
        }
    }
    
    @Override
    public void postExecute() {
        if (tzExecuteCount > 0) {
            super.postExecute();
        }
        tzExecuteCount = 0;
    }
    
    @Override
    public void addYXInputValues(List<float[]> yxValues) {
        addYXInputValues(yxValues, gtzyxInputValues, true);
    }
   
    // HACK, horrible HACK REFACTOR
    protected void addYXInputValues(List<float[]> yxValues, float[] data, boolean increment) {
        if (yxValues.size() != gInputCount) {
            throw new IllegalStateException("yxValues.size() >= gInputCount");
        }
        int tIndex = tzExecuteCount / zCount;
        int zIndex = tzExecuteCount % zCount;
        if (tIndex >= tInputCount) {
            throw new IllegalStateException("tIndex >= tInputCount");
        }
        if (zIndex >= zCount) {
            throw new IllegalStateException("zIndex >= zCount");
        }
        for (int gIndex = 0; gIndex < gInputCount; ++gIndex) {
            int gtzOffset =
                    gIndex * tInputCount * zCount * yxCountPadded +
                    tIndex * zCount * yxCountPadded + 
                    zIndex * yxCountPadded;
            System.arraycopy(yxValues.get(gIndex), 0, data, gtzOffset, yxCount);
        }
        if (increment) tzExecuteCount++;
    }
    
    // HACK, horrible HACK REFACTOR
    protected void resetTZExecuteCount() {
        tzExecuteCount = 0;
    }
    
    @Override
    protected int getTCountForExecution() {
        return tzExecuteCount / zCount;
    }

    protected  int k_getTZYXInputIndex(int gridIndex) {
        return gridIndex * tInputCountA[0] * zCountA[0] * getGlobalSize() + // performance on global memory read ?
               k_getTPassIndex() * zCountA[0] * getGlobalSize() +
               k_getZPassIndex() * getGlobalSize() +
               getGlobalId();
    }
    
    protected  float k_getTZYXInputValue(int gridIndex) {
        return gtzyxInputValues[k_getTZYXInputIndex(gridIndex)];
    }
        
}
