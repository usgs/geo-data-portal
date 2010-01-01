package gov.usgs.gdp.geotools;


import static org.junit.Assert.*;
import gov.usgs.gdp.io.FileHelper;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.geotools.data.shapefile.dbf.DbaseFileReader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class FileAnalysisTest {
	private static org.apache.log4j.Logger log = Logger.getLogger(FileAnalysisTest.class);
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
	public void readInDBaseFile() {
		String fileToLoad = this.tempDir 
		+ this.seperator 
		+ "Sample_Files" 
		+ this.seperator
		+ "Shapefiles" 
		+ this.seperator
		+ "hru20VSR.DBF";
	
		File file = FileHelper.loadFile(fileToLoad);
		assertNotNull("File came back null. Cannot continue test.", file);
		FileAnalysis fa = new FileAnalysis();
		DbaseFileReader result = fa.readInDBaseFile(file, false, Charset.defaultCharset());
		assertNotNull("DbaseFileReader object came back null", result);
		assertNotSame("", result.id());
		try {
			result.close();
		} catch (IOException e) {
			// Do nothing. Test is complete
		}
	}
	
}
