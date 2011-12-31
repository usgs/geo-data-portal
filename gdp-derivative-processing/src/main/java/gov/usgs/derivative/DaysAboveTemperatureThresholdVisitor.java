package gov.usgs.derivative;

import com.google.common.primitives.Floats;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.DataType;
import ucar.nc2.dt.GridDatatype;

/**
 *
 * @author tkunicki
 */
public class DaysAboveTemperatureThresholdVisitor extends DaysAboveThresholdVisitor {
    
    private final static Logger LOGGER = LoggerFactory.getLogger(DaysAboveTemperatureThresholdVisitor.class);

    @Override
    protected DerivativeValueDescriptor generateDerivativeValueDescriptor(List<GridDatatype> gridDatatypeList) {
        return new DerivativeValueDescriptor(
                "threshold", // name
                "air_temperature", // standard_name
                "degF", // units
                DataType.FLOAT,
                Floats.asList(new float[] { 90f, 95f, 100f } ),
                generateDerivativeOutputVariableName(gridDatatypeList, "days_above_threshold"), // name
                "number_of_days_with_air_temperature_above_threshold", // standard name TODO: ???
                "days", // units
                Short.valueOf((short)-1),
                DataType.SHORT);
    }

}
