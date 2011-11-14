package gov.usgs.cida.derivative;

import java.util.ArrayList;
import java.util.List;
import ucar.ma2.DataType;
import ucar.nc2.units.SimpleUnit;
import ucar.units.Unit;

/**
 *
 * @author tkunicki
 */
public class DerivativeValueDescriptor {
    
    private final String coordinateName;
    private final String coordinateStandardName;
    private final SimpleUnit coordinateUnit;
    private final DataType coordinateDataType;
    
    private List<? extends Number> coordinateValues;
    
    private final String outputName;
    private final String outputStandardName;
    private final SimpleUnit outputUnit;
    private final DataType outputDataType;

    public DerivativeValueDescriptor(
            String coordinateName,
            String coordinateStandardName,
            SimpleUnit coordinateUnit,
            DataType coordinateDataType,
            float coordinateStart,
            float coordinateIncrement,
            int coordinateCount,
            String outputName,
            String outputStandardName,
            SimpleUnit outputUnit,
            DataType outputDataType) {
        
        this.coordinateName = coordinateName;
        this.coordinateStandardName = coordinateStandardName;
        this.coordinateUnit = coordinateUnit;
        this.coordinateDataType = coordinateDataType;
        
        List<Float> coordinateValues = new ArrayList<Float>(coordinateCount);
        for(int coordinateIndex = 0; coordinateIndex < coordinateCount; ++coordinateIndex) {
            coordinateValues.add(Float.valueOf(coordinateStart + coordinateIndex * coordinateIncrement));
        }
        this.coordinateValues = coordinateValues;
        
        this.outputName = outputName;
        this.outputStandardName = outputStandardName;
        this.outputUnit = outputUnit;
        this.outputDataType = outputDataType;
    }
    
    public DerivativeValueDescriptor(
            String coordinateName,
            String coordinateStandardName,
            SimpleUnit coordinateUnit,
            DataType coordinateDataType,
            List<? extends Number> coordinateValues,
            String outputName,
            String outputStandardName,
            SimpleUnit outputUnit,
            DataType outputDataType) {
        
        this.coordinateName = coordinateName;
        this.coordinateStandardName = coordinateStandardName;
        this.coordinateUnit = coordinateUnit;
        this.coordinateDataType = coordinateDataType;
        
        this.coordinateValues = coordinateValues;
        
        this.outputName = outputName;
        this.outputStandardName = outputStandardName;
        this.outputUnit = outputUnit;
        this.outputDataType = outputDataType;
    }

    public String getCoordinateName() {
        return coordinateName;
    }

    public String getCoordinateStandardName() {
        return coordinateStandardName;
    }

    public SimpleUnit getCoordinateUnit() {
        return coordinateUnit;
    }
    
    public DataType getCoordinateDataType() {
        return coordinateDataType;
    }
    
    public List<? extends Number> getCoordinateValues() {
        return coordinateValues;
    }

    public String getOutputName() {
        return outputName;
    }

    public String getOutputStandardName() {
        return outputStandardName;
    }

    public SimpleUnit getOutputUnit() {
        return outputUnit;
    }

    public DataType getOutputDataType() {
        return outputDataType;
    }

}
