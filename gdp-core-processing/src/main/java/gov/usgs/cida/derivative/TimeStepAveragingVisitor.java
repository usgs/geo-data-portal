package gov.usgs.cida.derivative;

import com.google.common.primitives.Doubles;
import gov.usgs.cida.derivative.time.IntervalTimeStepDescriptor;
import gov.usgs.cida.derivative.time.NetCDFDateUtil;
import gov.usgs.cida.derivative.time.TimeStepDescriptor;
import gov.usgs.cida.gdp.coreprocessing.analysis.grid.GridUtility;
import java.util.Arrays;
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
import ucar.nc2.units.SimpleUnit;

/**
 *
 * @author tkunicki
 */
public class TimeStepAveragingVisitor extends DerivativeVisitor {
    
    private final static Logger LOGGER = LoggerFactory.getLogger(TimeStepAveragingVisitor.class);
    
    private int xCount;
    private int yCount;
    private int yxCount;
    
    private DerivativeValueDescriptor valueDescriptor;
    private TimeStepDescriptor timeStepDescriptor;
    
    
    private CoordinateAxis1DTime timeAxis;
    
    private int thresholdIndex = 0;
    
    private double outputTimeStepLengthYears;
    
    public TimeStepAveragingVisitor() {
        
    }
    
    
    @Override
    public void traverseStart(GridDatatype gridDatatype) {
        
        CoordinateAxis1D thresholdAxis = gridDatatype.getCoordinateSystem().getVerticalAxis();
        Variable thresholdVariable = thresholdAxis.getOriginalVariable();
        
        Variable gridVariable = gridDatatype.getVariable();
        
        valueDescriptor = new DerivativeValueDescriptor(
                thresholdVariable.getShortName(), // name
                thresholdVariable.findAttribute("standard_name").getStringValue(), // standard_name
                SimpleUnit.factory(thresholdVariable.getUnitsString()),
                thresholdVariable.getDataType(),
                Doubles.asList(thresholdAxis.getCoordValues()),
                gridVariable.getShortName(), // name
                gridVariable.findAttribute("standard_name").getStringValue(), // standard name TODO: ???
                SimpleUnit.factory("days"), // units
                DataType.FLOAT);
        
        timeAxis = gridDatatype.getCoordinateSystem().getTimeAxis1D();
        DateRange timeRange = timeAxis.getDateRange();
        
        timeStepDescriptor = new IntervalTimeStepDescriptor(
                NetCDFDateUtil.convertDateRangeToInterval(timeRange),
                Arrays.asList(new Interval[] {
                    new Interval("1961-01-01/1991-01-01"),
                    new Interval("2011-01-01/2041-01-01"),
                    new Interval("2041-01-01/2071-01-01"),
                    new Interval("2071-01-01/2098-01-01"),
                }));             // TODO: parameterize;
        
        GridCoordSystem gridCoordSystem = gridDatatype.getCoordinateSystem();
        xCount = GridUtility.getXAxisLength(gridCoordSystem);
        yCount = GridUtility.getYAxisLength(gridCoordSystem);
        yxCount = yCount * xCount;
        
        super.traverseStart(gridDatatype);
    }

    @Override
    public boolean zStart(int zIndex) {
        LOGGER.debug("Starting z index of {}", zIndex);
        thresholdIndex = zIndex;
        return super.zStart(zIndex);
    }
    
    @Override
    public boolean tStart(int tIndex) {
        if (super.tStart(tIndex)) {
            LOGGER.debug("Starting t index of {}, {}", tIndex, timeAxis.getTimeDate(tIndex).toGMTString());
            outputTimeStepLengthYears = timeStepDescriptor.getOutputTimeStepInterval(getOutputCurrentTimeStep()).toPeriod().getYears();
            return true;
        } else {
            return false;
        }
    }
    
    @Override
    public void processGridCell(int xCellIndex, int yCellIndex, double value) {
        int arrayIndex = thresholdIndex * yxCount + yCellIndex * xCount + xCellIndex;
        double current = outputArray.getDouble(arrayIndex);
        if (value < 0 || current < 0) {
            outputArray.setObject(arrayIndex, -1);
        } else {
            outputArray.setObject(arrayIndex, current += (value / outputTimeStepLengthYears));
        }
    }

    @Override
    public DerivativeValueDescriptor getValueDescriptor() {
        return valueDescriptor;
    }

    @Override
    public TimeStepDescriptor getTimeStepDescriptor() {
        return timeStepDescriptor;
    }
}
