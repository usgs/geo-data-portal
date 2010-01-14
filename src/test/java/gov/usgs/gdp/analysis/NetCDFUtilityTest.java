package gov.usgs.gdp.analysis;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URI;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.List;

import org.junit.Test;

import thredds.catalog.InvAccess;
import thredds.catalog.InvCatalog;
import thredds.catalog.InvCatalogFactory;
import thredds.catalog.InvDataset;
import ucar.nc2.dt.PointObsDatatype;


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
