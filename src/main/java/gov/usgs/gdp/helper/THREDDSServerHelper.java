package gov.usgs.gdp.helper;

import gov.usgs.gdp.analysis.NetCDFUtility;
import gov.usgs.gdp.bean.GridBean;
import gov.usgs.gdp.bean.TimeBean;
import gov.usgs.gdp.bean.XmlBean;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.LinkedList;
import java.util.List;

import thredds.catalog.*;
import ucar.nc2.VariableSimpleIF;
import ucar.nc2.ft.FeatureDataset;
import ucar.nc2.ft.FeatureDatasetFactoryManager;

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
    

    public static TimeBean getTimeBean(String datasetUrl, String gridSelection) throws IOException{
    	Formatter errorLog = new Formatter();
    	FeatureDataset featureDataset = null;
    	featureDataset = 
    		FeatureDatasetFactoryManager.open(null, datasetUrl, null, errorLog);

    	if (featureDataset != null) {
    		try {
    			
    			TimeBean timeBean = TimeBean.getTimeBean(datasetUrl, gridSelection);
    			if (timeBean != null && !timeBean.getTime().isEmpty()) return timeBean;
    			return null;
    		} finally {
    			featureDataset.close();
    		}
    	}
    	return null;
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

    public static InvCatalog getInvCatalogFromServer(String hostname, int port, String uri) {
    	String ThreddsURL = "http://" + hostname + ":" + port + uri;
    	URI catalogURI = URI.create(ThreddsURL);
    	InvCatalogFactory factory = new InvCatalogFactory("default", true);
    	InvCatalog catalog = factory.readXML(catalogURI);
    	return catalog;
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
	/*public static List<XmlBean> getDatasetListFromServer(String hostname,
			int port, String uri) throws IllegalArgumentException {
		
		if (hostname == null || "".equals(hostname)) throw new IllegalArgumentException("Hostname invalid or null");
		if (uri == null || "".equals(uri)) throw new IllegalArgumentException("URI invalid or null");
		if (port == 0) port = 80;
		
		List<XmlBean> result = new ArrayList<XmlBean>();
		List<InvAccess> invAccessList = THREDDSServerHelper.getInvAccessListFromServer(hostname, port, uri);
		
		if (invAccessList == null) return result;
		for (InvAccess invAccess : invAccessList) {
			
			InvDataset ds = invAccess.getDataset();
			List<InvPropertyBean> = ds.getP 
			DataSetBean dsb = new DataSetBean(ds);			
			result.add(dsb);
		}
		
		
		return result;
	}*/
	
	public static List<XmlBean> getGridBeanListFromServer(String datasetUrl) throws IllegalArgumentException, IOException{
		
		if (datasetUrl == null || "".equals(datasetUrl)) throw new IllegalArgumentException("DataSet URL invalid or null");
		
		List<XmlBean> result = new ArrayList<XmlBean>();
        Formatter errorLog = new Formatter();
        FeatureDataset featureDataset = null;
		try {
			featureDataset = FeatureDatasetFactoryManager.open(null, datasetUrl, null, errorLog);

			 if (featureDataset != null) {	  
				 List<VariableSimpleIF> vsifList = featureDataset.getDataVariables();
	            for (VariableSimpleIF vs : vsifList) {
	            	GridBean gb = new GridBean(vs);
	            	result.add(gb);
	            }
	        } else {
	        	return null;
	        }
			 
		} catch (IOException e) {
		
		
			throw e;
		} finally {
			if (featureDataset != null) {
				featureDataset.close();
			}
			
		}
		return result;
		
	}
	
}
