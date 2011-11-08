package gov.usgs.cida.derivative;

import gov.usgs.cida.derivative.time.IntervalTimeStepDescriptor;
import gov.usgs.cida.derivative.time.NetCDFDateUtil;
import gov.usgs.cida.derivative.time.TimeStepDescriptor;
import gov.usgs.cida.gdp.coreprocessing.analysis.grid.GridUtility;
import java.util.Arrays;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.DataType;
import ucar.ma2.Range;
import ucar.nc2.Variable;
import ucar.nc2.dataset.CoordinateAxis1D;
import ucar.nc2.dataset.CoordinateAxis1DTime;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.units.DateRange;

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
    
    private int thresholdIndex = 0;
    
    public TimeStepAveragingVisitor() {
        
    }
    
    CoordinateAxis1DTime timeAxis;
    @Override
    public void traverseStart(GridDatatype gridDatatype) {
        
        CoordinateAxis1D thesholdAxis = gridDatatype.getCoordinateSystem().getVerticalAxis();
        Variable thresholdVariable = thesholdAxis.getOriginalVariable();
        float thresholdMinimum = (float)thesholdAxis.getMinValue();
        float thresholdIncrement = (float)thesholdAxis.getIncrement();
        int thresholdCount = thesholdAxis.getShape(0);
        
        Variable gridVariable = gridDatatype.getVariable();
        
        valueDescriptor = new DerivativeValueDescriptor(
                thresholdVariable.getShortName(), // name
                thresholdVariable.findAttribute("standard_name").getStringValue(), // standard_name
                thresholdVariable.getUnitsString(),
                thresholdMinimum, // start   // TODO: parameterize;
                thresholdIncrement, // increment // TODO: parameterize;
                thresholdCount, // count    // TODO: parameterize;
                gridVariable.getShortName() + "-P30Y", // name
                gridVariable.findAttribute("standard_name").getStringValue(), // standard name TODO: ???
                "days", // units
                DataType.FLOAT);
        
        timeAxis = gridDatatype.getCoordinateSystem().getTimeAxis1D();
        DateRange timeRange = timeAxis.getDateRange();
        
        timeStepDescriptor = new IntervalTimeStepDescriptor(
                NetCDFDateUtil.convertDateRangeToInterval(timeRange),
                Arrays.asList(new Interval[] {
                    new Interval("1961-01-01/1991-01-01"),
                    new Interval("2011-01-01/2041-01-01"),
                    new Interval("2041-01-01/2071-01-01"),
                    new Interval("2071-01-01/2099-01-01"),
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
        LOGGER.debug("Starting t index of {}, {}", tIndex, timeAxis.getTimeDate(tIndex));
        return super.tStart(tIndex);
    }
    
    @Override
    public void processGridCell(int xCellIndex, int yCellIndex, double value) {
        int arrayIndex = thresholdIndex * yxCount + yCellIndex * xCount + xCellIndex;
        if (value > 0) {
            double current = outputArray.getShort(arrayIndex);
//            outputArray.setShort(arrayIndex, current += value);
            outputArray.setObject(arrayIndex, current += (value / 30d));
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
    
    public static void main(String[] args) {
        
    }

}
