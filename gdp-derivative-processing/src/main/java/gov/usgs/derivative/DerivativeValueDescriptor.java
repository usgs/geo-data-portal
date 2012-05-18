package gov.usgs.derivative;

import java.util.List;
import ucar.ma2.DataType;

/**
 *
 * @author tkunicki
 */
public class DerivativeValueDescriptor {
    
    private final String coordinateName;
    private final String coordinateStandardName;
    private final String coordinateUnitName;
    private final DataType coordinateDataType;
    
    private List<? extends Number> coordinateValues;
    
    private final String outputName;
    private final String outputStandardName;
    private final String outputUnitName;
    private final Number outputMissingValue;
    private final DataType outputDataType;

    
    public DerivativeValueDescriptor(
            String coordinateName,
            String coordinateStandardName,
            String coordinateUnitName,
            DataType coordinateDataType,
            List<? extends Number> coordinateValues,
            String outputName,
            String outputStandardName,
            String outputUnitName,
            Number outputMissingValue,
            DataType outputDataType) {
        
        this.coordinateName = coordinateName;
        this.coordinateStandardName = coordinateStandardName;
        this.coordinateUnitName = coordinateUnitName;
        this.coordinateDataType = coordinateDataType;
        
        this.coordinateValues = coordinateValues;
        
        this.outputName = outputName;
        this.outputStandardName = outputStandardName;
        this.outputUnitName = outputUnitName;
        this.outputMissingValue = outputMissingValue;
        this.outputDataType = outputDataType;
    }

    public String getCoordinateName() {
        return coordinateName;
    }

    public String getCoordinateStandardName() {
        return coordinateStandardName;
    }

    public String getCoordinateUnitName() {
        return coordinateUnitName;
    }
    
    public DataType getCoordinateDataType() {
        return coordinateDataType;
    }
    
    public List<? extends Number> getCoordinateValues() {
        return coordinateValues;
    }
    
    public boolean isDerivativeCoordinateValid() {
        return coordinateName != null 
                && coordinateStandardName != null
                && coordinateUnitName != null
                && coordinateValues != null;
    }

    public String getOutputName() {
        return outputName;
    }

    public String getOutputStandardName() {
        return outputStandardName;
    }

    public String getOutputUnitName() {
        return outputUnitName;
    }
    
    public Number getOutputMissingValue() {
        return outputMissingValue;
    }

    public DataType getOutputDataType() {
        return outputDataType;
    }

}
