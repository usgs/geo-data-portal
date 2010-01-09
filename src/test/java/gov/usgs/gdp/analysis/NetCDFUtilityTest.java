package gov.usgs.gdp.analysis;

import static org.junit.Assert.*;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import thredds.catalog.InvAccess;
import thredds.catalog.InvCatalog;
import thredds.catalog.InvCatalogFactory;
import thredds.catalog.InvDataset;


public class NetCDFUtilityTest {
	@Test
	public void testCreateNetCDFUtilityClass() {
		NetCDFUtility result = new NetCDFUtility();
		assertNotNull(result);
	}
	
	@Test 
	public void testGetOpendapResourcesAux() {
		URI catalogURI = URI.create("http://runoff.cr.usgs.gov:8086/thredds/hydrologic_catalog.xml");
		InvCatalogFactory factory = new InvCatalogFactory("default", true);
        InvCatalog catalog = factory.readXML(catalogURI);
        StringBuilder buff = new StringBuilder();
        assertTrue(buff.toString(),catalog.check(buff));
        
        assertNull(NetCDFUtility.getOpendapResourcesAux(null));
        
        for (InvDataset dataset : catalog.getDatasets()) {
            assertFalse(NetCDFUtility.getOpendapResourcesAux(dataset).isEmpty());
        }
	}
	
	@Test
	public void testGetOpenDapResources() {
		URI catalogURI = URI.create("http://runoff.cr.usgs.gov:8086/thredds/hydrologic_catalog.xml");
		InvCatalogFactory factory = new InvCatalogFactory("default", true);
        InvCatalog catalog = factory.readXML(catalogURI);
        StringBuilder buff = new StringBuilder();
        assertTrue(buff.toString(),catalog.check(buff));
        
        assertNull(NetCDFUtility.getOpenDapResources(null));
        
		List<InvAccess>  result = NetCDFUtility.getOpenDapResources(catalog);
		assertNotNull(result);
	}
	
	
}
