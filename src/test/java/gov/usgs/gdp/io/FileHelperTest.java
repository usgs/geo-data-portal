package gov.usgs.gdp.io;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class FileHelperTest {
	private static org.apache.log4j.Logger log = Logger.getLogger(FileHelperTest.class);
	private String tempDir = ""; 
	private String seperator = "";
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
		this.seperator =  java.io.File.separator;
		String currentTime = Long.toString((new Date()).getTime());
		this.tempDir = systemTempDir + this.seperator + currentTime;
		(new File(this.tempDir)).mkdir();
		
		// Copy example files 
		ClassLoader cl = Thread.currentThread().getContextClassLoader(); 
		URL sampleFileLocation = cl.getResource("Sample_Files/");
		if (sampleFileLocation != null) {
			File sampleFiles = null;
			try {
				sampleFiles = new File(sampleFileLocation.toURI());
			} catch (URISyntaxException e) {
				assertTrue("Exception encountered: " + e.getMessage(), false);
			}
			FileHelper.copyFileToFile(sampleFiles, this.tempDir + this.seperator);
		} else {
			assertTrue("Sample files could not be loaded for test", false);
		}
	}

	@After
	public void tearDown() throws Exception {
		FileUtils.deleteDirectory((new File(this.tempDir)));
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
	
	@Test
	public void testFindFile() {
		String fileToLoad = "hru20VSR.SHX";
		String rootDir = this.tempDir + this.seperator;
		File result = FileHelper.findFile(fileToLoad, rootDir);
		assertNotNull("FineFile did not find the file " + fileToLoad + " within " + rootDir, result);
		assertEquals("File loaded does not have the same name as the file suggested", fileToLoad, result.getName());
	}
	
	@Test
	public void testLoadFile() { 
		String fileToLoad = this.tempDir 
			+ this.seperator 
			+ "Sample_Files" 
			+ this.seperator
			+ "Shapefiles" 
			+ this.seperator
			+ "hru20VSR.SHX";
		
		File result = FileHelper.loadFile(fileToLoad);
		assertNotNull("File came back null", result);
		assertTrue("File is not a file", result.isFile());
	}
	
	@Test
	public void testGetFileList() {
		String dirToList = this.tempDir + this.seperator;
		List<String> result = FileHelper.getFileList(dirToList, true);
		assertNotNull("File listing came back null", result);
		assertFalse("There were no files listed", result.isEmpty());
	}
}
