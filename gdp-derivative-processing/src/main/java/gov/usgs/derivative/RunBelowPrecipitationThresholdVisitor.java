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
public class RunBelowPrecipitationThresholdVisitor extends RunBelowThresholdVisitor {

    private final static Logger LOGGER = LoggerFactory.getLogger(RunBelowPrecipitationThresholdVisitor.class);

    @Override
    protected DerivativeValueDescriptor generateDerivativeValueDescriptor(List<GridDatatype> gridDatatypeList) {
        return new DerivativeValueDescriptor(
                "threshold", // name
                "lwe_thickness_of_precipitation_amount", // standard_name
                "mm",
                DataType.FLOAT,
                Floats.asList(new float[] { 3f } ),
                generateDerivativeOutputVariableName(gridDatatypeList, "spell_length_below_threshold"), // name
                "spell_length_of_days_with_lwe_thickness_of_precipitation_amount_below_threshold", // standard name
                "days", // units
                Short.valueOf((short)-1),
                DataType.SHORT);
    }
    
}
