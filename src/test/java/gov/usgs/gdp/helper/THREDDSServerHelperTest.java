package gov.usgs.gdp.helper;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.ConnectException;
import java.net.UnknownHostException;

import org.junit.Test;

public class THREDDSServerHelperTest {
    @Test
    public void testIsServerReachable() {
        String workingHost = "www.google.com";
        int workingPort = 80;
        int timeout = 5000;

        boolean result = false;
        try {
            result = THREDDSServerHelper.isServerReachable(workingHost, workingPort, timeout);
        } catch (UnknownHostException e) {
            fail(e.getMessage());
        } catch (IOException e) {
            fail(e.getMessage());
        }
        assertTrue(result);

        int nonWorkingPort = 64738;
        try {
            result = THREDDSServerHelper.isServerReachable(workingHost, nonWorkingPort, timeout);
        } catch (UnknownHostException e) {
            fail(e.getMessage());
        } catch (IOException e) {
            assertTrue(e instanceof ConnectException);
        }

        String nonWorkingHost = "www.ivan-suftin-rocks.com";
        try {
            result = THREDDSServerHelper.isServerReachable(nonWorkingHost, workingPort, timeout);
        } catch (UnknownHostException e) {
            assertNotNull(e);
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }
}
