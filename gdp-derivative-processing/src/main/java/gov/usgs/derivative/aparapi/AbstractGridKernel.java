package gov.usgs.derivative.aparapi;

import com.amd.aparapi.Kernel;
import gov.usgs.derivative.OpenCLUtil;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author tkunicki
 */
public abstract class AbstractGridKernel extends Kernel implements GridKernel {
    
    private final static Logger LOGGER = LoggerFactory.getLogger(AbstractGridKernel.class);
    
    protected  int gInputCount;    // input grid count
    protected  int[] gInputCountA; // to pass to kernel in explicit mode
    protected  int tInputCount;    // t (time) dimension count (per kernel execution)
    protected  int[] tInputCountA; // to pass to kernel in explicit mode
    protected  int zCount;         // z (threshold) dimension count
    protected  int[] zCountA;      // to pass to kernel in explicit mode
    
    protected  int yxCount;        // product of x and y dimension size
    protected  int yxCountPadded;
    protected  int yxPadding;
    
    protected  int zyxOutputCount;
    protected  float[] zyxOutputValues;
    
    public AbstractGridKernel(int gInputCount, int tInputCount, int zCount, int yxCount) {
        
        this.gInputCount = gInputCount;
        this.gInputCountA = new int[] { gInputCount };

        this.tInputCount = tInputCount;
        this.tInputCountA = new int[] { tInputCount };
        
        this.zCount = zCount;
        this.zCountA = new int[] { zCount };

        this.yxCount = yxCount;
        this.yxCountPadded = OpenCLUtil.pad(yxCount);
        this.yxPadding = yxCountPadded - yxCount;
        
        zyxOutputCount = zCount * yxCountPadded;
        zyxOutputValues = new float[zyxOutputCount];
        
        setExecutionMode(EXECUTION_MODE.CPU);
        setExplicit(true);
        put(gInputCountA);
        put(tInputCountA);
        put(zCountA);
                
        LOGGER.debug("Initialized: g: {}, t: {}, z: {}, yx: {}, output storage: {} MiB", new Object[] {
            gInputCount,
            tInputCount,
            zCount,
            yxCount,
            (zyxOutputCount * (Float.SIZE/Byte.SIZE)) / (1 << 20)
        });
    }

    @Override
    public void execute() {
        initializeZYXOutput();
        put(zyxOutputValues);
        int tCountForExecution = getTCountForExecution();
        int passes = zCount * tCountForExecution;
        LOGGER.debug("Executing kernel: global size {}, passes {} [z = {}, t = {} current ({} max)]", new Object[] {
            yxCountPadded,
            passes,
            zCount,
            tCountForExecution,
            tInputCount,
        });
        preExecute();
        execute(yxCountPadded, passes);
        postExecute();
        get(zyxOutputValues);
    }

    @Override
    public  int getYXCount() {
        return yxCount;
    }

    @Override
    public  int getYXCountPadded() {
        return yxCountPadded;
    }

    @Override
    public  int getYXPadding() {
        return yxPadding;
    }

    @Override
    public  int getZCount() {
        return zCountA[0];
    }
    
    protected  int k_getTPassIndex() {
        return getPassId() / zCountA[0]; // performace on global memory read?
    }
    
    protected  int k_getZPassIndex() {
        return getPassId() % zCountA[0]; // performace on global memory read?
    }

    @Override
    public  float[] getZYXOutputValues() {
        return zyxOutputValues;
    }

    protected  int k_getZYXOutputIndex() {
        return k_getZPassIndex() * getGlobalSize() + getGlobalId();
    }
    
    protected float kgetZYXOutputValue() {
        return zyxOutputValues[k_getZYXOutputIndex()];
    }
    
    protected void k_setZYXOutputValue(float value) {
        this.zyxOutputValues[k_getZYXOutputIndex()] = value;
    }
    
    @Override
    public void postExecute() {
    }

    @Override
    public void preExecute() {
    }
    
    protected void initializeZYXOutput() {
        Arrays.fill(zyxOutputValues, 0);
    }
    
    protected abstract int getTCountForExecution();
}
