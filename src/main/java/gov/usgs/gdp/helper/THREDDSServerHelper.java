package gov.usgs.gdp.helper;

import gov.usgs.gdp.analysis.NetCDFUtility;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import thredds.catalog.*;

public class THREDDSServerHelper {

    /**
     * Tests whether or not a THREDDS server is reachable
     *
     * @param host
     * @param port
     * @param timeout - milliseconds
     * @return
     * @throws IOException
     */
    public static boolean isServerReachable(final String host, final int port, final int timeout) throws IOException {
        boolean result = false;

        Socket testSocket = new Socket();
        InetSocketAddress address = new InetSocketAddress(host, port);
        testSocket.connect(address, timeout);
        result = testSocket.isConnected();
        if (result) {
            testSocket.close();
        }

        return result;
    }
    
    /**
     * Returns a List of type InvAccess which is a set of resources a THREDDS server offers
     * 
     * @param hostname
     * @param port
     * @return
     */
    public static List<InvAccess> getInvAccessListFromServer(String hostname, int port) {
    	List<InvAccess> result = new LinkedList<InvAccess>();
    	String ThreddsURL = "http://" + hostname + ":" + port;
    	URI catalogURI = URI.create(ThreddsURL);
    	InvCatalogFactory factory = new InvCatalogFactory("default", true);
    	InvCatalog catalog = factory.readXML(catalogURI);
    	
    	StringBuilder buff = new StringBuilder();
    	if (!catalog.check(buff)) {
            return result;
        }
    	
    	// Grab resources from the THREDDS catalog
    	result = NetCDFUtility.getOpenDapResources(catalog);
        if (result == null) {
            return new LinkedList<InvAccess>();
        }
    	
    	return result;
    	
    }
}
