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
public final class TimeStepAveragingVisitor extends DerivativeGridVisitor {
    
    private final static Logger LOGGER = LoggerFactory.getLogger(TimeStepAveragingVisitor.class);

    @Override
    protected String getOutputFilePath() {
        return DerivativeUtil.DEFAULT_P30Y_PATH;
    }

    @Override
    protected String generateOutputFileBaseName(List<GridDatatype> gridDatatypeList) {
        return Joiner.on(".").join(
                "derivative",
                gridDatatypeList.get(0).getVariable().getName());
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
                new Interval("1961-01-01TZ/1991-01-01TZ"),
                new Interval("2011-01-01TZ/2041-01-01TZ"),
                new Interval("2041-01-01TZ/2071-01-01TZ"),
                new Interval("2071-01-01TZ/2100-01-01TZ"),
            }));             
    }
    
    @Override
    protected int requiredInputGridCount() {
        return 1;
    }

    @Override
    protected final GridType requiredInputGridType() {
        return GridType.TZYX;
    }

    @Override
    protected AbstractGridKernel generateGridKernel(List<GridDatatype> gridDatatypeList) throws ConversionException, Exception {
        GridDatatype gridDatatype = gridDatatypeList.get(0);
        
        GridCoordSystem gridCoordSystem = gridDatatype.getCoordinateSystem();
                
        int xCount = GridUtility.getXAxisLength(gridCoordSystem);
        int yCount = GridUtility.getYAxisLength(gridCoordSystem);
        int yxCount = yCount * xCount;
        
        int maxYears = 0;
        int outputTimeStepCount = getTimeStepDescriptor().getOutputTimeStepCount();
        for (int outputTimeStepIndex = 0; outputTimeStepIndex < outputTimeStepCount; ++outputTimeStepIndex) {
            int currentYears = getTimeStepDescriptor().getOutputTimeStepInterval(outputTimeStepIndex).toPeriod().getYears();
            if (currentYears > maxYears) {
                maxYears = currentYears;
            }
        }
        
        return new DerivativeKernel(
                maxYears,
                gridCoordSystem.getVerticalAxis().getShape(0),
                yxCount);
    }

    
    private class DerivativeKernel extends GridInputTZYXKernel {
        
        public DerivativeKernel(int tInputCount, int zInputCount, int yxCount) {
            super(requiredInputGridCount(), tInputCount, zInputCount, yxCount);
        }
        
        @Override
         public void run() {
            int zyxOutputIndex = k_getZYXOutputIndex();
            float value = k_getTZYXInputValue(0);
            if (value == value) {
                zyxOutputValues[zyxOutputIndex] = zyxOutputValues[zyxOutputIndex] + (value / (float)tInputCountA[0]);
            }
        }
        
    }
}
