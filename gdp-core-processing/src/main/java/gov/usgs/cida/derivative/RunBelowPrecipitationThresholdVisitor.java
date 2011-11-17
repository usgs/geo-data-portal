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
public class RunBelowPrecipitationThresholdVisitor extends RunBelowThresholdVisitor {

    private final static Logger LOGGER = LoggerFactory.getLogger(RunBelowPrecipitationThresholdVisitor.class);
    
    private DerivativeValueDescriptor valueDescriptor;
    
    public RunBelowPrecipitationThresholdVisitor() {
        valueDescriptor = new DerivativeValueDescriptor(
                "threshold", // name
                "lwe_thickness_of_precipitation_amount", // standard_name
                SimpleUnit.factory("mm"),
                DataType.FLOAT,
                Floats.asList(new float[] { 3f } ),
                "spell_length_below_threshold", // name
                "spell_length_of_days_with_lwe_thickness_of_precipitation_amount_below_threshold", // standard name
                SimpleUnit.factory("days"), // units
                DataType.SHORT);
    }

    @Override
    public DerivativeValueDescriptor getValueDescriptor() {
        return valueDescriptor;
    }
    
}
