package gov.usgs.gdp.bean;

import static org.junit.Assert.*;
import gov.usgs.gdp.io.FileHelper;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


public class ShapeFileSetBeanTest {
	private static org.apache.log4j.Logger log = Logger.getLogger(ShapeFileSetBeanTest.class);
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
	public void testGetAttributeListWithNullMemberVariable() {
		ShapeFileSetBean sfsb = new ShapeFileSetBean();
		List<String> result = sfsb.getAttributeList();
		assertNotNull(result);
		assertTrue(result.isEmpty());
	}
	
	@Test 
	public void testGetAttributeListFromBean() {
		String shpFile = this.tempDir 
		+ this.seperator 
		+ "Sample_Files" 
		+ this.seperator
		+ "Shapefiles" 
		+ this.seperator
		+ "hru20VSR.SHP";
		
		String prjFile = this.tempDir 
		+ this.seperator 
		+ "Sample_Files" 
		+ this.seperator
		+ "Shapefiles" 
		+ this.seperator
		+ "hru20VSR.PRJ";
		String dbfFile = this.tempDir 
		
		+ this.seperator 
		+ "Sample_Files" 
		+ this.seperator
		+ "Shapefiles" 
		+ this.seperator
		+ "hru20VSR.DBF";
		
		FilesBean filesBean = new FilesBean();
		Collection<File> files = new ArrayList<File>();
		files.add(new File(shpFile));
		files.add(new File(prjFile));
		files.add(new File(dbfFile));
		filesBean.setFiles(files);
		ShapeFileSetBean shapeFileSetBean = filesBean.getShapeFileSetBean();
		assertNotNull(shapeFileSetBean);
		List<String> result = ShapeFileSetBean.getAttributeListFromBean(shapeFileSetBean);
		assertNotNull(result);
		assertFalse(result.isEmpty());
	}
}
