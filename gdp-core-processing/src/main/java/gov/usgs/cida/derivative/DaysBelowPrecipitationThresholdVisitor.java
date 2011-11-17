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
public class DaysBelowPrecipitationThresholdVisitor extends DaysAboveThresholdVisitor {
    
    private final static Logger LOGGER = LoggerFactory.getLogger(DaysBelowPrecipitationThresholdVisitor.class);
    
    private DerivativeValueDescriptor valueDescriptor;
    
    public DaysBelowPrecipitationThresholdVisitor() {
        valueDescriptor = new DerivativeValueDescriptor(
                "threshold", // name
                "lwe_thickness_of_precipitation_amount", // standard_name
                SimpleUnit.factory("inches"),
                DataType.FLOAT,
                Floats.asList(new float[] { 1f, 2f, 3f, 4f } ),
                "days_below_threshold", // name
                "number_of_days_with_lwe_thickness_of_precipitation_amount_below_threshold", // standard name TODO: ???
                SimpleUnit.factory("days"), // units
                DataType.SHORT);
    }

    @Override
    public DerivativeValueDescriptor getValueDescriptor() {
        return valueDescriptor;
    }

}
