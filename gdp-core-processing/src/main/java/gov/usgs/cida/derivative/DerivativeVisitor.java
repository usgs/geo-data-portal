package gov.usgs.cida.derivative;

import gov.usgs.cida.derivative.time.TimeStepDescriptor;
import com.google.common.base.Joiner;
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
    
    private int timeStepCurrent;
    
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
      
        timeStepCurrent = -1;
        
        timeStepCount = getTimeStepDescriptor().getOutputTimeStepCount();
        
        LOGGER.info("Calculating derivatives for {} time steps", timeStepCount);
        
        String outputBaseFileName = getOutputFileBaseName();
        if (outputBaseFileName == null || outputBaseFileName.length() < 1) {
            outputBaseFileName = Joiner.on(".").join(
                    "derivative",
                    gridDatatype.getName(),
                    getValueDescriptor().getOutputName(),
                    Joiner.on(":").join(
                        getValueDescriptor().getCoordinateStart(),
                        getValueDescriptor().getCoordinateIncrement(),
                        getValueDescriptor().getCoordinateCount())
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
            if (timeStepCurrent > -1) {
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
        int timeStep = getTimeStepDescriptor().getOutputTimeStepIndex(timeStepDateTime);
        if (timeStep != timeStepCurrent) {
            if (timeStepCurrent > -1) {
                LOGGER.info("Finished parsing data for {} to {}",
                        getTimeStepDescriptor().getOutputTimeStepLowerBound(timeStepCurrent).toString(),
                        getTimeStepDescriptor().getOutputTimeStepUpperBound(timeStepCurrent).toString());
                writeTimeStepData();
                int arrayCount = xCount * yCount * getValueDescriptor().getCoordinateCount();
                for (int arrayIndex = 0; arrayIndex < arrayCount; ++arrayIndex) {
                    outputArray.setObject(arrayIndex, 0); // TODO:  how to know type?
                }
            } else {
                outputArray = Array.factory(
                        getValueDescriptor().getOutputDataType(),
                        new int[] {1, getValueDescriptor().getCoordinateCount(), yCount, xCount});
            }
            timeStepCurrent = timeStep;
            if (timeStepCurrent > -1 && timeStepCurrent < getTimeStepDescriptor().getOutputTimeStepCount()) {
                LOGGER.info("Start parsing data for {} to {}",
                            getTimeStepDescriptor().getOutputTimeStepLowerBound(timeStepCurrent).toString(),
                            getTimeStepDescriptor().getOutputTimeStepUpperBound(timeStepCurrent).toString());
            }
        }
        return timeStepCurrent > -1;
    }
 
    private void writeTimeStepData() {
        try {
            derivativeNetCDFFile.getNetCDFFile().write(
                    getValueDescriptor().getOutputName(),
                    new int[] { timeStepCurrent, 0, 0, 0},
                    outputArray);
        } catch (IOException e) {
            LOGGER.error("Error writing values to netcdf file", e);
        } catch (InvalidRangeException e) {
            LOGGER.error("Error writing values to netcdf file", e);
        }
    }
}
