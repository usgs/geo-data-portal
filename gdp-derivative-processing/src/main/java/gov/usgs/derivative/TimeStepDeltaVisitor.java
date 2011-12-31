package gov.usgs.derivative;

import com.google.common.base.Joiner;
import com.google.common.primitives.Doubles;
import gov.usgs.derivative.aparapi.AbstractGridKernel;
import gov.usgs.derivative.aparapi.GridInputTZYXKernel;
import gov.usgs.derivative.time.IntervalTimeStepDescriptor;
import gov.usgs.derivative.time.NetCDFDateUtil;
import gov.usgs.derivative.time.TimeStepDescriptor;
import gov.usgs.cida.gdp.coreprocessing.analysis.grid.GridType;
import gov.usgs.cida.gdp.coreprocessing.analysis.grid.GridUtility;
import java.util.Arrays;
import java.util.List;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.DataType;
import ucar.nc2.Variable;
import ucar.nc2.dataset.CoordinateAxis1D;
import ucar.nc2.dataset.CoordinateAxis1DTime;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.units.DateRange;
import ucar.units.ConversionException;

/**
 *
 * @author tkunicki
 */
public final class TimeStepDeltaVisitor extends DerivativeGridVisitor {
    
    private final static Logger LOGGER = LoggerFactory.getLogger(TimeStepDeltaVisitor.class);
    
    private DerivativeKernel mykernel;

    @Override
    protected String getOutputFilePath() {
        return DerivativeUtil.DEFAULT_P30Y_DELTA_PATH;
    }

    @Override
    protected String generateOutputFileBaseName(List<GridDatatype> gridDatatypeList) {
        return Joiner.on(".").join(
                "derivative",
                gridDatatypeList.get(0).getVariable().getName()+ "-delta");
    } 
    
    @Override
    protected DerivativeValueDescriptor generateDerivativeValueDescriptor(List<GridDatatype> gridDatatypeList) {
        GridDatatype gridDatatype = gridDatatypeList.get(0);
        
        CoordinateAxis1D thresholdAxis = gridDatatype.getCoordinateSystem().getVerticalAxis();
        Variable thresholdVariable = thresholdAxis.getOriginalVariable();
        Variable gridVariable = gridDatatype.getVariable();
        return new DerivativeValueDescriptor(
                thresholdVariable.getShortName(), // name
                thresholdVariable.findAttribute("standard_name").getStringValue(), // standard_name
                thresholdVariable.getUnitsString(),
                thresholdVariable.getDataType(),
                Doubles.asList(thresholdAxis.getCoordValues()),
                gridVariable.getShortName(), // name
                gridVariable.findAttribute("standard_name").getStringValue(), // standard name TODO: ???
                "days", // units
                Float.valueOf(-1f),
                DataType.FLOAT);
    }

    @Override
    protected TimeStepDescriptor generateDerivativeTimeStepDescriptor(List<GridDatatype> gridDatatypeList) {
        GridCoordSystem gridCoordSystem = gridDatatypeList.get(0).getCoordinateSystem();
        CoordinateAxis1DTime timeAxis = gridCoordSystem.getTimeAxis1D();
        DateRange timeRange = timeAxis.getDateRange();
        return new IntervalTimeStepDescriptor(
            NetCDFDateUtil.convertDateRangeToInterval(timeRange),
            // TODO: parameterize;
            Arrays.asList(new Interval[] {
//                new Interval("1961-01-01TZ/1991-01-01TZ"),
                new Interval("2011-01-01TZ/2041-01-01TZ"),
                new Interval("2041-01-01TZ/2071-01-01TZ"),
                new Interval("2071-01-01TZ/2098-01-01TZ"),
            }));             
    }

    
    
    @Override
    protected void outputTimeStepStart(int outputTimeStepIndex) {
        super.outputTimeStepStart(outputTimeStepIndex);
        // HACK, horrible HACK
        if (outputTimeStepIndex == 0) {
            mykernel.resetTZExecuteCount();
        }
    }
    
    @Override
    public void yxStart(List<float[]> yxValuesList) {
        if (missingValuesMask == null) {
            missingValuesMask = DerivativeUtil.generateMissingValuesMask(yxValuesList, missingValuesList);
        }
        if (getOutputCurrentTimeStep() < 0) {
            // HACK, horrible HACK
            mykernel.addYXInputValues(yxValuesList, mykernel.gtzyxBaseValues, true);
        } else {
            mykernel.addYXInputValues(yxValuesList);
        }
    }

    @Override
    protected boolean visitAllTimeSteps() {
        return true;
    }

    @Override
    protected AbstractGridKernel generateGridKernel(List<GridDatatype> gridDatatypeList) throws ConversionException, Exception {
        GridDatatype gridDatatype = gridDatatypeList.get(0);
        GridCoordSystem gridCoordSystem = gridDatatype.getCoordinateSystem();
        
        int zCount = gridCoordSystem.getVerticalAxis().getShape(0);
        int yxCount =  GridUtility.getXAxisLength(gridCoordSystem) * GridUtility.getYAxisLength(gridCoordSystem);
       
         mykernel = new DerivativeKernel(zCount, yxCount);
        
        return mykernel;
    }

    @Override
    protected int requiredInputGridCount() {
        return 1;
    }

    @Override
    protected GridType requiredInputGridType() {
        return GridType.TZYX;
    }
    
    private class DerivativeKernel extends GridInputTZYXKernel {
        
        protected float[] gtzyxBaseValues;
        protected boolean initialized = false;

        public DerivativeKernel(int zCount, int yxCount) {
            super(1, 1, zCount, yxCount);
            gtzyxBaseValues = new float[zyxOutputCount];
        }

        @Override
        public void preExecute() {
            super.preExecute();
            if (!initialized) {
                put(gtzyxBaseValues);
                initialized = true;
            }
        }

        @Override
        public void run() {
            int tzyxInputIndex = k_getTZYXInputIndex(0);
            int zyxOutputIndex = k_getZYXOutputIndex();
            float baseValue = gtzyxBaseValues[tzyxInputIndex];
            float currentValue = gtzyxInputValues[tzyxInputIndex];
            if (baseValue == baseValue && currentValue == currentValue) {
                zyxOutputValues[zyxOutputIndex] = gtzyxInputValues[tzyxInputIndex] - gtzyxBaseValues[tzyxInputIndex];
            }
        }

        @Override
        // HACK, horrible HACK
        protected void addYXInputValues(List<float[]> yxValues, float[] data, boolean increment) {
            super.addYXInputValues(yxValues, data, increment);
        }

        @Override
        // HACK, horrible HACK
        protected void resetTZExecuteCount() {
            super.resetTZExecuteCount();
        }
    }
}
