package gov.usgs.gdp.bean;


import static org.junit.Assert.*;
import static org.junit.Assert.assertTrue;
import gov.usgs.gdp.helper.FileHelper;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AvailableFilesBeanTest {

	private String tempDir = "";

	private String seperator = "";
	
	@Before
	public void setUp() throws Exception {
		this.seperator = FileHelper.getSeparator();
		this.tempDir = FileHelper.getSystemTemp() 
			+ this.seperator 
			+ "GDP-APP-TEMP" 
			+ this.seperator
			+ "testing-feel-free-to-delete"
			+ this.seperator;
		(new File(this.tempDir)).mkdir();
		(new File(this.tempDir + "user_dir" + this.seperator)).mkdir();
		
		// Copy example files 
		ClassLoader cl = Thread.currentThread().getContextClassLoader(); 
		URL sampleFileLocation = cl.getResource("Sample_Files" + FileHelper.getSeparator());
		if (sampleFileLocation != null) {
			File sampleFiles = null;
			try {
				sampleFiles = new File(sampleFileLocation.toURI());
			} catch (URISyntaxException e) {
				assertTrue("Exception encountered: " + e.getMessage(), false);
			}
			FileHelper.copyFileToFile(sampleFiles, this.tempDir + this.seperator);
			FileHelper.copyFileToFile(sampleFiles, this.tempDir + "user_dir" + this.seperator);
		} else {
			assertTrue("Sample files could not be loaded for test", false);
		}
	}


	@After
	public void tearDown() throws Exception {
		FileUtils.deleteDirectory((new File(this.tempDir)));
	}
	
	@Test
	public void testGetAvailableFilesBeanWithEmptyStringDirectory() {
		AvailableFilesBean result = 
			AvailableFilesBean.getAvailableFilesBean("", "");
		
		assertNull(result);
	}

	@Test
	public void testGetAvailableFilesBeanWithNullDirectory() {
		AvailableFilesBean result = 
			AvailableFilesBean.getAvailableFilesBean(null, null);
		
		assertNull(result);
	}

        @Test
	public void testGetAvailableFilesBeanWithPopulatedExamplesDirectory() {
		AvailableFilesBean result = 
			AvailableFilesBean.getAvailableFilesBean(this.tempDir, null);
		
		assertNotNull(result);
		assertNotNull(result.getExampleFileList());
		assertFalse(result.getExampleFileList().isEmpty());
		assertNotNull(result.getExampleFileList());
		assertFalse(result.getShapeSetList().isEmpty());
		assertTrue(result.getUserFileList().isEmpty());
	}
	
	@Test
	public void testGetAvailableFilesBeanWithPopulatedUserDirectory() {
		AvailableFilesBean result = 
			AvailableFilesBean.getAvailableFilesBean(this.tempDir, 
					this.tempDir 
						+ "user_dir" 
						+ this.seperator
						+ "Sample_Files"
						+ this.seperator
						+ "Shapefiles"
						+ this.seperator);
		
		assertNotNull(result);
		assertNotNull(result.getExampleFileList());
		assertFalse(result.getExampleFileList().isEmpty());
		assertNotNull(result.getExampleFileList());
		assertFalse(result.getShapeSetList().isEmpty());
		assertFalse(result.getUserFileList().isEmpty());
		
		
	}

        @Test
	public void testGetAvailableFilesBeanWithBogusUserDirectory() {
		AvailableFilesBean result =
			AvailableFilesBean.getAvailableFilesBean(this.tempDir,
					this.tempDir
						+ "user_dir"
						+ this.seperator
						+ "Sample_Files"
						+ this.seperator
						+ "test"
						+ this.seperator);

		assertNotNull(result);
		assertNotNull(result.getExampleFileList());
		assertFalse(result.getExampleFileList().isEmpty());
		assertNotNull(result.getExampleFileList());
		assertFalse(result.getShapeSetList().isEmpty());
		assertFalse(result.getUserFileList().isEmpty());


	}

	@Test
	public void testGetXmlWithEverythingPopulated() {
		AvailableFilesBean filesBean = 
			AvailableFilesBean.getAvailableFilesBean(this.tempDir, 
					this.tempDir 
						+ "user_dir" 
						+ this.seperator
						+ "Sample_Files"
						+ this.seperator
						+ "Shapefiles"
						+ this.seperator);
		String result = filesBean.toXml();
		
		assertNotNull(result);
		assertNotSame(0, result.length());
	}
}
