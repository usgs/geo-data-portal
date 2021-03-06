package gov.usgs.derivative;

import com.google.common.primitives.Floats;
import java.util.List;
import ucar.ma2.DataType;
import ucar.nc2.dt.GridDatatype;

/**
 *
 * @author tkunicki
 */
public class HeatingDegreeDayVisitor extends AnnualDegreeDayVisitor {

    public HeatingDegreeDayVisitor(String outputDir) {
        super(outputDir);
    }
    
    @Override
    protected AnnualDerivativeKernel createKernel(int yxCount, float[] thresholds) {
        return new HeatingDegreeDayKernel(yxCount, thresholds);
    }

    @Override
    protected DerivativeValueDescriptor generateDerivativeValueDescriptor(List<GridDatatype> gridDatatypeList) {
        return new DerivativeValueDescriptor(
                "threshold", // name
                "air_temperature", // standard_name
                "degF",
                DataType.FLOAT,
                Floats.asList(new float[] { 65f } ),
                generateDerivativeOutputVariableName(gridDatatypeList, "heating_degree_days"), // name
                "heating_degree_days", // standard name TODO: ???
                "degree*day", // units
                Float.valueOf(-1f),
                DataType.FLOAT);
    }

    protected class HeatingDegreeDayKernel extends AnnualDegreeDayKernel {
        
        public HeatingDegreeDayKernel(int yxCount, float[] thresholds) {
            super(yxCount, thresholds);
        }

        @Override
        public float k_degreeDays(float threshold, float tempMin, float tempMax) {
            float tempAve = (tempMin + tempMax) / 2f;
            return max(0, threshold - tempAve);
        }
    }
    
}
