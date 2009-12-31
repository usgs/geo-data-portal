package gov.usgs.gdp.io;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Date;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class FileHelperTest {
	private static org.apache.log4j.Logger log = Logger.getLogger(FileHelperTest.class);
	private String tempDir = ""; 
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
		String systemTempDir = System.getProperty("java.io.tmpdir"); 
		String seperator =  java.io.File.separator;
		String currentTime = Long.toString((new Date()).getTime());
		this.tempDir = systemTempDir + seperator + currentTime;
		(new File(this.tempDir)).mkdir();
	}

	@After
	public void tearDown() throws Exception {
		(new File(this.tempDir)).delete();
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

	@Test
	public void testDeleteDir() {
		boolean result = false;
		result = FileHelper.deleteDir(this.tempDir);
		assertTrue(result);
	}
	
	@Test
	public void testCreateDir() {
		boolean result = false;
		// 1 Has to be added since based on how quickly this test runs after the 
		// setUp() method, directory naming may clash, directory is not created but
		// java.io.File.mkdir() passes no exception
		String testDir =  System.getProperty("java.io.tmpdir") + 
			java.io.File.separator + 
			Long.toString((new Date()).getTime()) + 1;
		result = FileHelper.createDir(testDir);
		assertTrue(result);
		(new File(testDir)).delete();
	}
	
}
