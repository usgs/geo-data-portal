package gov.usgs.gdp.io;

import static org.junit.Assert.*;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.geotools.data.FileDataStore;
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
	private String tempDir = "";

	private String seperator = "";
	
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
	public void testCreateDir() {
		boolean result = false;
		String testDir =  System.getProperty("java.io.tmpdir") + 
			java.io.File.separator + 
			Long.toString((new Date()).getTime()) + 1;
		result = FileHelper.createDir(testDir);
		assertTrue(result);
		(new File(testDir)).delete();
	}
	
	@Test
	public void testCopyFileToFile() {
		File fileToCopy = new File(this.tempDir 
		+ this.seperator 
		+ "Sample_Files" 
		+ this.seperator
		+ "Shapefiles" 
		+ this.seperator
		+ "hru20VSR.SHX");
		
		String fileToCopyTo = this.tempDir 
		+ this.seperator 
		+ "Sample_Files" 
		+ this.seperator
		+ "Shapefiles" 
		+ this.seperator
		+ "hru20VSR.COPY";
		
		boolean result = FileHelper.copyFileToFile(fileToCopy, fileToCopyTo);
		assertTrue(result);
		result = FileHelper.copyFileToFile(new File("doesnt/exist"), "doesnt/exist");
		assertFalse(result);
		
	}
	
	@Test 
	public void testDeleteFile() {
		String fileToLoad = this.tempDir 
		+ this.seperator 
		+ "Sample_Files" 
		+ this.seperator
		+ "Shapefiles" 
		+ this.seperator
		+ "hru20VSR.SHX";
		
		boolean result = FileHelper.deleteFile("File/That/Doesnt/Exist");
		assertFalse(result);
		result = FileHelper.deleteFile(fileToLoad);
		assertTrue(result);
	}
	
	@Test 
	public void testDeleteDirRecursively() {
		File lockedFile = new File(this.tempDir 
		+ this.seperator 
		+ "Sample_Files" 
		+ this.seperator
		+ "Shapefiles" 
		+ this.seperator
		+ "hru20VSR.SHX");
		lockedFile.setWritable(false);
		
		String dirToDelete = this.tempDir 
		+ this.seperator;
		boolean result = FileHelper.deleteDirRecursively(new File(dirToDelete));
		assertTrue(result);
		result = FileHelper.deleteDirRecursively(new File("Directory/That/Doesnt/Exist"));
		assertFalse(result);
		result = FileHelper.deleteDirRecursively(lockedFile);
		assertFalse(result);
		lockedFile.setWritable(true);
		FileHelper.deleteFile(lockedFile);
	}
	
	@Test public void testDeleteDirRecursivelyUsingString() {
		String dirToDelete = this.tempDir 
		+ this.seperator;
		boolean result = FileHelper.deleteDirRecursively(dirToDelete);
		assertTrue(result);
		result = FileHelper.deleteDirRecursively("Directory/That/Doesnt/Exist");
		assertFalse(result);
	}
	
	@Test
	public void testFileHelper() {
		FileHelper result = new FileHelper();
		assertNotNull(result);
	}

	@Test
	public void testFindFile() {
		String fileToLoad = "hru20VSR.SHX";
		String rootDir = this.tempDir + this.seperator;
		File result = FileHelper.findFile(fileToLoad, rootDir);
		assertNotNull("FineFile did not find the file " + fileToLoad + " within " + rootDir, result);
		assertEquals("File loaded does not have the same name as the file suggested", fileToLoad, result.getName());
		result = FileHelper.findFile("should.not.work", rootDir);
		assertNull(result);
	}

	@Test
	public void testGetFileList() {
		String dirToList = this.tempDir + this.seperator;
		List<String> result = null;
		result = FileHelper.getFileList(null, true);
		assertNull(result);
		result = FileHelper.getFileList(dirToList, true);
		assertNotNull("File listing came back null", result);
		assertFalse("There were no files listed", result.isEmpty());
		String fakeDirToList = this.tempDir + this.seperator + "9387509352";
		result = FileHelper.getFileList(fakeDirToList, true);
		assertNull(result);
	}

	@Test
	public void testGetSeparator() {
		String result = FileHelper.getSeparator();
		assertNotNull(result);
		assertFalse("".equals(result));
		log.debug("System separator: " + result);
	}
	
	@Test
	public void testGetSystemPathSeparator() {
		String result = FileHelper.getSystemPathSeparator();
		assertNotNull(result);
		assertFalse("".equals(result));
		log.debug("System path separator: " + result);
	}
	
	@Test
	public void testGetSystemTemp() {
		String result = FileHelper.getSystemTemp();
		assertNotNull(result);
		assertFalse("".equals(result));
		log.debug("System temp path: " + result);
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
	public void testGetFileCollection() {
		String dirToList = this.tempDir + this.seperator;
		Collection<File> result = null;
		
		String nullString = null;
		result = FileHelper.getFileCollection(nullString, true);
		assertNull(result);
		result = FileHelper.getFileCollection(dirToList, true);
		assertNotNull("File listing came back null", result);
		assertFalse("There were no files listed", result.isEmpty());
		String fakeDirToList = this.tempDir + this.seperator + "9387509352";
		result = FileHelper.getFileCollection(fakeDirToList, true);
		assertNull(result);
	}
	
	@Test
	public void testGetShapeFileDataStores() {
		String firstFileToLoad = this.tempDir 
		+ this.seperator 
		+ "Sample_Files" 
		+ this.seperator
		+ "Shapefiles" 
		+ this.seperator
		+ "hru20VSR.SHP";
		
		String secondFileToLoad = this.tempDir 
		+ this.seperator 
		+ "Sample_Files" 
		+ this.seperator
		+ "Shapefiles" 
		+ this.seperator
		+ "Yahara_River_HRUs_geo_WGS84.shp";
		
		List<String> fileList = new ArrayList<String>();
		
		List<FileDataStore> result = null;
		result = FileHelper.getShapeFileDataStores(fileList);
		assertNotNull(result);
		assertTrue(result.isEmpty());
		
		fileList.add(firstFileToLoad);
		result = FileHelper.getShapeFileDataStores(fileList);
		assertNotNull(result);
		assertFalse(result.isEmpty());
		assertEquals(1, result.size());
		
		fileList.add(secondFileToLoad);
		result = FileHelper.getShapeFileDataStores(fileList);
		assertNotNull(result);
		assertFalse(result.isEmpty());
		assertEquals(2, result.size());
	}
}
