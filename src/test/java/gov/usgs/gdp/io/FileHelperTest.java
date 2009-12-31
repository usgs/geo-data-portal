package gov.usgs.gdp.io;

import static org.junit.Assert.*;
import gov.usgs.gdp.servlet.ParseFile;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class FileHelperTest {
	private static org.apache.log4j.Logger log = Logger.getLogger(FileHelperTest.class);

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		log.debug("Started testing class");
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		log.debug("Ended testing class");
	}
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetSystemTemp() {
		String result = FileHelper.getSystemTemp();
		assertNotNull(result);
		assertFalse("".equals(result));
		log.debug("System temp path: " + result);
	}

	@Test
	public void testGetSystemPathSeparator() {
		String result = FileHelper.getSystemPathSeparator();
		assertNotNull(result);
		assertFalse("".equals(result));
		log.debug("System path separator: " + result);
	}

	@Test
	public void testGetSeparator() {
		String result = FileHelper.getSeparator();
		assertNotNull(result);
		assertFalse("".equals(result));
		log.debug("System separator: " + result);
	}
	
}
