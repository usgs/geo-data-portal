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
import ucar.nc2.Attribute;
import ucar.nc2.Variable;
import ucar.nc2.constants.CF;
import ucar.nc2.constants.CDM;
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
        
        Variable gridVariable = gridDatatype.getVariable();
        Attribute gridStandardName = gridVariable.findAttribute(CF.STANDARD_NAME);
        Attribute gridUnits = gridVariable.findAttribute(CDM.UNITS);
        
        CoordinateAxis1D thresholdAxis = gridDatatype.getCoordinateSystem().getVerticalAxis();
        
        if (thresholdAxis == null) {
            return new DerivativeValueDescriptor(
                    null, // name
                    null, // standard_name
                    null, // units
                    null, // DataType
                    null, // values
                    gridVariable.getShortName(), // name
                    gridStandardName == null ? null : gridStandardName.getStringValue(), // standard name TODO: ???
                    gridUnits == null ? null : gridUnits.getStringValue(), // units
                    Float.valueOf(-1f),
                    DataType.FLOAT);
        } else {
            Variable thresholdVariable = thresholdAxis.getOriginalVariable() ;
            Attribute thresholdStandardName = thresholdVariable.findAttribute(CF.STANDARD_NAME);
            Attribute thresholdUnits = thresholdVariable.findAttribute(CDM.UNITS);
            return new DerivativeValueDescriptor(
                    thresholdVariable.getShortName(), // name
                    thresholdStandardName == null ? null : thresholdStandardName.getStringValue(), // standard_name
                    thresholdUnits == null ? null : thresholdUnits.getStringValue(),
                    thresholdVariable.getDataType(),
                    Doubles.asList(thresholdAxis.getCoordValues()),
                    gridVariable.getShortName(), // name
                    gridStandardName == null ? null : gridStandardName.getStringValue(), // standard name TODO: ???
                    gridUnits == null ? null : gridUnits.getStringValue(), // units
                    Float.valueOf(-1f),
                    DataType.FLOAT);
        }
    }

    @Override
    protected TimeStepDescriptor generateDerivativeTimeStepDescriptor(List<GridDatatype> gridDatatypeList) {
        GridCoordSystem gridCoordSystem = gridDatatypeList.get(0).getCoordinateSystem();
        CoordinateAxis1DTime timeAxis = gridCoordSystem.getTimeAxis1D();
        DateRange timeRange = timeAxis.getDateRange();
        return new IntervalTimeStepDescriptor(
            NetCDFDateUtil.toIntervalUTC(timeRange),
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
            mykernel.addYXInputValues(yxValuesList, mykernel.k_gtzyxBaseValues, true);
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
    protected int getInputGridCount() {
        return 1;
    }

    @Override
    protected boolean isValidInputGridType(GridType gridType) {
        return gridType == GridType.TZYX;
    }
    
    private class DerivativeKernel extends GridInputTZYXKernel {
        
        protected float[] k_gtzyxBaseValues;
        protected boolean initialized = false;

        public DerivativeKernel(int zCount, int yxCount) {
            super(1, 1, zCount, yxCount);
            k_gtzyxBaseValues = new float[zyxOutputCount];
        }

        @Override
        public void preExecute() {
            super.preExecute();
            if (!initialized) {
                put(k_gtzyxBaseValues);
                initialized = true;
            }
        }

        @Override
        public void run() {
            int tzyxInputIndex = k_getTZYXInputIndex(0);
            int zyxOutputIndex = k_getZYXOutputIndex();
            float baseValue = k_gtzyxBaseValues[tzyxInputIndex];
            float currentValue = k_gtzyxInputValues[tzyxInputIndex];
            if (baseValue == baseValue && currentValue == currentValue) {
                k_zyxOutputValues[zyxOutputIndex] = k_gtzyxInputValues[tzyxInputIndex] - k_gtzyxBaseValues[tzyxInputIndex];
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
