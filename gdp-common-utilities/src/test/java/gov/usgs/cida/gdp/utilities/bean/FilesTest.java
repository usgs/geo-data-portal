package gov.usgs.cida.gdp.utilities.bean;

import gov.usgs.cida.gdp.utilities.bean.Files;
import gov.usgs.cida.gdp.utilities.bean.ShapeFileSet;
import static org.junit.Assert.*;
import gov.usgs.cida.gdp.utilities.FileHelper;
import gov.usgs.cida.gdp.utilities.FileHelper;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.slf4j.LoggerFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


public class FilesTest {
	
	private static final String testFile = "demo_HUCs";
	
	private static org.slf4j.Logger log = LoggerFactory.getLogger(FilesTest.class);
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
		this.seperator = FileHelper.getSeparator();
		this.tempDir = FileHelper.getSystemTemp() 
		+ this.seperator 
		+ "GDP-APP-TEMP" 
		+ this.seperator
		+ "testing-feel-free-to-delete"
		+ this.seperator;
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
			fail("Sample files could not be loaded for test");
		}
	}

	@After
	public void tearDown() throws Exception {
		FileUtils.deleteDirectory((new File(this.tempDir)));
	}
	
	@Test
	public void testGetShapeFileSetList() {
		String shpFile = this.tempDir 
		+ this.seperator 
		+ "Sample_Files" 
		+ this.seperator
		+ "Shapefiles" 
		+ this.seperator
		+ testFile + ".shp";
		
		String prjFile = this.tempDir 
		+ this.seperator 
		+ "Sample_Files" 
		+ this.seperator
		+ "Shapefiles" 
		+ this.seperator
		+ testFile + ".prj";
		
		String dbfFile = this.tempDir 
		+ this.seperator 
		+ "Sample_Files" 
		+ this.seperator
		+ "Shapefiles" 
		+ this.seperator
		+ testFile + ".dbf";
		
		Files filesBean = new Files();
		Collection<File> files = new ArrayList<File>();
		files.add(new File(shpFile));
		files.add(new File(prjFile));
		files.add(new File(dbfFile));
		filesBean.setFiles(files);
		ShapeFileSet result = filesBean.getShapeFileSetBean();
		assertNotNull(result);
	}
	
	@Test
	public void testGetFilesBeanSetList() {
		Collection<File> files = FileHelper.getFileCollection(this.tempDir 
				+ this.seperator 
				+ "Sample_Files"
				+ this.seperator , true);
		assertNotNull(files);
		List<Files> filesList = Files.getFilesBeanSetList(files);
		assertNotNull(filesList);
		assertFalse(filesList.isEmpty());
	}
	
}
