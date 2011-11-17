package gov.usgs.cida.derivative;

import com.google.common.primitives.Floats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.DataType;
import ucar.nc2.units.SimpleUnit;

/**
 *
 * @author tkunicki
 */
public class DaysAboveTemperatureThresholdVisitor extends DaysAboveThresholdVisitor {
    
    private final static Logger LOGGER = LoggerFactory.getLogger(DaysAboveTemperatureThresholdVisitor.class);
    
    private DerivativeValueDescriptor valueDescriptor;
    
    public DaysAboveTemperatureThresholdVisitor() {
        valueDescriptor = new DerivativeValueDescriptor(
                "threshold", // name
                "air_temperature", // standard_name
                SimpleUnit.factory("degF"),
                DataType.FLOAT,
                Floats.asList(new float[] { 90f, 95f, 100f } ),
                "days_above_threshold", // name
                "number_of_days_with_air_temperature_above_threshold", // standard name TODO: ???
                SimpleUnit.factory("days"), // units
                DataType.SHORT);
    }

    @Override
    public DerivativeValueDescriptor getValueDescriptor() {
        return valueDescriptor;
    }

}
