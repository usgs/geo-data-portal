package gov.usgs.gdp.geotools;


import static org.junit.Assert.*;
import gov.usgs.gdp.io.FileHelper;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.geotools.data.shapefile.dbf.DbaseFileReader;
import org.geotools.data.shapefile.shp.ShapefileReader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class GeoToolsFileAnalysisTest {
	private static org.apache.log4j.Logger log = Logger.getLogger(GeoToolsFileAnalysisTest.class);
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
	public void testGetDBaseFileSummary() {
		List<String> result =  null;
		String fileToLoad = this.tempDir 
			+ this.seperator 
			+ "Sample_Files" 
			+ this.seperator
			+ "Shapefiles" 
			+ this.seperator
			+ "hru20VSR.DBF";
		GeoToolsFileAnalysis analyzer = new GeoToolsFileAnalysis();
		analyzer.setDbFileReader(GeoToolsFileAnalysis.readInDBaseFile(FileHelper.loadFile(fileToLoad), false, Charset.defaultCharset()));
		assertNotNull(analyzer.getDbFileReader()); // Ensure we've read in a file
		result = analyzer.getDBaseFileSummary();
		assertNotNull("Analyzer returned a null result object", result);
		assertFalse("Analyzer returned an empty result object",result.isEmpty());
	}
	
	@Test
	public void testGetShapeFileReader() {
		String fileToLoad = this.tempDir 
		+ this.seperator 
		+ "Sample_Files" 
		+ this.seperator
		+ "Shapefiles" 
		+ this.seperator
		+ "hru20VSR.SHP";
		ShapefileReader result = GeoToolsFileAnalysis.getShapeFileReader(fileToLoad);
		assertNotNull(result);
	}
	
	@Test
	public void testGetGeotoolsSummary() {
		String fileToLoad = this.tempDir 
		+ this.seperator 
		+ "Sample_Files" 
		+ this.seperator
		+ "Shapefiles" 
		+ this.seperator
		+ "hru20VSR.SHP";
		ShapefileReader shpFileReader = GeoToolsFileAnalysis.getShapeFileReader(fileToLoad);
		assertNotNull(shpFileReader);
		List<String> result = GeoToolsFileAnalysis.getShapeFileSummary(shpFileReader);
		assertNotNull(result);
		assertFalse(result.isEmpty());
	}
	
	@Test
	public void testReadInDBaseFile() {
		String fileToLoad = this.tempDir 
		+ this.seperator 
		+ "Sample_Files" 
		+ this.seperator
		+ "Shapefiles" 
		+ this.seperator
		+ "hru20VSR.DBF";
	
		File file = FileHelper.loadFile(fileToLoad);
		assertNotNull("File came back null. Cannot continue test.", file);
		GeoToolsFileAnalysis fa = new GeoToolsFileAnalysis();
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
