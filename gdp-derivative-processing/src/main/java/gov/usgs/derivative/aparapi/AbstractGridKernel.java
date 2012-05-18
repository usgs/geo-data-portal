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
    
    // input grid count
    protected final int gInputCount;   
    // to pass to kernel in explicit mode
    protected final int[] k_gInputCountA;
    
    // maximum t (time) dimension count (per kernel execution), used for buffer allocation
    protected final int tInputCountMaximum;
    // to pass to kernel in explicit mode
    protected final int[] k_tInputCountMaximumA;
    
    // current t (time) dimension count (for kernel execution), used for execution accounting
//    protected int tInputCountExecute;
    // to pass to kernel in explicit mode
    protected final int[] k_tInputCountExecuteA;
    
    // z (threshold) dimension count
    protected final int zCount;
    // to pass to kernel in explicit mode
    protected int[] k_zCountA;            
    
    // product of x and y dimension size
    protected final int yxCount;
    // product of x and y dimension size padded to integer multiple of 512
    protected final int yxCountPadded;
    protected final int yxPadding;
    
    
    protected final int zyxOutputCount;
    // output values from single kernel execution
    protected float[] k_zyxOutputValues;
    
    public AbstractGridKernel(int gInputCount, int tInputCountMaximum, int zCount, int yxCount) {
        
        this.gInputCount = gInputCount;
        this.k_gInputCountA = new int[] { gInputCount };

        this.tInputCountMaximum = tInputCountMaximum;
        this.k_tInputCountMaximumA = new int[] { tInputCountMaximum };
        
        this.k_tInputCountExecuteA = new int[1];
        
        this.zCount = zCount;
        this.k_zCountA = new int[] { zCount };

        this.yxCount = yxCount;
        this.yxCountPadded = OpenCLUtil.pad(yxCount);
        this.yxPadding = yxCountPadded - yxCount;
        
        zyxOutputCount = zCount * yxCountPadded;
        k_zyxOutputValues = new float[zyxOutputCount];
        
        setExecutionMode(EXECUTION_MODE.CPU);
        setExplicit(true);
        put(k_gInputCountA);
        put(k_tInputCountMaximumA);
        put(k_zCountA);
                
        LOGGER.debug("Initialized: g: {}, t: {}, z: {}, yx: {}, output storage: {} MiB", new Object[] {
            gInputCount,
            tInputCountMaximum,
            zCount,
            yxCount,
            (zyxOutputCount * (Float.SIZE/Byte.SIZE)) / (1 << 20)
        });
    }

    @Override
    public void execute() {
        initializeZYXOutput();
        put(k_zyxOutputValues);
        int tInputCountExecute = getExecutionTimeStepCount();
        k_tInputCountExecuteA[0] = tInputCountExecute;
        put(k_tInputCountExecuteA);
        int passes = zCount * tInputCountExecute;
        LOGGER.debug("Executing kernel: global size {}, passes {} [z = {}, t = {} current ({} max)]", new Object[] {
            yxCountPadded,
            passes,
            zCount,
            tInputCountExecute,
            tInputCountMaximum,
        });
        preExecute();
        execute(yxCountPadded, passes);
        postExecute();
        get(k_zyxOutputValues);
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
        return k_zCountA[0];
    }
    
    protected  int k_getTPassIndex() {
        return getPassId() / k_zCountA[0]; // performace on global memory read?
    }
    
    protected  int k_getZPassIndex() {
        return getPassId() % k_zCountA[0]; // performace on global memory read?
    }

    @Override
    public  float[] getZYXOutputValues() {
        return k_zyxOutputValues;
    }

    protected  int k_getZYXOutputIndex() {
        return k_getZPassIndex() * getGlobalSize() + getGlobalId();
    }
    
    protected float kgetZYXOutputValue() {
        return k_zyxOutputValues[k_getZYXOutputIndex()];
    }
    
    protected void k_setZYXOutputValue(float value) {
        this.k_zyxOutputValues[k_getZYXOutputIndex()] = value;
    }
    
    @Override
    public void postExecute() {
    }

    @Override
    public void preExecute() {
    }
    
    protected void initializeZYXOutput() {
        Arrays.fill(k_zyxOutputValues, 0);
    }
    
    protected abstract int getExecutionTimeStepCount();
}
