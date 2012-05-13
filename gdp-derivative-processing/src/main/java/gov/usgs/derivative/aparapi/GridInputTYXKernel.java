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
    
    protected float[] zValues;
    
    protected float[] gtyxInputValues;
    
    private int tExecuteCount;
        
    public GridInputTYXKernel(int gInputCount, int tInputCount, int yxCount, float[] zValues) {
        super(gInputCount, tInputCount, zValues.length, yxCount);

        this.zValues = new float[zValues.length];
        System.arraycopy(zValues, 0, this.zValues, 0, zValues.length); // defensive copy

        this.gtyxInputValues = new float[gInputCount * tInputCount * yxCountPadded];

        put(this.zValues);
        
        tExecuteCount = 0;
                
        LOGGER.debug("Initialized: input storage: {} MiB", new Object[] {
            (gtyxInputValues.length * (Float.SIZE/Byte.SIZE)) / (1 << 20),
        });
    }

    @Override
    public void preExecute() {
        super.preExecute();
        // if tExecuteCount < tInputCount we didn't get a enough data to fill buffer. Set missing timesteps to NaN (missing value)
        if (tExecuteCount < tInputCount) {
            for (int gIndex = 0; gIndex < gInputCount; ++gIndex) {
                for (int tIndex = tExecuteCount; tIndex < tInputCount; ++tIndex) {
                    int gtOffset =
                        gIndex * tInputCount * yxCountPadded +
                        tIndex * yxCountPadded;
                    Arrays.fill(gtyxInputValues, gtOffset, gtOffset + yxCountPadded, Float.NaN);
               }
           } 
        }
        put(gtyxInputValues);
    }
    
    @Override
    public void postExecute() {
        super.postExecute();
        tExecuteCount = 0;
    }

    @Override
    public void addYXInputValues(List<float[]> yxValues) {
        if (yxValues.size() > gInputCount) {
            throw new IllegalStateException("yxValues.size() >= gInputCount");
        }
        int tIndex = tExecuteCount;
        if (tIndex >= tInputCount) {
            throw new IllegalStateException("tIndex >= tInputCount");
        }
        for (int gIndex = 0; gIndex < gInputCount; ++gIndex) {
            int gtOffset =
                    gIndex * tInputCount * yxCountPadded +
                    tIndex * yxCountPadded;
            System.arraycopy(yxValues.get(gIndex), 0, gtyxInputValues, gtOffset, yxCount);
        }
        tExecuteCount++;
    }

    @Override
    protected int getTCountForExecution() {
        return tExecuteCount;
    }
    
    protected float k_getZValue() {
        return zValues[k_getZPassIndex()];
    }
    
    protected int k_getTYXInputIndex(int gridIndex) {
        return gridIndex * tInputCountA[0] * getGlobalSize() +  // performance on global memory read ?
               k_getTPassIndex() * getGlobalSize() +
               getGlobalId();
    }
    
    protected float k_getTYXInputValue(int gridIndex) {
        return gtyxInputValues[k_getTYXInputIndex(gridIndex)];
    }
    
}
