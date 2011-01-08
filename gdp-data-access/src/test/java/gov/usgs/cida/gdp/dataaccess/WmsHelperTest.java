/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.usgs.cida.gdp.dataaccess;

import java.io.IOException;
import java.net.UnknownHostException;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 *
 * @author isuftin
 */
public class WmsHelperTest {

    @Test
    public void createWmsHelperTest() throws Exception {
        WmsHelper result = new WmsHelper();
        assertThat(result, is(notNullValue()));
    }

    @Test
    public void getCapabilitiesWithBogusURL() throws Exception {
        String result = null;
        try {
            result = WmsHelper.getCapabilities("http://bogus.url.gov");
        } catch (IOException ex) {
            assertThat(ex, is(instanceOf(UnknownHostException.class)));
        }
        assertThat(result, is(nullValue()));
    }

}