package gov.usgs.derivative;

import com.google.common.base.Joiner;
import gov.usgs.derivative.aparapi.AbstractGridKernel;
import gov.usgs.derivative.grid.GridVisitor;
import gov.usgs.derivative.time.TimeStepDescriptor;
import gov.usgs.cida.gdp.coreprocessing.analysis.grid.GridType;
import gov.usgs.cida.gdp.coreprocessing.analysis.grid.GridUtility;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.dataset.CoordinateAxis1DTime;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDatatype;
import ucar.units.ConversionException;

/**
 *
 * @author tkunicki
 */
public abstract class DerivativeGridVisitor extends GridVisitor {
    
    private final static Logger LOGGER = LoggerFactory.getLogger(DerivativeGridVisitor.class);
    
    private int xCount;
    private int yCount;
    private int yxCount;
    
    private CoordinateAxis1DTime timeAxis;
    
    private int cal365OffsetHACK = 0;
    
    private int outputTimeStepCurrentIndex;
    
    private DerivativeValueDescriptor valueDescriptor;
    private TimeStepDescriptor timeStepDescriptor;
    private DerivativeNetCDFFile derivativeNetCDFFile;
    
    private boolean traverseContinue; 
    
    protected List<Number> missingValuesList;
    protected boolean[] missingValuesMask = null;
    
    protected Array outputArray;
    
    protected AbstractGridKernel kernel;
    
    public DerivativeGridVisitor() {
        traverseContinue = true;
    }
    
    public final DerivativeValueDescriptor getValueDescriptor() {
        return valueDescriptor;
    }
    
    public final TimeStepDescriptor getTimeStepDescriptor() {
        return timeStepDescriptor;
    }
    
    public final AbstractGridKernel getKernel() {
        return kernel;
    }
    
    protected abstract String getOutputFilePath();
    
    protected String generateOutputFileBaseName(List<GridDatatype> gridDatatypeList) {
        return Joiner.on(".").join(
            "derivative",
            gridDatatypeList.get(0).getVariable().getName(),
            getValueDescriptor().getOutputName()
//                    Joiner.on(":").join(getValueDescriptor().getCoordinateValues()),
            );
    }
    
    protected abstract DerivativeValueDescriptor generateDerivativeValueDescriptor(List<GridDatatype> gridDatatypeList);
    
    protected abstract TimeStepDescriptor generateDerivativeTimeStepDescriptor(List<GridDatatype> gridDatatypeList);
    
    protected abstract AbstractGridKernel generateGridKernel(List<GridDatatype> gridDatatypeList) throws ConversionException, Exception;
    
    @Override
    public void traverseStart(List<GridDatatype> gridDatatypeList) {
        super.traverseStart(gridDatatypeList);
        
        if (!validGridDataTypeList(gridDatatypeList)) {
            LOGGER.error("Invalid GridDatatype(s) encountered for this instance of {}.", getClass().getName());
            traverseContinue = false;
            return;
        }
        
        // GRR. HACK...
        missingValuesList = new ArrayList<Number>(gridDatatypeList.size());
        for (int gridIndex = 0; gridIndex < gridDatatypeList.size(); ++gridIndex) {
            GridDatatype gdt = gridDatatypeList.get(gridIndex);
            Attribute att = gdt.findAttributeIgnoreCase("missing_value");
            if (att != null) {
                Number n = att.getNumericValue();
                if (n != null) {
                    missingValuesList.add(n);
                } else {
                    missingValuesList.add(Float.NaN);
                }
            } else {
                missingValuesList.add(Float.NaN);
            }
            
        }
        
        GridCoordSystem gridCoordSystem = gridDatatypeList.get(0).getCoordinateSystem();
        
        timeAxis = gridCoordSystem.getTimeAxis1D();
        
        xCount = GridUtility.getXAxisLength(gridCoordSystem);
        yCount = GridUtility.getYAxisLength(gridCoordSystem);
        yxCount = xCount* yCount;
      
        outputTimeStepCurrentIndex = -1;
        
        valueDescriptor = generateDerivativeValueDescriptor(gridDatatypeList);
        timeStepDescriptor = generateDerivativeTimeStepDescriptor(gridDatatypeList);
        try {
            derivativeNetCDFFile = new DerivativeNetCDFFile(
                    getOutputFilePath(),
                    generateOutputFileBaseName(gridDatatypeList),
                    valueDescriptor,
                    timeStepDescriptor);
            derivativeNetCDFFile.createOuputNetCDFFile(gridCoordSystem);
            
            
        } catch (IOException e) {
            LOGGER.error("Error creating output netcdf file", e);
            traverseContinue = false;
        } catch (InvalidRangeException e) {
            LOGGER.error("Error creating output netcdf file", e);
            traverseContinue = false;
        }
        
        try {
            kernel = generateGridKernel(gridDatatypeList);
        } catch (ConversionException e) {
            LOGGER.error("Error converting input grid units to threshold units", e);
            traverseContinue = false;
        } catch (Exception e) {
            // Bah, SimpleUnit.factoryWithExceptions just shows Exception?  double bah!
            LOGGER.error("Error parsing units for threshold",  e);
            traverseContinue = false;
        }
        
        LOGGER.info("Calculating derivatives for {} time steps", getTimeStepDescriptor().getOutputTimeStepCount());
    }
    
    @Override
    public boolean traverseContinue() {
        return traverseContinue ? super.traverseContinue() : false;
    }

    @Override
    public void traverseEnd() {
        try {
            if (outputTimeStepCurrentIndex > -1) {
                outputTimeStepEnd(outputTimeStepCurrentIndex);
                writeTimeStepData();
            }
            derivativeNetCDFFile.getNetCDFFile().close();
        } catch (IOException e) {
            LOGGER.error("Error writing output netcdf file", e);
        }
        kernel.dispose();
        kernel = null;
    }
    
    @Override
    public boolean tStart(int tIndex) {
        DateTime inputTimeStepDateTime = new DateTime(timeAxis.getTimeDate(tIndex));
        // HACK START
        if (inputTimeStepDateTime.getMonthOfYear() == 2 && inputTimeStepDateTime.getDayOfMonth() == 29) {
            cal365OffsetHACK++;
        }
        inputTimeStepDateTime = inputTimeStepDateTime.plusDays(cal365OffsetHACK);
        // HACK END
        int outputTimeStepNextIndex = getTimeStepDescriptor().getOutputTimeStepIndex(inputTimeStepDateTime);
        if (outputTimeStepNextIndex != outputTimeStepCurrentIndex) {
            if (outputTimeStepCurrentIndex > -1) {
                outputTimeStepEnd(outputTimeStepCurrentIndex);
                writeTimeStepData();
                int arrayCount = yxCount * getValueDescriptor().getCoordinateValues().size();
                for (int arrayIndex = 0; arrayIndex < arrayCount; ++arrayIndex) {
                    outputArray.setObject(arrayIndex, 0);
                }
            } else {
                outputArray = Array.factory(
                        getValueDescriptor().getOutputDataType(),
                        new int[] {1, getValueDescriptor().getCoordinateValues().size(), yCount, xCount});
            }
            outputTimeStepCurrentIndex = outputTimeStepNextIndex;
            if (outputTimeStepCurrentIndex > -1 && outputTimeStepCurrentIndex < getTimeStepDescriptor().getOutputTimeStepCount()) {
                outputTimeStepStart(outputTimeStepCurrentIndex);
                LOGGER.debug("Start parsing data for {} to {}",
                            getTimeStepDescriptor().getOutputTimeStepLowerBound(outputTimeStepCurrentIndex).toString(),
                            getTimeStepDescriptor().getOutputTimeStepUpperBound(outputTimeStepCurrentIndex).toString());
            }
        }
        return outputTimeStepCurrentIndex > -1 || visitAllTimeSteps();
    }
    
    @Override
    public void yxStart(List<float[]> yxValuesList) {
        if (missingValuesMask == null) {
            missingValuesMask = DerivativeUtil.generateMissingValuesMask(yxValuesList, missingValuesList);
        }
        kernel.addYXInputValues(yxValuesList);
    }
 
    private void writeTimeStepData() {
        try {
            derivativeNetCDFFile.getNetCDFFile().write(
                    getValueDescriptor().getOutputName(),
                    new int[] { outputTimeStepCurrentIndex, 0, 0, 0},
                    outputArray);
            LOGGER.debug("Wrote data for {} to {}",
                        getTimeStepDescriptor().getOutputTimeStepLowerBound(outputTimeStepCurrentIndex).toString(),
                        getTimeStepDescriptor().getOutputTimeStepUpperBound(outputTimeStepCurrentIndex).toString());
        } catch (IOException e) {
            LOGGER.error("Error writing values to netcdf file", e);
        } catch (InvalidRangeException e) {
            LOGGER.error("Error writing values to netcdf file", e);
        }
    }
    
    public int getOutputCurrentTimeStep() {
        return outputTimeStepCurrentIndex;
    }
    
    protected void outputTimeStepStart(int outputTimeStepIndex) {}
    
    protected final void outputTimeStepEnd(int outputTimeStepIndex) {
        
        kernel.execute();
        LOGGER.debug("Kernel Execution: {} {}ms", kernel.getExecutionMode(), kernel.getExecutionTime());
        
        float[] zyxOutputValues = kernel.getZYXOutputValues();
        
        Number outputMissingValue = getValueDescriptor().getOutputMissingValue();
        int outputArrayIndex = 0;
        int zyxOutputIndex = 0;
        for (int zIndex = 0; zIndex < kernel.getZCount(); ++zIndex) {
            for (int yxIndex = 0; yxIndex < kernel.getYXCount(); ++yxIndex) {
                // TODO:  Warning, autobox/unbox, do we care about penalty?
                outputArray.setObject(
                        outputArrayIndex++,
                        // TODO:  Don't assume output MissingValue!
                        missingValuesMask[yxIndex] ? outputMissingValue : zyxOutputValues[zyxOutputIndex]);
                zyxOutputIndex++;
            }
            zyxOutputIndex += kernel.getYXPadding();
        }
    }
    
    // if false, only timesteps which overlap output timesteps will be passed.
    protected boolean visitAllTimeSteps() {
        return false;
    }
    
    protected abstract int requiredInputGridCount();
    
    protected abstract GridType requiredInputGridType();
    
    protected final boolean validGridDataTypeList(List<GridDatatype> gridDataTypeList) {
        if (gridDataTypeList.size() != requiredInputGridCount()) {
            LOGGER.error("Invalid GridDatatype count for this visitor: expected {}, found {}", requiredInputGridCount(), gridDataTypeList.size());
            return false;
        }
        for (GridDatatype gridDatatype : gridDataTypeList) {
            GridType gridType = GridType.findGridType(gridDatatype.getCoordinateSystem());
            if (gridType != requiredInputGridType()) {
                LOGGER.error("Invalid GridType for this visitor:  expected {}, found {} ", requiredInputGridType(), gridType);
                return false;
            }
        }
        return true;
    }
}
