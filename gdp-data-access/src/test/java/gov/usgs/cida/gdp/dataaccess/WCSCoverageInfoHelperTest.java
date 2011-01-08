/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usgs.cida.gdp.dataaccess;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import java.net.UnknownHostException;
import org.hamcrest.core.IsNull;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 *
 * @author isuftin
 */
public class WCSCoverageInfoHelperTest {

    public WCSCoverageInfoHelperTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void createWCSCoerageInfoHelper() {
        WCSCoverageInfoHelper result = new WCSCoverageInfoHelper();
        assertThat(result, is(instanceOf(WCSCoverageInfoHelper.class)));
    }

    @Test
    public void getWcsDescribeCoveragesWithBogusURLTest() {
        String result = null;
        try {
            result = WCSCoverageInfoHelper.getWcsDescribeCoverages("http://bogus.url.gov");
        } catch (IOException ex) {
            assertThat(ex, is(instanceOf(UnknownHostException.class)));
        }
        assertThat(result, is(nullValue()));
    }
}
