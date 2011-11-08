package gov.usgs.cida.derivative;

import ucar.ma2.DataType;

/**
 *
 * @author tkunicki
 */
public class DerivativeValueDescriptor {
    private final String coordinateName;
    private final String coordinateStandardName;
    private final String coordinateUnits;
    private final int coordinateCount;
    private final float coordinateStart;
    private final float coordinateIncrement;
    private final String outputName;
    private final String outputStandardName;
    private final String outputUnits;
    private final DataType outputDataType;

    public DerivativeValueDescriptor(String coordinateName, String coordinateStandardName, String coordinateUnits, float coordinateStart, float coordinateIncrement, int coordinateCount, String outputName, String outputStandardName, String outputUnits, DataType outputDataType) {
        this.coordinateName = coordinateName;
        this.coordinateStandardName = coordinateStandardName;
        this.coordinateUnits = coordinateUnits;
        this.coordinateStart = coordinateStart;
        this.coordinateIncrement = coordinateIncrement;
        this.coordinateCount = coordinateCount;
        this.outputName = outputName;
        this.outputStandardName = outputStandardName;
        this.outputUnits = outputUnits;
        this.outputDataType = outputDataType;
    }

    public int getCoordinateCount() {
        return coordinateCount;
    }

    public float getCoordinateIncrement() {
        return coordinateIncrement;
    }

    public String getCoordinateName() {
        return coordinateName;
    }

    public String getCoordinateStandardName() {
        return coordinateStandardName;
    }

    public float getCoordinateStart() {
        return coordinateStart;
    }

    public String getCoordinateUnits() {
        return coordinateUnits;
    }

    public String getOutputName() {
        return outputName;
    }

    public String getOutputStandardName() {
        return outputStandardName;
    }

    public String getOutputUnits() {
        return outputUnits;
    }

    public DataType getOutputDataType() {
        return outputDataType;
    }

    public float getCoordinateValue(int coordinateIndex) {
        return coordinateStart + ((float) coordinateIndex * coordinateIncrement);
    }
    
}
