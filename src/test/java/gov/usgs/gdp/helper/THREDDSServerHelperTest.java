package gov.usgs.gdp.helper;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.util.List;

import org.junit.Test;

import thredds.catalog.InvAccess;

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
    
    @Test
    public void testGetInvAccessListFromServer() {
    	String host = "runoff.cr.usgs.gov";
    	int port = 8086; 
    	String uri = "/thredds/hydrologic_catalog.xml";
    	List<InvAccess> result = THREDDSServerHelper.getInvAccessListFromServer(host, port, uri);
    	assertNotNull(result);
    	assertFalse(result.isEmpty());
    	
    }
}
