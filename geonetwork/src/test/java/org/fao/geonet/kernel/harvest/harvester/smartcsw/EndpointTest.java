package org.fao.geonet.kernel.harvest.harvester.smartcsw;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import org.fao.geonet.kernel.harvest.harvester.smartcsw.Endpoint.EndpointType;

public class EndpointTest {

	@Test
	public void testDODS() {
		String url = "dods://testServer.usgs.gov/thredds/dodsC/dataset.nc";
		Endpoint ep = new Endpoint(url);
		assertEquals("Should have been seen as OPeNDAP Endpoint", ep.getType(), EndpointType.DODS);
	}

	@Test
	public void testDODS_http() {
		String url = "http://testServer.usgs.gov/thredds/dodsC/dataset.ncml";
		Endpoint ep = new Endpoint(url);
		assertEquals("dodsC indicates OPeNDAP endpoint", ep.getType(), EndpointType.DODS);
	}

	@Test
	public void testDODS_real() {
		String url = "http://igsarm-cida-javatest2.er.usgs.gov:8080/thredds/dodsC/qpe/rfcqpe_w_meta.ncml";
		Endpoint ep = new Endpoint(url);
		assertEquals("THREDDS OPeNDAP endpoint should be recognized", ep.getType(), EndpointType.DODS);
	}

	@Test
	public void testWCS() {
		String url = "http://testserver.usgs.gov/wcs?request=GetCapabilities&service=WCS";
		Endpoint ep = new Endpoint(url);
		assertEquals("Not recognized as a WCS endpoint", ep.getType(), EndpointType.WCS);
	}

	@Test
	public void testWMS() {
		String url = "http://testserver.usgs.gov/WMSServer?request=GetMap&";
		Endpoint ep = new Endpoint(url);
		assertEquals("Not recognized as a WCS endpoint", ep.getType(), EndpointType.WMS);
	}

	@Test
	public void testWFS() {
		String url = "http://testserver.usgs.gov/endpoint?request=GetCapabilities&service=WFS";
		Endpoint ep = new Endpoint(url);
		assertEquals("Not recognized as a WFS endpoint", ep.getType(), EndpointType.WFS);
	}

}
