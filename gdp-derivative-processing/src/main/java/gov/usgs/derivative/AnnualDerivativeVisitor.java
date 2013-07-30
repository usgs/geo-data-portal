package gov.usgs.derivative;

import com.google.common.base.Joiner;
import gov.usgs.derivative.aparapi.AbstractGridKernel;
import gov.usgs.derivative.aparapi.GridInputTYXKernel;
import gov.usgs.derivative.time.NetCDFDateUtil;
import gov.usgs.derivative.time.RepeatingPeriodTimeStepDescriptor;
import gov.usgs.derivative.time.TimeStepDescriptor;
import gov.usgs.cida.gdp.coreprocessing.analysis.grid.GridType;
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
public abstract class AnnualDerivativeVisitor extends DerivativeGridVisitor {
    
    private final static Logger LOGGER = LoggerFactory.getLogger(AnnualDerivativeVisitor.class);
    protected final String outputDir;
    
    public AnnualDerivativeVisitor(String outputDir) {
        this.outputDir = outputDir;
    }

    @Override
    protected String getOutputFilePath() {
        return this.outputDir;
    }
    
    @Override
    protected String generateOutputFileBaseName(List<GridDatatype> gridDatatypeList) {
        return Joiner.on(".").join(
            "derivative",
            getValueDescriptor().getOutputName());
    }

    @Override
    protected TimeStepDescriptor generateDerivativeTimeStepDescriptor(List<GridDatatype> gridDatatypeList) {
        GridCoordSystem gridCoordSystem = gridDatatypeList.get(0).getCoordinateSystem();
        CoordinateAxis1DTime timeAxis = gridCoordSystem.getTimeAxis1D();
        DateRange timeRange = timeAxis.getDateRange();
        
        return new RepeatingPeriodTimeStepDescriptor(
                NetCDFDateUtil.toIntervalUTC(timeRange),
                Years.ONE);             // TODO: parameterize;
    }
    
    protected String generateDerivativeOutputVariableName(List<GridDatatype> gridDatatypeList, String derivative) {
        return Joiner.on("-").join(
                gridDatatypeList.get(0).getName(),
                derivative);
    }
    
    @Override
    protected AbstractGridKernel generateGridKernel(List<GridDatatype> gridDatatypeList) throws ConversionException, Exception {
        GridCoordSystem gcs = gridDatatypeList.get(0).getCoordinateSystem();
        int yxCount = GridUtility.getXAxisLength(gcs) * GridUtility.getYAxisLength(gcs);
        float[] thresholds = extractConvertedThresholds(gridDatatypeList);
        return createKernel(yxCount, thresholds);
    }
    
    protected abstract AbstractGridKernel createKernel(int yxCount, float[] thresholds);
    
    protected float[] extractConvertedThresholds(List<GridDatatype> gridDatatypeList) throws ConversionException, Exception {
        SimpleUnit gridUnit = SimpleUnit.factory(gridDatatypeList.get(0).getUnitsString());
        
        Converter converter = SimpleUnit.factory(
                    getValueDescriptor().getCoordinateUnitName()).getUnit().getConverterTo(gridUnit.getUnit());
            
        List<? extends Number> thresholdList = getValueDescriptor().getCoordinateValues();
        int thresholdCount = getValueDescriptor().getCoordinateValues().size();
        float[] thresholds = new float[thresholdCount];
        for (int index = 0; index < thresholdCount; ++index) {
            thresholds[index] = converter.convert(thresholdList.get(index).floatValue());
        }
        return thresholds;    
    }
    
    @Override
    // NOTE: *final* as this protects grid structure assumptions in
    // AnnualDerivativeKernel.run() and AnnualDerivativeKernel.addXYInputValues(...)
    protected final boolean isValidInputGridType(GridType gridType) {
        return gridType == GridType.TYX;
    }
    
    protected abstract class AnnualDerivativeKernel extends GridInputTYXKernel {
        
        public AnnualDerivativeKernel(int gInputCount, int yxCount, float[] zValues) {
            super(gInputCount, 366, yxCount, zValues); // 366 since some years have leap days.
        }
        
    }
}
