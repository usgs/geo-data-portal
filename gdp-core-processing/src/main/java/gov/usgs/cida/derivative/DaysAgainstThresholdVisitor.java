package gov.usgs.cida.derivative;

import gov.usgs.cida.derivative.time.NetCDFDateUtil;
import gov.usgs.cida.derivative.time.RepeatingPeriodTimeStepDescriptor;
import gov.usgs.cida.derivative.time.TimeStepDescriptor;
import gov.usgs.cida.gdp.coreprocessing.analysis.grid.GridUtility;
import java.util.List;
import org.joda.time.Years;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.nc2.dataset.CoordinateAxis1DTime;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.units.DateRange;
import ucar.nc2.units.SimpleUnit;
import ucar.units.ConversionException;
import ucar.units.Converter;

/**
 *
 * @author tkunicki
 */
public abstract class DaysAgainstThresholdVisitor extends DerivativeVisitor {
    
    private final static Logger LOGGER = LoggerFactory.getLogger(DaysAboveTemperatureThresholdVisitor.class);
    
    private int xCount;
    private int yCount;
    private int yxCount;
    
    private TimeStepDescriptor timeStepDescriptor;
    
    private Converter thresholdToInputConverter;
    
    private boolean traverseContinue = true;
    
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
        
        SimpleUnit gridUnit = SimpleUnit.factory(gridDatatype.getUnitsString());
        try {
            thresholdToInputConverter = getValueDescriptor().getCoordinateUnit().getUnit().getConverterTo(gridUnit.getUnit());
        } catch (ConversionException e) {
            LOGGER.error("Error converting input to threshold units", e);
            traverseContinue = false;
        }
        
        super.traverseStart(gridDatatype);
    }
    
    @Override
    public boolean traverseContinue() {
        return traverseContinue ? super.traverseContinue() : false;
    }

    @Override
    public void processGridCell(int xCellIndex, int yCellIndex, double value) {
        List<? extends Number> thresholdList = getValueDescriptor().getCoordinateValues();
        int thresholdCount = thresholdList.size();
        for (int thresholdIndex = 0; thresholdIndex < thresholdCount; ++thresholdIndex) {
            float threshold = thresholdToInputConverter.convert(
                    thresholdList.get(thresholdIndex).floatValue());
            int arrayIndex = thresholdIndex * yxCount + yCellIndex * xCount + xCellIndex;
            if (includeValue(threshold, value)) {
                short current = outputArray.getShort(arrayIndex);
                outputArray.setShort(arrayIndex, ++current);
            } else if (Double.isNaN(value)) {
                outputArray.setShort(arrayIndex, (short)-1);
            }
        }
    }

    @Override
    public TimeStepDescriptor getTimeStepDescriptor() {
        return timeStepDescriptor;
    }

    public abstract boolean includeValue(double threshold, double value);
}
