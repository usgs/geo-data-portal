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
public class RunAboveTemperatureThresholdVisitor extends RunAboveThresholdVisitor {

    private final static Logger LOGGER = LoggerFactory.getLogger(RunAboveTemperatureThresholdVisitor.class);
    
    private DerivativeValueDescriptor valueDescriptor;
    
    public RunAboveTemperatureThresholdVisitor() {
        valueDescriptor = new DerivativeValueDescriptor(
                "threshold", // name
                "air_temperature", // standard_name
                SimpleUnit.factory("degF"),
                DataType.FLOAT,
                Floats.asList(new float[] { 90f, 95f, 100f } ),
                "spell_length_above_threshold", // name
                "spell_length_of_days_with_air_temperature_above_threshold", // standard name TODO: ???
                SimpleUnit.factory("days"), // units
                DataType.SHORT);
    }

    @Override
    public DerivativeValueDescriptor getValueDescriptor() {
        return valueDescriptor;
    }
    
}
