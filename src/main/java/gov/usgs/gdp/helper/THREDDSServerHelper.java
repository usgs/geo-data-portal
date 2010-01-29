package gov.usgs.gdp.helper;

import gov.usgs.gdp.analysis.NetCDFUtility;
import gov.usgs.gdp.bean.DataSetBean;
import gov.usgs.gdp.bean.XmlBean;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.util.ArrayList;
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
    public static List<InvAccess> getInvAccessListFromServer(String hostname, int port, String uri) {
    	List<InvAccess> result = new LinkedList<InvAccess>();
    	String ThreddsURL = "http://" + hostname + ":" + port + uri;
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

    /**
     * Returns a List<XmlBean> which is actually a List<DataSetBean>
     * 
     * @param hostname
     * @param port
     * @param uri
     * @return
     * @throws IllegalArgumentException
     */
	public static List<XmlBean> getDatasetListFromServer(String hostname,
			int port, String uri) throws IllegalArgumentException {
		
		if (hostname == null || "".equals(hostname)) throw new IllegalArgumentException("Hostname invalid or null");
		if (port == 0) port = 80;
		
		List<XmlBean> result = new ArrayList<XmlBean>();
		List<InvAccess> invAccessList = THREDDSServerHelper.getInvAccessListFromServer(hostname, port, uri);
		
		if (invAccessList == null) return result;
		for (InvAccess invAccess : invAccessList) {
			
			InvDataset ds = invAccess.getDataset();
			DataSetBean dsb = new DataSetBean(ds);			
			result.add(dsb);
		}
		
		
		return result;
	}
}
