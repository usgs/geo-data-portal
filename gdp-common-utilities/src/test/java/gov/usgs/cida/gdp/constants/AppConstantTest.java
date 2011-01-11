package gov.usgs.cida.gdp.constants;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author isuftin
 */
public class AppConstantTest {

    @Test
    public void testDefaultsExist() {
        AppConstant[] constants = AppConstant.values();
        for (AppConstant constant : constants) {
            assertNotNull(constant);
            assertFalse(constant.name() + " is empty", constant.toString().isEmpty());
        }
    }

}