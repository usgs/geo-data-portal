package gov.usgs.derivative;

import com.google.common.base.Joiner;
import com.google.common.primitives.Doubles;
import gov.usgs.derivative.aparapi.AbstractGridKernel;
import gov.usgs.derivative.aparapi.GridInputTZYXKernel;
import gov.usgs.derivative.time.NetCDFDateUtil;
import gov.usgs.derivative.time.RepeatingPeriodTimeStepDescriptor;
import gov.usgs.derivative.time.TimeStepDescriptor;
import gov.usgs.cida.gdp.coreprocessing.analysis.grid.GridType;
import gov.usgs.cida.gdp.coreprocessing.analysis.grid.GridUtility;
import java.util.List;
import org.joda.time.Interval;
import org.joda.time.Years;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;
import ucar.nc2.constants.CF;
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
public class AnnualScenarioEnsembleAveragingVisitor extends DerivativeGridVisitor {

    private final String scenario;
    
    public AnnualScenarioEnsembleAveragingVisitor(String scenario) {
        this.scenario = scenario;
    }
    
    protected String generateDerivativeOutputVariableName(List<GridDatatype> gridDatatypeList) {
        // NOTE:  assumes input grid variable naming convention:
        String gridName = gridDatatypeList.get(0).getName();
        String variableName = gridName.substring(gridName.lastIndexOf(scenario));
        return Joiner.on("_").join(
                "ensemble",
                variableName);
    }
    
    @Override
    protected String generateOutputFileBaseName(List<GridDatatype> gridDatatypeList) {
        return Joiner.on(".").join(
            "derivative",
            getValueDescriptor().getOutputName());
    }
    
    @Override
    protected int getInputGridCount() {
        return 4;
    }

    @Override
    protected DerivativeValueDescriptor generateDerivativeValueDescriptor(List<GridDatatype> gridDatatypeList) {
        GridDatatype gridDatatype = gridDatatypeList.get(0);
        
        CoordinateAxis1D thresholdAxis = gridDatatype.getCoordinateSystem().getVerticalAxis();
        
        Variable thresholdVariable = thresholdAxis.getOriginalVariable();
        Attribute thresholdStandardName = thresholdVariable.findAttribute(CF.STANDARD_NAME);
        Attribute thresholdUnits = thresholdVariable.findAttribute(CF.UNITS);
        
        Variable gridVariable = gridDatatype.getVariable();
        Attribute gridStandardName = gridVariable.findAttribute(CF.STANDARD_NAME);
        Attribute gridUnits = gridVariable.findAttribute(CF.UNITS);
        
        return new DerivativeValueDescriptor(
                thresholdVariable.getShortName(), // name
                thresholdStandardName == null ? null : thresholdStandardName.getStringValue(), // standard_name
                thresholdUnits == null ? null : thresholdUnits.getStringValue(),
                thresholdVariable.getDataType(),
                Doubles.asList(thresholdAxis.getCoordValues()),
                generateDerivativeOutputVariableName(gridDatatypeList), // name
                gridStandardName == null ? null : gridStandardName.getStringValue(), // standard name
                gridUnits == null ? null : gridUnits.getStringValue(), // units
                gridVariable.findAttribute("missing_value").getNumericValue(),
                DataType.FLOAT);
    }

    @Override
    protected String getOutputFilePath() {
        return DerivativeUtil.DEFAULT_P1Y_PATH;
    }

    @Override
    protected TimeStepDescriptor generateDerivativeTimeStepDescriptor(List<GridDatatype> gridDatatypeList) {
        GridCoordSystem gridCoordSystem = gridDatatypeList.get(0).getCoordinateSystem();
        CoordinateAxis1DTime timeAxis = gridCoordSystem.getTimeAxis1D();
        DateRange timeRange = timeAxis.getDateRange();
        
        Interval i = NetCDFDateUtil.toIntervalUTC(timeRange);
        i = i.withEnd(i.getEnd().plus(Years.ONE));
        return new RepeatingPeriodTimeStepDescriptor(
                i,
                Years.ONE);    
    }

    @Override
    protected AbstractGridKernel generateGridKernel(List<GridDatatype> gridDatatypeList) throws ConversionException, Exception {
                GridDatatype gridDatatype = gridDatatypeList.get(0);
        GridCoordSystem gridCoordSystem = gridDatatype.getCoordinateSystem();
        
        int zCount = gridCoordSystem.getVerticalAxis().getShape(0);
        int yxCount =  GridUtility.getXAxisLength(gridCoordSystem) * GridUtility.getYAxisLength(gridCoordSystem);
        
        return new AnnualScenarioEnsembleAveragingKernel(zCount, yxCount);
    }

    @Override
    protected boolean isValidInputGridType(GridType gridType) {
        return gridType == GridType.TZYX;
    }
    
    protected class AnnualScenarioEnsembleAveragingKernel extends GridInputTZYXKernel {
        
        public AnnualScenarioEnsembleAveragingKernel(int zCount, int yxCount) {
            super(getInputGridCount(), 1, zCount, yxCount);
        }
        
        @Override
        public void run() {
            int gInputCountI = k_gInputCountA[0];
            float gInputCountF = (float)k_gInputCountA[0];
            float average = 0;
            for (int gInputIndexI = 0; gInputIndexI < gInputCountI; ++gInputIndexI) {
                float value = k_getTZYXInputValue(gInputIndexI) / gInputCountF;
                average = average + value;
            }
            k_zyxOutputValues[k_getZYXOutputIndex()] = average;
        }
        
    }
    
}
