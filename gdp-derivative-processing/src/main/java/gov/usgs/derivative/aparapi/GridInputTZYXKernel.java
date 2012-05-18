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
    
    protected float[] k_gtzyxInputValues;
    
    private int tzExecuteCount;
        
    public GridInputTZYXKernel(int gInputCount, int tInputCountMax, int zInputCount, int yxCount) {
        super(gInputCount, tInputCountMax, zInputCount, yxCount);
        
        this.k_gtzyxInputValues = new float[gInputCount * tInputCountMax * zInputCount * yxCountPadded];
        
        tzExecuteCount = 0;
                
        LOGGER.debug("Initialized: input storage: {} MiB", new Object[] {
            (k_gtzyxInputValues.length * (Float.SIZE/Byte.SIZE)) / (1 << 20),
        });
    }

    @Override
    public void preExecute() {
        if (tzExecuteCount > 0) {
            super.preExecute();
            int tExecuteCount = tzExecuteCount / zCount;
            // if tExecuteCount < tInputCount we didn't get a enough data to fill buffer. Set missing timesteps to NaN (missing value)
            if (tExecuteCount < tInputCountMaximum) {
                for (int gIndex = 0; gIndex < gInputCount; ++gIndex) {
                    for (int tIndex = tExecuteCount; tIndex < tInputCountMaximum; ++tIndex) {
                        for (int zIndex = 0; zIndex < zCount; ++zIndex) {
                            int gtzOffset =
                                gIndex * tInputCountMaximum * zCount * yxCountPadded +
                                tIndex * zCount * yxCountPadded + 
                                zIndex * yxCountPadded;
                            Arrays.fill(k_gtzyxInputValues, gtzOffset, gtzOffset + yxCountPadded, Float.NaN);
                        }
                    }
                } 
            }
            put(k_gtzyxInputValues);
        }
    }

    @Override
    public void execute() {
        if (tzExecuteCount > 0) {
            super.execute();
        } else {
            Arrays.fill(k_zyxOutputValues, Float.NaN);
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
        addYXInputValues(yxValues, k_gtzyxInputValues, true);
    }
   
    // HACK, horrible HACK REFACTOR
    protected void addYXInputValues(List<float[]> yxValues, float[] data, boolean increment) {
        if (yxValues.size() != gInputCount) {
            throw new IllegalStateException("yxValues.size() >= gInputCount");
        }
        int tIndex = tzExecuteCount / zCount;
        int zIndex = tzExecuteCount % zCount;
        if (tIndex >= tInputCountMaximum) {
            throw new IllegalStateException("tIndex >= tInputCountMaximum");
        }
        if (zIndex >= zCount) {
            throw new IllegalStateException("zIndex >= zCount");
        }
        for (int gIndex = 0; gIndex < gInputCount; ++gIndex) {
            int gtzOffset =
                    gIndex * tInputCountMaximum * zCount * yxCountPadded +
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
    protected int getExecutionTimeStepCount() {
        return tzExecuteCount / zCount;
    }

    protected int k_getTZYXInputIndex(int gridIndex) {
        return gridIndex * k_tInputCountMaximumA[0] * k_zCountA[0] * getGlobalSize() + // performance on global memory read ?
               k_getTPassIndex() * k_zCountA[0] * getGlobalSize() +
               k_getZPassIndex() * getGlobalSize() +
               getGlobalId();
    }
    
    protected float k_getTZYXInputValue(int gridIndex) {
        return k_gtzyxInputValues[k_getTZYXInputIndex(gridIndex)];
    }
        
}
