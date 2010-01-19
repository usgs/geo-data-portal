package gov.usgs.gdp.analysis;

import static org.junit.Assert.*;

import gov.usgs.gdp.helper.THREDDSServerHelper;

import java.io.IOException;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Test;

import thredds.catalog.InvAccess;
import thredds.catalog.InvCatalog;
import thredds.catalog.InvCatalogFactory;
import thredds.catalog.InvDataset;


public class NetCDFUtilityTest {
	private static org.apache.log4j.Logger log = Logger.getLogger(NetCDFUtilityTest.class);

	@Test
	public void testCreateNetCDFUtilityClass() {
		NetCDFUtility result = new NetCDFUtility();
		assertNotNull(result);
	}
	
	@Test 
	public void testGetOpendapResourcesAux() {
		boolean isServerReachable = false;
		try {
			isServerReachable = THREDDSServerHelper.isServerReachable("runoff.cr.usgs.gov", 8086, 5000);
		} catch (UnknownHostException e) {
			isServerReachable = false;
		} catch (IOException e) {
			isServerReachable = false;
		}
		
		if (isServerReachable) {
			URI catalogURI = URI.create("http://runoff.cr.usgs.gov:8086/thredds/hydrologic_catalog.xml");
			InvCatalogFactory factory = new InvCatalogFactory("default", true);
	        InvCatalog catalog = factory.readXML(catalogURI);
	        StringBuilder buff = new StringBuilder();
	        assertTrue(buff.toString(),catalog.check(buff));
	        
	        assertNull(NetCDFUtility.getOpendapResourcesAux(null));
	        
	        for (InvDataset dataset : catalog.getDatasets()) {
	            assertFalse(NetCDFUtility.getOpendapResourcesAux(dataset).isEmpty());
	        }
		} else {
			log.debug("runoff.cr.usgs.gov:8086 is unreachable. Skipping this unit test.");
		}
	}
	
	@Test
	public void testGetOpenDapResources() {
		boolean isServerReachable = false;
		try {
			isServerReachable = THREDDSServerHelper.isServerReachable("runoff.cr.usgs.gov", 8086, 5000);
		} catch (UnknownHostException e) {
			isServerReachable = false;
		} catch (IOException e) {
			isServerReachable = false;
		}
		
		if (isServerReachable) {
		URI catalogURI = URI.create("http://runoff.cr.usgs.gov:8086/thredds/hydrologic_catalog.xml");
		InvCatalogFactory factory = new InvCatalogFactory("default", true);
        InvCatalog catalog = factory.readXML(catalogURI);
        StringBuilder buff = new StringBuilder();
        assertTrue(buff.toString(),catalog.check(buff));
        
        assertNull(NetCDFUtility.getOpenDapResources(null));
        
		List<InvAccess>  result = NetCDFUtility.getOpenDapResources(catalog);
		assertNotNull(result);
		} else {
			log.debug("runoff.cr.usgs.gov:8086 is unreachable. Skipping this unit test.");
		}
	}
	
/*	@Test
	public void testOpenPointObsDataSet() {
		PointObsDatatype result = null;
		try {
			result = NetCDFUtility.openPointObsDataSet("http://130.11.161.219:8080/thredds/catalog/models/catalog.xml",  new Formatter());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertNotNull(result);
	}*/
}
