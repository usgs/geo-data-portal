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
import ucar.nc2.constants.CF;
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
//    private Variable derivativeCoordinateVariable;
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

//    public Variable getCoordinateVariable() {
//        return derivativeOutputVariable;
//    }

//    public Variable getOutputVariable() {
//        return derivativeOutputVariable;
//    }

    public synchronized NetcdfFile createOuputNetCDFFile(GridCoordSystem gridCoordSystem) throws IOException, InvalidRangeException {
        if (netCDFFile != null) {
            throw new IllegalStateException("NetCDF Derivative file already created");
        }
        final int xCount = GridUtility.getXAxisLength(gridCoordSystem);
        final int yCount = GridUtility.getYAxisLength(gridCoordSystem);
        final int timeCount = timeStepDescriptor.getOutputTimeStepCount();
        
        final boolean derivativeCoordinateValid = valueDescriptor.isDerivativeCoordinateValid();
        
        netCDFFile = null;
        try {
            
            String path = this.path.endsWith(File.separator) ? this.path : this.path.concat(File.separator);
            File pathAsFile = new File(path);
            if (!pathAsFile.exists() && !pathAsFile.mkdirs() ) {
                throw new IOException("Unable to create directory for derivatice NetCDF file: " + path);
            }
            
            final String timeName = gridCoordSystem.getTimeAxis1D().getName();
            final String timeBoundsName = timeName + "_bounds";
            
            netCDFFile = NetcdfFileWriteable.createNew(path + name + ".nc"); // TODO: parameterize
            
            Dimension xDimension = netCDFFile.addDimension(gridCoordSystem.getXHorizAxis().getName(), xCount);
            Dimension yDimension = netCDFFile.addDimension(gridCoordSystem.getYHorizAxis().getName(), yCount);
            Dimension timeDimension = netCDFFile.addDimension(timeName, timeStepDescriptor.getOutputTimeStepCount());
            Dimension timeBoundsDimension = netCDFFile.addDimension(timeBoundsName, 2);
            
            Variable timeVariable = netCDFFile.addVariable(timeName, DataType.INT, new Dimension[]{timeDimension});
            timeVariable.addAttribute(new Attribute(CF.UNITS, "days since " + CalendarUtil.formatCF_UTC(timeStepDescriptor.getOutputInterval().getStart().toDate())));
            timeVariable.addAttribute(new Attribute("climatology", "time_bounds"));
            Variable timeBoundsVariable = netCDFFile.addVariable(timeBoundsName, DataType.INT, new Dimension[]{timeDimension, timeBoundsDimension});
            
            Dimension derivativeCoordinateDimension = null;
            Variable derivativeCoordinateVariable = null;
            if (derivativeCoordinateValid) {
                derivativeCoordinateDimension = netCDFFile.addDimension(valueDescriptor.getCoordinateName(), valueDescriptor.getCoordinateValues().size());
                derivativeCoordinateVariable = netCDFFile.addVariable(valueDescriptor.getCoordinateName(), valueDescriptor.getCoordinateDataType(), new Dimension[]{derivativeCoordinateDimension});
                if (valueDescriptor.getCoordinateStandardName() != null) {
                    derivativeCoordinateVariable.addAttribute(new Attribute(CF.STANDARD_NAME, valueDescriptor.getCoordinateStandardName()));
                }
                if (valueDescriptor.getCoordinateUnitName() != null) {
                    derivativeCoordinateVariable.addAttribute(new Attribute(CF.UNITS, valueDescriptor.getCoordinateUnitName()));
                }
                derivativeCoordinateVariable.addAttribute(new Attribute("positive", CF.POSITIVE_UP));
            }
            
            derivativeOutputVariable = derivativeCoordinateValid ?
                    netCDFFile.addVariable(valueDescriptor.getOutputName(), valueDescriptor.getOutputDataType(), new Dimension[]{timeDimension, derivativeCoordinateDimension, yDimension, xDimension}) :
                    netCDFFile.addVariable(valueDescriptor.getOutputName(), valueDescriptor.getOutputDataType(), new Dimension[]{timeDimension, yDimension, xDimension});
            if (valueDescriptor.getOutputStandardName() != null) {
                derivativeOutputVariable.addAttribute(new Attribute(CF.STANDARD_NAME, valueDescriptor.getOutputStandardName()));
            }
            if (valueDescriptor.getOutputUnitName() != null) {
                derivativeOutputVariable.addAttribute(new Attribute(CF.UNITS, valueDescriptor.getOutputUnitName()));
            }
            DataType outputDataType = valueDescriptor.getOutputDataType();
            Number outputMissingValue = valueDescriptor.getOutputMissingValue();
            switch (outputDataType) {
                case BYTE:
                    derivativeOutputVariable.addAttribute(new Attribute(CF.MISSING_VALUE, outputMissingValue.byteValue()));
                    derivativeOutputVariable.addAttribute(new Attribute("_FillValue", outputMissingValue.byteValue()));
                    break;
                case SHORT:
                    derivativeOutputVariable.addAttribute(new Attribute(CF.MISSING_VALUE, outputMissingValue.shortValue()));
                    derivativeOutputVariable.addAttribute(new Attribute("_FillValue", outputMissingValue.shortValue()));
                    break;
                case INT:
                    derivativeOutputVariable.addAttribute(new Attribute(CF.MISSING_VALUE, outputMissingValue.intValue()));
                    derivativeOutputVariable.addAttribute(new Attribute("_FillValue", outputMissingValue.intValue()));
                    break;
                case LONG:
                    derivativeOutputVariable.addAttribute(new Attribute(CF.MISSING_VALUE, outputMissingValue.longValue()));
                    derivativeOutputVariable.addAttribute(new Attribute("_FillValue", outputMissingValue.longValue()));
                    break;
                case FLOAT:
                    derivativeOutputVariable.addAttribute(new Attribute(CF.MISSING_VALUE, outputMissingValue.floatValue()));
                    derivativeOutputVariable.addAttribute(new Attribute("_FillValue", outputMissingValue.floatValue()));
                    break;
                case DOUBLE:
                    derivativeOutputVariable.addAttribute(new Attribute(CF.MISSING_VALUE, outputMissingValue.doubleValue()));
                    derivativeOutputVariable.addAttribute(new Attribute("_FillValue", outputMissingValue.doubleValue()));
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
            
            if (derivativeCoordinateValid) {
                Array derivativeValuesArray = Array.factory(valueDescriptor.getCoordinateDataType(), derivativeCoordinateVariable.getShape());
                int valueCount = valueDescriptor.getCoordinateValues().size();
                for (int valueIndex = 0; valueIndex < valueCount; ++valueIndex) {
                    derivativeValuesArray.setObject(valueIndex, valueDescriptor.getCoordinateValues().get(valueIndex));
                }
                netCDFFile.write(derivativeCoordinateVariable.getName(), derivativeValuesArray);
            }
        } finally {
        }
        return netCDFFile;
    }
    
}
