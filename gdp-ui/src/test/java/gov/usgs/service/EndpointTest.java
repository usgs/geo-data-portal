/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.usgs.service;

import gov.usgs.service.Endpoint.EndpointType;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jwalker
 */
public class EndpointTest {

	private static Endpoint testpoint;

	@BeforeClass
	public static void setUpClass() throws Exception {
		testpoint = new Endpoint("http://internal.cida.usgs.gov/gdp/process/WebProcessingService?service=WPS&request=GetCapabilities");
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
		testpoint = null;
	}

	/**
	 * Test of getURL method, of class Endpoint.
	 */ @Test
	public void testGetURL() {
		System.out.println("getURL");
		String expResult = "http://internal.cida.usgs.gov/gdp/process/WebProcessingService?service=WPS&request=GetCapabilities";
		String result = testpoint.getURL();
		assertEquals(expResult, result);
	}

	/**
	 * Test of getType method, of class Endpoint.
	 */ @Test
	public void testGetType() {
		System.out.println("getType");
		EndpointType expResult = EndpointType.WPS;
		EndpointType result = testpoint.getType();
		assertEquals(expResult, result);
	}

	/**
	 * Test of getComparisonString method, of class Endpoint.
	 */ @Test
	public void testCreateComparisonString() {
		System.out.println("createComparisonString");
		String expResult = "http://internal.cida.usgs.gov/gdp/process/";
		String result = testpoint.getComparisonString();
		assertEquals(expResult, result);
		testpoint = new Endpoint("http://internal.cida.usgs.gov/gdp/process/ServletServlet");
		result = testpoint.getComparisonString();
		assertEquals(expResult, result);
		testpoint = new Endpoint("http://internal.cida.usgs.gov/gdp/process/Servlet?query=with/slashes");
		result = testpoint.getComparisonString();
		assertEquals(expResult, result);
	}

	/**
	 * Test of equals method, of class Endpoint.
	 */ @Test
	public void testEquals() {
		System.out.println("equals");
		Endpoint other = new Endpoint("http://internal.cida.usgs.gov/gdp/process/WebProcessingService?service=WPS&request=DescribeProcess&id=Algorithm1");
		boolean expResult = true;
		boolean result = testpoint.equals(other);
		assertEquals(expResult, result);
	}

	/**
	 * Test of equals method, of class Endpoint.
	 */ @Test
	public void testEqualsFalse() {
		System.out.println("equalsFalse");
		Endpoint other = new Endpoint("http://internal.cida.usgs.gov/gdp/utility/WebProcessingService?Service=WPS&Request=GetCapabilities");
		boolean expResult = false;
		boolean result = testpoint.equals(other);
		assertEquals(expResult, result);
	}
}