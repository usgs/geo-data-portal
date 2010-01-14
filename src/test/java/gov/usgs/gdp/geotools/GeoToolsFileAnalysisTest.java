package gov.usgs.gdp.geotools;


import static org.junit.Assert.*;
import gov.usgs.gdp.analysis.GeoToolsFileAnalysis;
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
import org.geotools.data.FileDataStore;
import org.geotools.data.shapefile.dbf.DbaseFileHeader;
import org.geotools.data.shapefile.dbf.DbaseFileReader;
import org.geotools.data.shapefile.shp.ShapefileReader;
import org.geotools.data.shapefile.shp.ShapefileReader.Record;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.vividsolutions.jts.geom.Geometry;

public class GeoToolsFileAnalysisTest {
	private static org.apache.log4j.Logger log = Logger.getLogger(GeoToolsFileAnalysisTest.class);
	private String tempDir = ""; 
	private String seperator = "";
	@Before
	public void setUp() throws Exception {
		this.tempDir = System.getProperty("java.io.tmpdir");

		if ( !(this.tempDir.endsWith("/") || this.tempDir.endsWith("\\")) )
		   this.tempDir = this.tempDir + System.getProperty("file.separator");
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
		String nullString = null;
		File loadedFile = new File(fileToLoad);
		
		GeoToolsFileAnalysis analyzer = new GeoToolsFileAnalysis();
		result = analyzer.getDBaseFileSummary();
		assertNull(result);
		
		result = GeoToolsFileAnalysis.getDBaseFileSummary("");
		assertNull(result);
		
		result = GeoToolsFileAnalysis.getDBaseFileSummary(nullString);
		assertNull(result);
		
		result = GeoToolsFileAnalysis.getDBaseFileSummary(loadedFile);
		assertNotNull(result);
		assertFalse(result.isEmpty());
		
		result = GeoToolsFileAnalysis.getDBaseFileSummary(fileToLoad);
		assertNotNull(result);
		assertFalse(result.isEmpty());
		analyzer.setDbFileReader(GeoToolsFileAnalysis.readInDBaseFile(FileHelper.loadFile(fileToLoad), false, Charset.defaultCharset()));
		assertNotNull(analyzer.getDbFileReader()); // Ensure we've read in a file
		
		result = analyzer.getDBaseFileSummary();
		assertNotNull("Analyzer returned a null result object", result);
		assertFalse("Analyzer returned an empty result object",result.isEmpty());
		loadedFile.delete();
	}
	
	@Test
	public void testProcessShapeFilePolygons() {
		String fileToLoad = this.tempDir 
		+ this.seperator 
		+ "Sample_Files" 
		+ this.seperator
		+ "Shapefiles" 
		+ this.seperator
		+ "statesp020.shp";
		
		ShapefileReader result = GeoToolsFileAnalysis.getShapeFileReader(fileToLoad);
		try {
			
			while (result.hasNext()) {
				Record record = result.nextRecord();
				Geometry shape = (Geometry) record.shape();
				int numOfGeometries = shape.getNumGeometries();
				
				
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try { result.close(); } catch (IOException e) {	e.printStackTrace(); }
		}
			
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
		
		String nullString= null;
		ShapefileReader result = GeoToolsFileAnalysis.getShapeFileReader(nullString);
		assertNull(result);
		
		File nullFile = null;
		result = GeoToolsFileAnalysis.getShapeFileReader(nullFile);
		assertNull(result);
		
		result = GeoToolsFileAnalysis.getShapeFileReader(fileToLoad);
		assertNotNull(result);
		try {
			result.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testGetShapeFileHeaderSummary() {
		List<String> result = null;
		String fileToLoad = this.tempDir 
		+ this.seperator 
		+ "Sample_Files" 
		+ this.seperator
		+ "Shapefiles" 
		+ this.seperator
		+ "hru20VSR.SHP";
		
		String nullString = null;
		ShapefileReader reader = GeoToolsFileAnalysis.getShapeFileReader(nullString);
		assertNull(reader);
		
		reader = GeoToolsFileAnalysis.getShapeFileReader(fileToLoad);
		assertNotNull(reader);
		
		
		
		result = GeoToolsFileAnalysis.getShapeFileHeaderSummary(reader);
		assertNotNull("GeoToolsFileAnalysis.getShapeFileHeaderSummary returned null",result);
		assertFalse("GeoToolsFileAnalysis.getShapeFileHeaderSummary returned empty List of type String",result.isEmpty());
		try {
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		result = GeoToolsFileAnalysis.getShapeFileHeaderSummary(reader);
		assertNull(result);
		
		
		
		ShapefileReader nullReader = null;
		result = GeoToolsFileAnalysis.getShapeFileHeaderSummary(nullReader);
		assertNull(result);
	}
	
	@Test
	public void testGetShapeFileHeaderSummaryUsingFileObject() {
		List<String> result = null;
		String fileToLoad = this.tempDir 
		+ this.seperator 
		+ "Sample_Files" 
		+ this.seperator
		+ "Shapefiles" 
		+ this.seperator
		+ "hru20VSR.SHP";
		
		result = GeoToolsFileAnalysis.getShapeFileHeaderSummary(new File(fileToLoad));
		assertNotNull(result);
		fileToLoad = null;
	}
	
	@Test 
	public void testGetShapefileSummary() {
		List<String> result =  null;
		String fileToLoad = this.tempDir 
			+ this.seperator 
			+ "Sample_Files" 
			+ this.seperator
			+ "Shapefiles" 
			+ this.seperator
			+ "hru20VSR.SHP";
		String nullString = null;
		
		GeoToolsFileAnalysis subject = new GeoToolsFileAnalysis();
		result = subject.getShapeFileSummary();
		assertNull(result);
		
		result = GeoToolsFileAnalysis.getShapeFileSummary("");
		assertNull(result);
		
		result = GeoToolsFileAnalysis.getShapeFileSummary(nullString);
		assertNull(result);
		
		ShapefileReader shpFileReader = GeoToolsFileAnalysis.getShapeFileReader(fileToLoad);
		subject.setShpFileReader(shpFileReader);
		result = subject.getShapeFileSummary();
		assertNotNull(result);
		assertFalse(result.isEmpty());
				
		result = GeoToolsFileAnalysis.getShapeFileSummary(fileToLoad);
		assertNotNull(result);
		assertFalse(result.isEmpty());
		
		result = GeoToolsFileAnalysis.getShapeFileSummary(new File(fileToLoad));
		assertNotNull(result);
		assertFalse(result.isEmpty());
		
		File nullFile = null;
		result = GeoToolsFileAnalysis.getShapeFileSummary(nullFile);
		assertNull(result);
		
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
		String fakeFileToLoad = fileToLoad + "xxx";
		String nonDbaseFileToLoad = this.tempDir 
		+ this.seperator 
		+ "Sample_Files" 
		+ this.seperator
		+ "Shapefiles" 
		+ this.seperator
		+ "hru20VSR.SHP";
		
		File file = FileHelper.loadFile(fileToLoad);
		File fakeFile = FileHelper.loadFile(fakeFileToLoad);
		File nonDbFile = FileHelper.loadFile(nonDbaseFileToLoad);
		assertNotNull("File came back null. Cannot continue test.", file);
		DbaseFileReader result = GeoToolsFileAnalysis.readInDBaseFile(file, false, Charset.defaultCharset());
		assertNotNull("DbaseFileReader object came back null", result);
		assertNotSame("", result.id());		
		try {
			result.close();
		} catch (IOException e) {
			// Do nothing. Test is complete
		}
		// Test FileNotFound
		result = GeoToolsFileAnalysis.readInDBaseFile(fakeFile, false, Charset.defaultCharset()); 
		assertNull(result);
		// Test IOException
		result = GeoToolsFileAnalysis.readInDBaseFile(nonDbFile, false, Charset.defaultCharset());
		assertNull(result);
	}
	
	@Test
	public void testGetDbaseFileHeader() {
		String fileToLoad = this.tempDir 
		+ this.seperator 
		+ "Sample_Files" 
		+ this.seperator
		+ "Shapefiles" 
		+ this.seperator
		+ "hru20VSR.DBF";
		GeoToolsFileAnalysis subject = new GeoToolsFileAnalysis();
		DbaseFileHeader result = subject.getDBaseFileHeader();
		assertNull(result);
		File file = FileHelper.loadFile(fileToLoad);
		DbaseFileReader reader = GeoToolsFileAnalysis.readInDBaseFile(file, false, Charset.defaultCharset());
		subject.setDbFileReader(reader);
		result = subject.getDBaseFileHeader();
		assertNotNull(result);
	}
	
	@Test
	public void testGetFileDataStore() {
		String fileToLoad = this.tempDir 
		+ this.seperator 
		+ "Sample_Files" 
		+ this.seperator
		+ "Shapefiles" 
		+ this.seperator
		+ "hru20VSR.SHP";
		
		FileDataStore result = GeoToolsFileAnalysis.getFileDataStore(new File("does/not/exist"));
		assertNull(result);
		
		result = GeoToolsFileAnalysis.getFileDataStore(null);
		assertNull(result);
		
		result = GeoToolsFileAnalysis.getFileDataStore(new File(fileToLoad));
		assertNotNull(result);
	}
}
