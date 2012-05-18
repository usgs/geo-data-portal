package gov.usgs.derivative.aparapi;

import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tkunicki
 */
public abstract class GridInputTYXKernel extends AbstractGridKernel implements GridKernel {
    
    private final static Logger LOGGER = LoggerFactory.getLogger(GridInputTYXKernel.class);
    
    protected final float[] k_zValues;
    
    protected final float[] k_gtyxInputValues;
    
    private int tExecuteCount;
        
    public GridInputTYXKernel(int gInputCount, int tInputCountMax, int yxCount, float[] zValues) {
        super(gInputCount, tInputCountMax, zValues.length, yxCount);

        this.k_zValues = new float[zValues.length];
        System.arraycopy(zValues, 0, this.k_zValues, 0, zValues.length); // defensive copy

        this.k_gtyxInputValues = new float[gInputCount * tInputCountMax * yxCountPadded];

        put(this.k_zValues);
        
        tExecuteCount = 0;
                
        LOGGER.debug("Initialized: input storage: {} MiB", new Object[] {
            (k_gtyxInputValues.length * (Float.SIZE/Byte.SIZE)) / (1 << 20),
        });
    }

    @Override
    public void preExecute() {
        if (tExecuteCount > 0) {
            super.preExecute();
            // if tExecuteCount < tInputCount we didn't get a enough data to fill buffer. Set missing timesteps to NaN (missing value)
            if (tExecuteCount < tInputCountMaximum) {
                for (int gIndex = 0; gIndex < gInputCount; ++gIndex) {
                    for (int tIndex = tExecuteCount; tIndex < tInputCountMaximum; ++tIndex) {
                        int gtOffset =
                            gIndex * tInputCountMaximum * yxCountPadded +
                            tIndex * yxCountPadded;
                        Arrays.fill(k_gtyxInputValues, gtOffset, gtOffset + yxCountPadded, Float.NaN);
                    }
                } 
            }
            put(k_gtyxInputValues);
        }
    }
    
    @Override
    public void execute() {
        if (tExecuteCount > 0) {
            super.execute();
        } else {
            Arrays.fill(k_zyxOutputValues, Float.NaN);
        }
    }
    
    @Override
    public void postExecute() {
        if (tExecuteCount > 0) {
            super.postExecute();
        }
        tExecuteCount = 0;
    }

    @Override
    public void addYXInputValues(List<float[]> yxValues) {
        if (yxValues.size() > gInputCount) {
            throw new IllegalStateException("yxValues.size() >= gInputCount");
        }
        int tIndex = tExecuteCount;
        if (tIndex >= tInputCountMaximum) {
            throw new IllegalStateException("tIndex >= tInputCountMaximum");
        }
        for (int gIndex = 0; gIndex < gInputCount; ++gIndex) {
            int gtOffset =
                    gIndex * tInputCountMaximum * yxCountPadded +
                    tIndex * yxCountPadded;
            System.arraycopy(yxValues.get(gIndex), 0, k_gtyxInputValues, gtOffset, yxCount);
        }
        tExecuteCount++;
    }

    @Override
    protected int getExecutionTimeStepCount() {
        return tExecuteCount;
    }
    
    protected float k_getZValue() {
        return k_zValues[k_getZPassIndex()];
    }
    
    protected int k_getTYXInputIndex(int gridIndex) {
        return gridIndex * k_tInputCountMaximumA[0] * getGlobalSize() +  // performance on global memory read ?
               k_getTPassIndex() * getGlobalSize() +
               getGlobalId();
    }
    
    protected float k_getTYXInputValue(int gridIndex) {
        return k_gtyxInputValues[k_getTYXInputIndex(gridIndex)];
    }
    
}
