package ucar.nc2.iosp.geotiff.epsg.csv;

import java.util.HashMap;
import java.util.Map;
import javax.measure.converter.UnitConverter;
import javax.measure.quantity.Angle;
import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

/**
 *
 * @author tkunicki
 */
public class UnitOfMeasureUtil {
    
    private final static Map<Integer, Unit<?>> converted;
    
    static {
         converted = new HashMap<Integer, Unit<?>>();
         converted.put(9107, NonSI.DEGREE_ANGLE.transform(new ToSexagesimalConverter(1)).asType(Angle.class));
         converted.put(9110, NonSI.DEGREE_ANGLE.transform(new ToSexagesimalConverter(10000)).asType(Angle.class));
    }
    
    public synchronized static Unit<?> convert(UnitOfMeasureEntry entry) {
        int code = entry.getCode();
        Unit<?> unit = converted.get(code);
        if (unit == null) {
            int targetCode = entry.getTargetUnitOfMeasureCode();
            Unit<?> targetUnit = convertByCode(targetCode);
            if (code == targetCode) {
                unit = targetUnit;
            } else {
                double factorB = entry.getFactorB();
                double factorC = entry.getFactorC();
                if (factorB == factorB && factorC == factorC) {
                    unit = targetUnit.times(factorB).divide(factorC);
                } else {
                    // TODO: figure if it's possible (and needed) to support 9108,9111 and 9115-9121.  Some of these may (will?)
                    // look to have non-real number representations so maybe we won't ever need to support them (i.e. how would
                    // the coords be represented in a GeoTIFF?).  If new ones are added throw them into the map with created int
                    // the static constructior...
                    throw new UnsupportedOperationException("Unable to generate unit representation for requested EPSG UoM");
                }
            }
            converted.put(code, unit);
        }
        return unit;
    }
    
    private static Unit<?> convertByCode(int code) {
        switch (code) {
            case 9001:
                return SI.METER;
            case 9101:
                return SI.RADIAN;
            case 9102:
                return NonSI.DEGREE_ANGLE;
            case 9201:
                return Unit.ONE;
            default: throw new IllegalStateException("Unable to find target UoM");
        }
    }
    
    private static class FromSexagesimalConverter extends UnitConverter {
        
        private final int divisor;
        private UnitConverter inverse;

        private FromSexagesimalConverter(int divisor) {
            this.divisor = divisor;
        }

        @Override
        public synchronized UnitConverter inverse() {
            if (inverse == null) {
                inverse = new ToSexagesimalConverter(divisor);
            }
            return inverse;
        }

        @Override
        public double convert(double value) {
            final int deg, min, sec;
            deg = (int) value; // Round toward 0
            value = (value - deg) * 60;
            min = (int) value; // Round toward 0
            value = (value - min) * 60;
            sec = (int) value; // Round toward 0
            value -= sec;      // The remainer (fraction of seconds)
            return (((deg * 100 + min) * 100 + sec) + value) / divisor;
        }

        public final double derivative(double x) {
            return 1;
        }

        @Override
        public boolean isLinear() {
            return true;
        }
    }

    private static final class ToSexagesimalConverter extends UnitConverter {

        private static final double EPS = 1E-8;
        
        private final int multiplier;
        
        private  UnitConverter inverse;
        
        public ToSexagesimalConverter(int multiplier) {
            this.multiplier = multiplier;
        }

        /**
         * Performs a conversion from sexagesimal degrees to fractional degrees.
         */
        @Override
        public double convert(double value) {
            value *= this.multiplier;
            int deg, min;
            deg = (int) (value / 10000);
            value -= 10000 * deg;
            min = (int) (value / 100);
            value -= 100 * min;
            if (min <= -60 || min >= 60) {  // Accepts NaN
                if (Math.abs(Math.abs(min) - 100) <= EPS) {
                    if (min >= 0) {
                        deg++;
                    } else {
                        deg--;
                    }
                    min = 0;
                } else {
                    throw new ArithmeticException("Invalid minutes: " + min);
                }
            }
            if (value <= -60 || value >= 60) { // Accepts NaN
                if (Math.abs(Math.abs(value) - 100) <= EPS) {
                    if (value >= 0) {
                        min++;
                    } else {
                        min--;
                    }
                    value = 0;
                } else {
                    throw new ArithmeticException("Invalid seconds: " + value);
                }
            }
            value = ((value / 60) + min) / 60 + deg;
            return value;
        }

        @Override
        public synchronized UnitConverter inverse() {
            if (inverse == null) {
                inverse = new FromSexagesimalConverter(multiplier);
            }
            return inverse;
        }

        @Override
        public boolean isLinear() {
            return true;
        }
    }

}
