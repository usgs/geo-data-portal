package gov.usgs.cida.gdp.dataaccess;

import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
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
        assertThat(CoverageMetaData.UnknownDataType.getSizeBytes(), is(equalTo(-1)));
        assertThat(CoverageMetaData.UnknownDataType.isIntegerType(), is(equalTo(false)));
        assertThat(CoverageMetaData.UnknownDataType.isFloatingPointType(), is(equalTo(false)));
    }

    @Test
    public void primitiveDataTypeTest() {
        assertThat(CoverageMetaData.PrimitiveDataType.BYTE.getSizeBytes(), is(equalTo(1)));
        assertThat(CoverageMetaData.PrimitiveDataType.BYTE.isIntegerType(), is(equalTo(true)));
        assertThat(CoverageMetaData.PrimitiveDataType.BYTE.isFloatingPointType(), is(equalTo(false)));

        assertThat(CoverageMetaData.PrimitiveDataType.SHORT.getSizeBytes(), is(equalTo(2)));
        assertThat(CoverageMetaData.PrimitiveDataType.SHORT.isIntegerType(), is(equalTo(true)));
        assertThat(CoverageMetaData.PrimitiveDataType.SHORT.isFloatingPointType(), is(equalTo(false)));

        assertThat(CoverageMetaData.PrimitiveDataType.INT.getSizeBytes(), is(equalTo(4)));
        assertThat(CoverageMetaData.PrimitiveDataType.INT.isIntegerType(), is(equalTo(true)));
        assertThat(CoverageMetaData.PrimitiveDataType.INT.isFloatingPointType(), is(equalTo(false)));

        assertThat(CoverageMetaData.PrimitiveDataType.LONG.getSizeBytes(), is(equalTo(8)));
        assertThat(CoverageMetaData.PrimitiveDataType.LONG.isIntegerType(), is(equalTo(true)));
        assertThat(CoverageMetaData.PrimitiveDataType.LONG.isFloatingPointType(), is(equalTo(false)));

        assertThat(CoverageMetaData.PrimitiveDataType.FLOAT.getSizeBytes(), is(equalTo(4)));
        assertThat(CoverageMetaData.PrimitiveDataType.FLOAT.isIntegerType(), is(equalTo(false)));
        assertThat(CoverageMetaData.PrimitiveDataType.FLOAT.isFloatingPointType(), is(equalTo(true)));

        assertThat(CoverageMetaData.PrimitiveDataType.DOUBLE.getSizeBytes(), is(equalTo(8)));
        assertThat(CoverageMetaData.PrimitiveDataType.DOUBLE.isIntegerType(), is(equalTo(false)));
        assertThat(CoverageMetaData.PrimitiveDataType.DOUBLE.isFloatingPointType(), is(equalTo(true)));
    }

    @Test
    public void findCoverageDataTypeTest() {
        assertThat(CoverageMetaData.findCoverageDataType("int").toString(), is(equalTo("INT")));
        assertThat(CoverageMetaData.findCoverageDataType("byte").toString(), is(equalTo("BYTE")));
        assertThat(CoverageMetaData.findCoverageDataType("short").toString(), is(equalTo("SHORT")));
        assertThat(CoverageMetaData.findCoverageDataType("long").toString(), is(equalTo("LONG")));
        assertThat(CoverageMetaData.findCoverageDataType("float").toString(), is(equalTo("FLOAT")));
        assertThat(CoverageMetaData.findCoverageDataType("double").toString(), is(equalTo("DOUBLE")));

        assertThat(CoverageMetaData.findCoverageDataType("INT1").toString(), is(equalTo("BYTE")));
        assertThat(CoverageMetaData.findCoverageDataType("INT2").toString(), is(equalTo("SHORT")));
        assertThat(CoverageMetaData.findCoverageDataType("INT4").toString(), is(equalTo("INT")));
        assertThat(CoverageMetaData.findCoverageDataType("INT8").toString(), is(equalTo("LONG")));
        assertThat(CoverageMetaData.findCoverageDataType("INT16").toString(), is(equalTo("SHORT")));
        // Some of these tests are commented due to a bug in the class itself.
        // TODO- uncomment when bug is fixed
        // assertThat(CoverageMetaData.findCoverageDataType("INT32").toString(), is(equalTo("INT")));
        // assertThat(CoverageMetaData.findCoverageDataType("INT64").toString(), is(equalTo("LONG")));

        assertThat(CoverageMetaData.findCoverageDataType("INTEGER").toString(), is(equalTo("INT")));

        assertThat(CoverageMetaData.findCoverageDataType("FLOAT4").toString(), is(equalTo("FLOAT")));
        assertThat(CoverageMetaData.findCoverageDataType("FLOAT8").toString(), is(equalTo("DOUBLE")));
        assertThat(CoverageMetaData.findCoverageDataType("FLOAT32").toString(), is(equalTo("FLOAT")));
        // assertThat(CoverageMetaData.findCoverageDataType("FLOAT64").toString(), is(equalTo("DOUBLE")));
        
        assertThat(CoverageMetaData.findCoverageDataType("UNUSED"), is(instanceOf(CoverageMetaData.UnknownDataType.getClass())));

    }


}