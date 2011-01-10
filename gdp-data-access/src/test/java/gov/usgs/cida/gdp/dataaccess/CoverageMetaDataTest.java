package gov.usgs.cida.gdp.dataaccess;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

import static gov.usgs.cida.gdp.dataaccess.CoverageMetaData.*;
/**
 *
 * @author isuftin
 */
public class CoverageMetaDataTest {

    @Test
    public void createCoverageMetaData() {
        CoverageMetaData cmd = new CoverageMetaData();
        assertThat(cmd, notNullValue());
    }

    @Test
    public void unknownDataTypeTest() {
        assertThat(UnknownDataType.getSizeBytes(), is(equalTo(-1)));
        assertThat(UnknownDataType.isIntegerType(), is(equalTo(false)));
        assertThat(UnknownDataType.isFloatingPointType(), is(equalTo(false)));
    }

    @Test
    public void primitiveDataTypeTest() {
        assertThat(PrimitiveDataType.BYTE.getSizeBytes(), is(equalTo(1)));
        assertThat(PrimitiveDataType.BYTE.isIntegerType(), is(equalTo(true)));
        assertThat(PrimitiveDataType.BYTE.isFloatingPointType(), is(equalTo(false)));

        assertThat(PrimitiveDataType.SHORT.getSizeBytes(), is(equalTo(2)));
        assertThat(PrimitiveDataType.SHORT.isIntegerType(), is(equalTo(true)));
        assertThat(PrimitiveDataType.SHORT.isFloatingPointType(), is(equalTo(false)));

        assertThat(PrimitiveDataType.INT.getSizeBytes(), is(equalTo(4)));
        assertThat(PrimitiveDataType.INT.isIntegerType(), is(equalTo(true)));
        assertThat(PrimitiveDataType.INT.isFloatingPointType(), is(equalTo(false)));

        assertThat(PrimitiveDataType.LONG.getSizeBytes(), is(equalTo(8)));
        assertThat(PrimitiveDataType.LONG.isIntegerType(), is(equalTo(true)));
        assertThat(PrimitiveDataType.LONG.isFloatingPointType(), is(equalTo(false)));

        assertThat(PrimitiveDataType.FLOAT.getSizeBytes(), is(equalTo(4)));
        assertThat(PrimitiveDataType.FLOAT.isIntegerType(), is(equalTo(false)));
        assertThat(PrimitiveDataType.FLOAT.isFloatingPointType(), is(equalTo(true)));

        assertThat(PrimitiveDataType.DOUBLE.getSizeBytes(), is(equalTo(8)));
        assertThat(PrimitiveDataType.DOUBLE.isIntegerType(), is(equalTo(false)));
        assertThat(PrimitiveDataType.DOUBLE.isFloatingPointType(), is(equalTo(true)));
    }

    @Test
    public void findCoverageDataTypeTest() {
        assertThat(findCoverageDataType("int").toString(), is(equalTo("INT")));
        assertThat(findCoverageDataType("byte").toString(), is(equalTo("BYTE")));
        assertThat(findCoverageDataType("short").toString(), is(equalTo("SHORT")));
        assertThat(findCoverageDataType("long").toString(), is(equalTo("LONG")));
        assertThat(findCoverageDataType("float").toString(), is(equalTo("FLOAT")));
        assertThat(findCoverageDataType("double").toString(), is(equalTo("DOUBLE")));

        assertThat(findCoverageDataType("INT1").toString(), is(equalTo("BYTE")));
        assertThat(findCoverageDataType("INT2").toString(), is(equalTo("SHORT")));
        assertThat(findCoverageDataType("INT4").toString(), is(equalTo("INT")));
        assertThat(findCoverageDataType("INT8").toString(), is(equalTo("LONG")));
        assertThat(findCoverageDataType("INT16").toString(), is(equalTo("SHORT")));
        assertThat(findCoverageDataType("INT32").toString(), is(equalTo("INT")));
        assertThat(findCoverageDataType("INT64").toString(), is(equalTo("LONG")));

        assertThat(findCoverageDataType("INTEGER").toString(), is(equalTo("INT")));

        assertThat(findCoverageDataType("FLOAT4").toString(), is(equalTo("FLOAT")));
        assertThat(findCoverageDataType("FLOAT8").toString(), is(equalTo("DOUBLE")));
        assertThat(findCoverageDataType("FLOAT32").toString(), is(equalTo("FLOAT")));
        assertThat(findCoverageDataType("FLOAT64").toString(), is(equalTo("DOUBLE")));
        
        assertThat(findCoverageDataType("UNUSED"), is(instanceOf(UnknownDataType.getClass())));

    }


}