/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.usgs.cida.gdp.dataaccess;

/**
 *
 * @author tkunicki
 */
public class CoverageMetaData {

    public interface DataType {
        public int getSizeBytes();
        public boolean isIntegerType();
        public boolean isFloatingPointType();
    }

    public final static DataType UnknownDataType = new DataType() {
        @Override public int getSizeBytes() { return -1;  }
        @Override public boolean isIntegerType() { return false; }
        @Override public boolean isFloatingPointType() { return false; }
    };

    public enum PrimitiveDataType implements DataType {

        BYTE(1, true),
        SHORT(2, true),
        INT(4, true),
        LONG(8, true),
        FLOAT(4, false),
        DOUBLE(8, false);

        private int sizeBytes;
        private boolean integerType;

        private PrimitiveDataType(int sizeBytes, boolean integerType) {
            this.sizeBytes = sizeBytes;
            this.integerType = integerType;
        }

        @Override
        public int getSizeBytes() {
            return sizeBytes;
        }

        @Override
        public boolean isIntegerType() {
            return integerType;
        }

        @Override
        public boolean isFloatingPointType() {
            return !integerType;
        }
    }

    public static DataType findCoverageDataType(String string) {
        string = string.toUpperCase();

        for (PrimitiveDataType dataType : PrimitiveDataType.values()) {
            if (string.equals(dataType.name().toUpperCase())) {
                return dataType;
            }
        }

        if (string.contains("INT")) {
            // Bytes
            if (string.endsWith("1")) {
                return PrimitiveDataType.BYTE;
            }
            if (string.endsWith("2")) {
                return PrimitiveDataType.SHORT;
            }
            if (string.endsWith("4")) {
                return PrimitiveDataType.INT;
            }
            // Byte or Bit?
            if (string.endsWith("8")) {
                return PrimitiveDataType.LONG;
//                return PrimitiveDataType.BYTE;
            }
            // Bits
            if (string.endsWith("16")) {
                return PrimitiveDataType.SHORT;
            }
            if (string.endsWith("32")) {
                return PrimitiveDataType.INT;
            }
            if (string.endsWith("64")) {
                return PrimitiveDataType.LONG;
            }
        }
        
        if (string.contains("INTEGER")) {
            return PrimitiveDataType.INT;
        }

        if (string.contains("FLOAT")) {
            // Bytes
            if (string.endsWith("4")) {
                return PrimitiveDataType.FLOAT;
            }
            if (string.endsWith("8")) {
                return PrimitiveDataType.DOUBLE;
            }
            // Bits
            if (string.endsWith("32")) {
                return PrimitiveDataType.FLOAT;
            }
            if (string.endsWith("64")) {
                return PrimitiveDataType.DOUBLE;
            }
        }

        return UnknownDataType;
    }
    
}
