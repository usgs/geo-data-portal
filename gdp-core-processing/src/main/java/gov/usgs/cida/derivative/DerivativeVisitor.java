package gov.usgs.cida.derivative;

import com.google.common.base.Joiner;
import gov.usgs.cida.derivative.time.TimeStepDescriptor;
import gov.usgs.cida.gdp.coreprocessing.analysis.grid.GridCellVisitor;
import gov.usgs.cida.gdp.coreprocessing.analysis.grid.GridUtility;
import java.io.IOException;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.dataset.CoordinateAxis1DTime;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDatatype;

/**
 *
 * @author tkunicki
 */
public abstract class DerivativeVisitor extends GridCellVisitor {
    
    private final static Logger LOGGER = LoggerFactory.getLogger(DerivativeVisitor.class);

    private DateTime timeStepDateTime;
    
    private int xCount;
    private int yCount;
    
    private int timeStepCount;
    
    private CoordinateAxis1DTime timeAxis;
    
    private int outputCurrentTimeStep;
    
    private DerivativeNetCDFFile derivativeNetCDFFile;
    
    protected Array outputArray;
    
    public DerivativeVisitor() {

    }
    
    public abstract DerivativeValueDescriptor getValueDescriptor();
    
    public abstract TimeStepDescriptor getTimeStepDescriptor();
    
    public String getOutputFileBaseName() {
        return null;
    }
    
    @Override
    public void traverseStart(GridDatatype gridDatatype) {
        
        GridCoordSystem gridCoordSystem = gridDatatype.getCoordinateSystem();
        
        timeAxis = gridCoordSystem.getTimeAxis1D();
        
        xCount = GridUtility.getXAxisLength(gridCoordSystem);
        yCount = GridUtility.getYAxisLength(gridCoordSystem);
      
        outputCurrentTimeStep = -1;
        
        timeStepCount = getTimeStepDescriptor().getOutputTimeStepCount();
        
        LOGGER.info("Calculating derivatives for {} time steps", timeStepCount);
        
        String outputBaseFileName = getOutputFileBaseName();
        if (outputBaseFileName == null || outputBaseFileName.length() < 1) {
            outputBaseFileName = Joiner.on(".").join(
                    "derivative",
                    gridDatatype.getVariable().getName(),
                    getValueDescriptor().getCoordinateName()
//                    Joiner.on(":").join(getValueDescriptor().getCoordinateValues()),
                    );
        }
        
        try {
            derivativeNetCDFFile = new DerivativeNetCDFFile(outputBaseFileName, getValueDescriptor(), getTimeStepDescriptor());
            derivativeNetCDFFile.createOuputNetCDFFile(gridCoordSystem);
        } catch (IOException e) {
            LOGGER.error("Error creating output netcdf file", e);
        } catch (InvalidRangeException e) {
            LOGGER.error("Error creating output netcdf file", e);
        }
    }

    @Override
    public void traverseEnd() {
        try {
            if (outputCurrentTimeStep > -1) {
                writeTimeStepData();
            }
            derivativeNetCDFFile.getNetCDFFile().close();
        } catch (IOException e) {
            LOGGER.error("Error writing output netcdf file", e);
        }
    }
    
    @Override
    public boolean tStart(int tIndex) {
        timeStepDateTime = new DateTime(timeAxis.getTimeDate(tIndex));
        int outputNextTimeStep = getTimeStepDescriptor().getOutputTimeStepIndex(timeStepDateTime);
        if (outputNextTimeStep != outputCurrentTimeStep) {
            if (outputCurrentTimeStep > -1) {
                LOGGER.info("Finished parsing data for {} to {}",
                        getTimeStepDescriptor().getOutputTimeStepLowerBound(outputCurrentTimeStep).toString(),
                        getTimeStepDescriptor().getOutputTimeStepUpperBound(outputCurrentTimeStep).toString());
                writeTimeStepData();
                int arrayCount = xCount * yCount * getValueDescriptor().getCoordinateValues().size();
                for (int arrayIndex = 0; arrayIndex < arrayCount; ++arrayIndex) {
                    outputArray.setObject(arrayIndex, 0); // TODO:  how to know type?
                }
            } else {
                outputArray = Array.factory(
                        getValueDescriptor().getOutputDataType(),
                        new int[] {1, getValueDescriptor().getCoordinateValues().size(), yCount, xCount});
            }
            outputCurrentTimeStep = outputNextTimeStep;
            if (outputCurrentTimeStep > -1 && outputCurrentTimeStep < getTimeStepDescriptor().getOutputTimeStepCount()) {
                LOGGER.info("Start parsing data for {} to {}",
                            getTimeStepDescriptor().getOutputTimeStepLowerBound(outputCurrentTimeStep).toString(),
                            getTimeStepDescriptor().getOutputTimeStepUpperBound(outputCurrentTimeStep).toString());
            }
        }
        return outputCurrentTimeStep > -1;
    }
 
    private void writeTimeStepData() {
        try {
            derivativeNetCDFFile.getNetCDFFile().write(
                    getValueDescriptor().getOutputName(),
                    new int[] { outputCurrentTimeStep, 0, 0, 0},
                    outputArray);
        } catch (IOException e) {
            LOGGER.error("Error writing values to netcdf file", e);
        } catch (InvalidRangeException e) {
            LOGGER.error("Error writing values to netcdf file", e);
        }
    }
    
    public int getOutputCurrentTimeStep() {
        return outputCurrentTimeStep;
    }
}
