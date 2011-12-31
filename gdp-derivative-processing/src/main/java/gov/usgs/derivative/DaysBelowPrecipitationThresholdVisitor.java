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
public class DaysBelowPrecipitationThresholdVisitor extends DaysAboveThresholdVisitor {
    
    private final static Logger LOGGER = LoggerFactory.getLogger(DaysBelowPrecipitationThresholdVisitor.class);
    
    @Override
    public DerivativeValueDescriptor generateDerivativeValueDescriptor(List<GridDatatype> gridDataTypeList) {
        return new DerivativeValueDescriptor(
                "threshold", // name
                "lwe_thickness_of_precipitation_amount", // standard_name
                "inches",
                DataType.FLOAT,
                Floats.asList(new float[] { 1f, 2f, 3f, 4f } ),
                generateDerivativeOutputVariableName(gridDataTypeList, "days_below_threshold"), // name
                "number_of_days_with_lwe_thickness_of_precipitation_amount_below_threshold", // standard name TODO: ???
                "days", // units
                Short.valueOf((short)-1),
                DataType.SHORT);
    }

}
