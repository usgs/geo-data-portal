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
public class DaysBelowTemperatureThresholdVisitor extends DaysBelowThresholdVisitor {
    
    private final static Logger LOGGER = LoggerFactory.getLogger(DaysBelowTemperatureThresholdVisitor.class);

    @Override
    protected DerivativeValueDescriptor generateDerivativeValueDescriptor(List<GridDatatype> gridDatatypeList) {
        return new DerivativeValueDescriptor(
                "threshold", // name
                "air_temperature", // standard_name
                "degF",
                DataType.FLOAT,
                Floats.asList(new float[] { 32f, 10f, 0f}),
                generateDerivativeOutputVariableName(gridDatatypeList, "days_below_threshold"), // name
                "number_of_days_with_air_temperature_below_threshold", // standard name TODO: ???
                "days", // units
                Short.valueOf((short)-1),
                DataType.SHORT);
    }

}
