package gov.usgs.cida.derivative;

import gov.usgs.cida.derivative.time.NetCDFDateUtil;
import gov.usgs.cida.derivative.time.RepeatingPeriodTimeStepDescriptor;
import gov.usgs.cida.derivative.time.TimeStepDescriptor;
import gov.usgs.cida.gdp.coreprocessing.analysis.grid.GridUtility;
import org.joda.time.Years;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.DataType;
import ucar.nc2.dataset.CoordinateAxis1DTime;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.units.DateRange;

/**
 *
 * @author tkunicki
 */
public class DaysAboveTemperatureThresholdVisitor extends DerivativeVisitor {
    
    private final static Logger LOGGER = LoggerFactory.getLogger(DaysAboveTemperatureThresholdVisitor.class);
    
    private int xCount;
    private int yCount;
    private int yxCount;
    
    private DerivativeValueDescriptor valueDescriptor;
    private TimeStepDescriptor timeStepDescriptor;
    
    public DaysAboveTemperatureThresholdVisitor() {
        valueDescriptor = new DerivativeValueDescriptor(
                "threshold", // name
                "air_temperature", // standard_name
                "degC",
                -5f, // start   // TODO: parameterize;
                5f, // increment // TODO: parameterize;
                10, // count    // TODO: parameterize;
                "days_above_threshold", // name
                "number_of_days_with_air_temperature_above_threshold", // standard name TODO: ???
                "days", // units
                DataType.SHORT);
    }
    
    @Override
    public void traverseStart(GridDatatype gridDatatype) {
        
        CoordinateAxis1DTime timeAxis = gridDatatype.getCoordinateSystem().getTimeAxis1D();
        DateRange timeRange = timeAxis.getDateRange();
        
        timeStepDescriptor = new RepeatingPeriodTimeStepDescriptor(
                NetCDFDateUtil.convertDateRangeToInterval(timeRange),
                Years.ONE);             // TODO: parameterize;
        
        GridCoordSystem gridCoordSystem = gridDatatype.getCoordinateSystem();
        xCount = GridUtility.getXAxisLength(gridCoordSystem);
        yCount = GridUtility.getYAxisLength(gridCoordSystem);
        yxCount = yCount * xCount;
        
        super.traverseStart(gridDatatype);
    }

    @Override
    public void processGridCell(int xCellIndex, int yCellIndex, double value) {
        int thresholdCount = valueDescriptor.getCoordinateCount();
        for (int thresholdIndex = 0; thresholdIndex < thresholdCount; ++thresholdIndex) {
            float threshold = valueDescriptor.getCoordinateValue(thresholdIndex);
            int arrayIndex = thresholdIndex * yxCount + yCellIndex * xCount + xCellIndex;
            if (value > threshold) {
                short current = outputArray.getShort(arrayIndex);
                outputArray.setShort(arrayIndex, ++current);
            } else if (Double.isNaN(value)) {
                outputArray.setShort(arrayIndex, (short)-1);
            }
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
