package gov.usgs.derivative;

import gov.usgs.derivative.time.CalendarUtil;
import gov.usgs.derivative.time.TimeStepDescriptor;
import gov.usgs.cida.gdp.coreprocessing.analysis.grid.GridUtility;
import java.io.File;
import java.io.IOException;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.NetcdfFileWriteable;
import ucar.nc2.Variable;
import ucar.nc2.dt.GridCoordSystem;

/**
 *
 * @author tkunicki
 */
public class DerivativeNetCDFFile {
    
    private final String path;
    private final String name;
    private final DerivativeValueDescriptor valueDescriptor;
    private final TimeStepDescriptor timeStepDescriptor;
    private NetcdfFileWriteable netCDFFile;
    private Variable derivativeCoordinateVariable;
    private Variable derivativeOutputVariable;

    public DerivativeNetCDFFile(String path, String baseName, DerivativeValueDescriptor valueDescriptor, TimeStepDescriptor timeStepDescriptor) {
        this.path = path;
        this.name = baseName;
        this.valueDescriptor = valueDescriptor;
        this.timeStepDescriptor = timeStepDescriptor;
    }

    public NetcdfFileWriteable getNetCDFFile() {
        return netCDFFile;
    }

    public Variable getCoordinateVariable() {
        return derivativeOutputVariable;
    }

    public Variable getOutputVariable() {
        return derivativeOutputVariable;
    }

    public synchronized NetcdfFile createOuputNetCDFFile(GridCoordSystem gridCoordSystem) throws IOException, InvalidRangeException {
        if (netCDFFile != null) {
            throw new IllegalStateException("NetCDF Derivative file already created");
        }
        int xCount = GridUtility.getXAxisLength(gridCoordSystem);
        int yCount = GridUtility.getYAxisLength(gridCoordSystem);
        int timeCount = timeStepDescriptor.getOutputTimeStepCount();
        netCDFFile = null;
        try {
            String path = this.path.endsWith(File.separator) ? this.path : this.path.concat(File.separator);
            netCDFFile = NetcdfFileWriteable.createNew(path + name + ".nc"); // TODO: parameterize
            Dimension xDimension = netCDFFile.addDimension(gridCoordSystem.getXHorizAxis().getName(), xCount);
            Dimension yDimension = netCDFFile.addDimension(gridCoordSystem.getYHorizAxis().getName(), yCount);
            Dimension timeDimension = netCDFFile.addDimension("time", timeStepDescriptor.getOutputTimeStepCount());
            Dimension timeBoundsDimension = netCDFFile.addDimension("time_bounds", 2);
            Dimension derivativeCoordinateDimension = netCDFFile.addDimension(valueDescriptor.getCoordinateName(), valueDescriptor.getCoordinateValues().size());
            Variable timeVariable = netCDFFile.addVariable("time", DataType.INT, new Dimension[]{timeDimension});
            timeVariable.addAttribute(new Attribute("units", "days since " + CalendarUtil.formatCF_UTC(timeStepDescriptor.getOutputInterval().getStart().toDate())));
            timeVariable.addAttribute(new Attribute("climatology", "time_bounds"));
            Variable timeBoundsVariable = netCDFFile.addVariable("time_bounds", DataType.INT, new Dimension[]{timeDimension, timeBoundsDimension});
            derivativeCoordinateVariable = netCDFFile.addVariable(valueDescriptor.getCoordinateName(), valueDescriptor.getCoordinateDataType(), new Dimension[]{derivativeCoordinateDimension});
            if (valueDescriptor.getCoordinateStandardName() != null) {
                derivativeCoordinateVariable.addAttribute(new Attribute("standard_name", valueDescriptor.getCoordinateStandardName()));
            }
            if (valueDescriptor.getCoordinateUnitName() != null) {
                derivativeCoordinateVariable.addAttribute(new Attribute("units", valueDescriptor.getCoordinateUnitName()));
            }
            derivativeCoordinateVariable.addAttribute(new Attribute("positive", "up"));
            derivativeOutputVariable = netCDFFile.addVariable(valueDescriptor.getOutputName(), valueDescriptor.getOutputDataType(), new Dimension[]{timeDimension, derivativeCoordinateDimension, yDimension, xDimension});
            if (valueDescriptor.getOutputStandardName() != null) {
                derivativeOutputVariable.addAttribute(new Attribute("standard_name", valueDescriptor.getOutputStandardName()));
            }
            if (valueDescriptor.getOutputUnitName() != null) {
                derivativeOutputVariable.addAttribute(new Attribute("units", valueDescriptor.getOutputUnitName()));
            }
            DataType outputDataType = valueDescriptor.getOutputDataType();
            Number outputMissingValue = valueDescriptor.getOutputMissingValue();
            switch (outputDataType) {
                case BYTE:
                    derivativeOutputVariable.addAttribute(new Attribute("missing_value", outputMissingValue.byteValue()));
                    break;
                case SHORT:
                    derivativeOutputVariable.addAttribute(new Attribute("missing_value", outputMissingValue.shortValue()));
                    break;
                case INT:
                    derivativeOutputVariable.addAttribute(new Attribute("missing_value", outputMissingValue.intValue()));
                    break;
                case LONG:
                    derivativeOutputVariable.addAttribute(new Attribute("missing_value", outputMissingValue.longValue()));
                    break;
                case FLOAT:
                    derivativeOutputVariable.addAttribute(new Attribute("missing_value", outputMissingValue.floatValue()));
                    break;
                case DOUBLE:
                    derivativeOutputVariable.addAttribute(new Attribute("missing_value", outputMissingValue.doubleValue()));
                    break;
            }
            netCDFFile.create();
            Array timeArray = Array.factory(DataType.INT, timeVariable.getShape());
            Array timeBoundsArray = Array.factory(DataType.INT, timeBoundsVariable.getShape());
            for (int timeIndex = 0; timeIndex < timeCount; ++timeIndex) {
                int timeValue = timeStepDescriptor.getDaysFromTimeStepLowerBound(timeIndex);
                timeArray.setInt(timeIndex, timeValue);
                int timeBoundsIndex = timeIndex << 1;
                int timeBoundLowerValue = timeValue;
                int timeBoundUpperValue = timeStepDescriptor.getDaysFromTimeStepUpperBound(timeIndex);
                timeBoundsArray.setInt(timeBoundsIndex, timeBoundLowerValue);
                timeBoundsArray.setInt(timeBoundsIndex + 1, timeBoundUpperValue);
            }
            netCDFFile.write(timeVariable.getName(), timeArray);
            netCDFFile.write(timeBoundsVariable.getName(), timeBoundsArray);
            Array derivativeValuesArray = Array.factory(valueDescriptor.getCoordinateDataType(), derivativeCoordinateVariable.getShape());
            int valueCount = valueDescriptor.getCoordinateValues().size();
            for (int valueIndex = 0; valueIndex < valueCount; ++valueIndex) {
                derivativeValuesArray.setObject(valueIndex, valueDescriptor.getCoordinateValues().get(valueIndex));
            }
            netCDFFile.write(derivativeCoordinateVariable.getName(), derivativeValuesArray);
        } finally {
        }
        return netCDFFile;
    }
    
}
