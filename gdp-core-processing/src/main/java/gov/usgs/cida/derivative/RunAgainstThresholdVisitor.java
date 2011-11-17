package gov.usgs.cida.derivative;

import gov.usgs.cida.derivative.time.NetCDFDateUtil;
import gov.usgs.cida.derivative.time.RepeatingPeriodTimeStepDescriptor;
import gov.usgs.cida.derivative.time.TimeStepDescriptor;
import gov.usgs.cida.gdp.coreprocessing.analysis.grid.GridUtility;
import java.util.ArrayList;
import java.util.Arrays;
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
public abstract class RunAgainstThresholdVisitor extends DerivativeVisitor {
    
    private final static Logger LOGGER = LoggerFactory.getLogger(DaysAboveTemperatureThresholdVisitor.class);
    
    private int xCount;
    private int yCount;
    private int yxCount;
    
    private TimeStepDescriptor timeStepDescriptor;
    
    private Converter thresholdToInputConverter;
    private List<Number> convertedThresholdValueList;
    
    private boolean traverseContinue = true;
    
    private int thresholdCount;
    
    private short[][] outputTimeStepCurrentObservedRunData;
    private short[][] outputTimeStepMaximumObservedRunData;
    
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
            convertedThresholdValueList = new ArrayList(getValueDescriptor().getCoordinateValues());
            for (int index = 0; index < convertedThresholdValueList.size(); ++index) {
                convertedThresholdValueList.set(index, thresholdToInputConverter.convert(convertedThresholdValueList.get(index).floatValue()));
            }
        } catch (ConversionException e) {
            LOGGER.error("Error converting input to threshold units", e);
            traverseContinue = false;
        }
        
        thresholdCount = getValueDescriptor().getCoordinateValues().size();
        outputTimeStepCurrentObservedRunData = new short[thresholdCount][yxCount];
        outputTimeStepMaximumObservedRunData = new short[thresholdCount][yxCount];
        
        super.traverseStart(gridDatatype);
    }
    
    @Override
    public boolean traverseContinue() {
        return traverseContinue ? super.traverseContinue() : false;
    }

    @Override
    public void processGridCell(int xCellIndex, int yCellIndex, double value) {
        int yxIndex = yCellIndex * xCount + xCellIndex;
        for (int thresholdIndex = 0; thresholdIndex < thresholdCount; ++thresholdIndex) {
            float threshold = convertedThresholdValueList.get(thresholdIndex).floatValue();
            if (includeValue(threshold, value)) {
                short current = ++outputTimeStepCurrentObservedRunData[thresholdIndex][yxIndex];
                short maximum = outputTimeStepMaximumObservedRunData[thresholdIndex][yxIndex];
                if (current > maximum) {
                    outputTimeStepMaximumObservedRunData[thresholdIndex][yxIndex] = current;
                }
            } else {
                outputTimeStepCurrentObservedRunData[thresholdIndex][yxIndex] = 0;
            }
        }
    }

    @Override
    public TimeStepDescriptor getTimeStepDescriptor() {
        return timeStepDescriptor;
    }

    public abstract boolean includeValue(double threshold, double value);


    @Override
    protected void outputTimeStepStart(int outputTimeStepIndex) {
        super.outputTimeStepStart(outputTimeStepIndex);
        
        for (short[] runData : outputTimeStepCurrentObservedRunData) {
            Arrays.fill(runData, (short)0);
        }
        for (short[] runData : outputTimeStepMaximumObservedRunData) {
            Arrays.fill(runData, (short)0);
        }
    }
    
    @Override
    protected void outputTimeStepEnd(int outputTimeStepIndex) {
        super.outputTimeStepEnd(outputTimeStepIndex);
        
        for (int thresholdIndex = 0; thresholdIndex < thresholdCount; ++thresholdIndex) {
            for (int yxIndex = 0; yxIndex < yxCount; ++yxIndex) {
                int arrayIndex = thresholdIndex * yxCount + yxIndex;
                outputArray.setShort(arrayIndex, outputTimeStepMaximumObservedRunData[thresholdIndex][yxIndex]);
            }
        }
        
    }
    
    
}
