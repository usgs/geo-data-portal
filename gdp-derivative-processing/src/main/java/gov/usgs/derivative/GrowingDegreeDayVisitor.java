package gov.usgs.derivative;

import com.google.common.primitives.Floats;
import java.util.List;
import ucar.ma2.DataType;
import ucar.nc2.dt.GridDatatype;

/**
 *
 * @author tkunicki
 */
public class GrowingDegreeDayVisitor extends AnnualDegreeDayVisitor {
    
    public GrowingDegreeDayVisitor(String outputDir) {
        super(outputDir);
    }

    @Override
    protected AnnualDerivativeKernel createKernel(int yxCount, float[] thresholds) {
        return new GrowingDegreeDayKernel(yxCount, thresholds);
    }

    @Override
    protected DerivativeValueDescriptor generateDerivativeValueDescriptor(List<GridDatatype> gridDatatypeList) {
        return new DerivativeValueDescriptor(
                "threshold", // name
                "air_temperature", // standard_name
                "degF",
                DataType.FLOAT,
                Floats.asList(new float[] { 60f } ),
                generateDerivativeOutputVariableName(gridDatatypeList, "growing_degree_days"), // name
                "growing_degree_days", // standard name TODO: ???
                "degree*day", // units
                Float.valueOf(-1f),
                DataType.FLOAT);
    }
    
    protected class GrowingDegreeDayKernel extends AnnualDegreeDayKernel {
        
        public GrowingDegreeDayKernel(int yxCount, float[] thresholds) {
            super(yxCount, thresholds);
        }

        @Override
        public float k_degreeDays(float threshold, float tempMin, float tempMax) {
            float tempMinClamped = max(tempMin, threshold);  // should be separate threshold
            float tempMaxClamped = max(tempMax, threshold);  // should be separate threshold with min clamp
            float tempAve = (tempMinClamped + tempMaxClamped) / 2f;
            return max(0, tempAve - threshold);
        }
    }
    
}
