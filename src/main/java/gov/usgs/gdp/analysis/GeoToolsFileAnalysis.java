package gov.usgs.gdp.analysis;

import gov.usgs.cida.gdp.utilities.FileHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.shapefile.ShpFiles;
import org.geotools.data.shapefile.dbf.DbaseFileHeader;
import org.geotools.data.shapefile.dbf.DbaseFileReader;
import org.geotools.data.shapefile.shp.ShapefileException;
import org.geotools.data.shapefile.shp.ShapefileHeader;
import org.geotools.data.shapefile.shp.ShapefileReader;

import com.vividsolutions.jts.geom.GeometryFactory;

public class GeoToolsFileAnalysis {
	DbaseFileReader dbFileReader;
	ShapefileReader shpFileReader;
	
	public static FileDataStore getFileDataStore(File file) throws IOException {
		if (file == null) return null;
		return FileDataStoreFinder.getDataStore(file);
	}
	
	
	
	/**
	 * Reads in a File and transforms it to a DbaseFileReader object
	 * @param file
	 * @return
	 * @throws IOException 
	 */
	public static DbaseFileReader readInDBaseFile(File file, boolean useMemoryMappedBuffer, Charset charset) throws IOException {
		DbaseFileReader result = null;
		FileChannel in = null;
		try {
			in =  new FileInputStream(file).getChannel();
			result = new DbaseFileReader(in, useMemoryMappedBuffer, charset);
		} finally {
			if (in != null) {
				in.close();
			}
		}
		return result;		
	}

	
	/**
	 * Pull a DBase File Header object from an internal in DbaseFileReader object
	 * @param dbFileReader
	 * @return null if no internal DbaseFileReader object is defined
	 */
	public DbaseFileHeader getDBaseFileHeader() {
		DbaseFileHeader result = null;
		
		DbaseFileReader localFileReader = getDbFileReader();
		if (localFileReader != null) result = getDBaseFileHeader(localFileReader);
		
		return result;
	}
	
	public List<String> getDBaseFileSummary() throws IOException {
		if (getDbFileReader() == null) return null; 
		return GeoToolsFileAnalysis.getDBaseFileSummary(getDbFileReader());
	}
	
	public static  List<String> getDBaseFileSummary(File file) throws IOException {
		DbaseFileReader reader = GeoToolsFileAnalysis.readInDBaseFile(file, false, Charset.defaultCharset());
		try {
		List<String> result = null;
			result = GeoToolsFileAnalysis.getDBaseFileSummary(reader);
			if (!(reader.getHeader() == null)) {
				reader.close();
			}
			return result;
		} finally {
			if (reader.getHeader() != null)	reader.close();
		}

	}
	
	
	/**
	 * Returns a summary of the DBase File
	 * 
	 * @param dbaseFileReader
	 * @return
	 * @throws IOException 
	 */
	public static List<String> getDBaseFileSummary(DbaseFileReader dbaseFileReader) throws IOException {
		List<String> result = new ArrayList<String>();
		int fields = dbaseFileReader.getHeader().getNumFields();
		int recordCount = dbaseFileReader.getHeader().getNumRecords();
		result.add("File Type: DBase File");
		result.add("File Last Updated: " + dbaseFileReader.getHeader().getLastUpdateDate());
		result.add("Header Length: " + dbaseFileReader.getHeader().getHeaderLength());
		result.add("RecordLength: " + dbaseFileReader.getHeader().getRecordLength());
		result.add("Number Of Fields: " + fields);
		result.add("Largest Field Size (in bytes): " + dbaseFileReader.getHeader().getLargestFieldSize());
		result.add("Number Of Records: " + recordCount);
		result.add("Header String Rep: " + dbaseFileReader.getHeader().toString());
		result.add("Begin record scroll: \n");
		
		try {
			while (dbaseFileReader.hasNext()) {
				DbaseFileReader.Row row = null;
					try {
						row = dbaseFileReader.readRow();
						result.add(row.toString());
					} catch (IOException e) {
						// Do nothing.
					}
					
			}
			return result;
		} finally {
			dbaseFileReader.close();
		}
		
		
	}
	
	public static List<String> getDBaseFileSummary(String filePath) throws IOException {
		if ("".equals(filePath) || filePath == null) return null;
		return GeoToolsFileAnalysis.getDBaseFileSummary(GeoToolsFileAnalysis.readInDBaseFile(FileHelper.loadFile(filePath), false, Charset.defaultCharset()));
	}
	
	public static List<String> getShapeFileSummary(String filePath) throws IOException {
		if ("".equals(filePath) || filePath == null) return null;
		return GeoToolsFileAnalysis.getShapeFileSummary(GeoToolsFileAnalysis.getShapeFileReader(filePath));
	}
	
	public List<String> getShapeFileSummary() throws IOException {
		if (getShpFileReader() == null) return null;
		return GeoToolsFileAnalysis.getShapeFileSummary(getShpFileReader());
	}
	
	/**
	 * Returns a Shape File summary
	 * @param file
	 * @return
	 * @throws IOException 
	 */
	public static List<String> getShapeFileSummary(ShapefileReader file) throws IOException {
		List<String> result = null;
		// Load in the files and try them out....
		while (file.hasNext()) {
			if (result == null) result = new ArrayList<String>();
			ShapefileReader.Record nextRecord = file.nextRecord();
			result.add("Record number: " + nextRecord.toString().trim());
		}
		file.close();
		return result;
	}

	/**
	 * Gets a ShapeFile summary using a File Object
	 * 
	 * @param shpFile
	 * @return
	 * @throws IOException 
	 */
	public static List<String> getShapeFileSummary(File shpFile) throws IOException {
		if (shpFile == null) return null;
		return GeoToolsFileAnalysis.getShapeFileSummary(GeoToolsFileAnalysis.getShapeFileReader(shpFile));
	}

	/**
	 * Returns a Shapefile Header summary
	 * 
	 * @param file
	 * @return
	 */
	public static List<String> getShapeFileHeaderSummary(ShapefileReader file) {
		if (file == null) return null;
		List<String> result = null;
		ShapefileHeader shpHead = file.getHeader();
		if (shpHead != null) {
			result = new ArrayList<String>();
			result.add(shpHead.toString().trim());
		}
		return result;
	}
	
	/**
	 * Pull a DBase File Header object from a passed in DbaseFileReader object
	 * @param dbFileReader
	 * @return
	 */
	public DbaseFileHeader getDBaseFileHeader(DbaseFileReader localFileReader) {
		DbaseFileHeader result = null;
		
		result = localFileReader.getHeader();
		
		return result;
	}

	public DbaseFileReader getDbFileReader() {
		return this.dbFileReader;
	}

	public void setDbFileReader(DbaseFileReader localFileReader) {
		this.dbFileReader = localFileReader;
	}

	public ShapefileReader getShpFileReader() {
		return this.shpFileReader;
	}

	public void setShpFileReader(ShapefileReader localShpFileReader) {
		this.shpFileReader = localShpFileReader;
	}

	public static List<String> getShapeFileHeaderSummary(File shpFile) throws IOException {
		ShapefileReader reader = GeoToolsFileAnalysis.getShapeFileReader(shpFile);
		List<String> result = GeoToolsFileAnalysis.getShapeFileHeaderSummary(reader);
		reader.close();
		return result;
	}


	/**
	 * Read in a SHAPE file from the file structure
	 * 
	 * @param file
	 * @return
	 * @throws IOException 
	 * @throws MalformedURLException 
	 * @throws ShapefileException 
	 */
	public static ShapefileReader getShapeFileReader(String file) throws ShapefileException, MalformedURLException, IOException {
		if (file == null) return null;
		ShapefileReader result = null;

			result = new ShapefileReader(new ShpFiles(file), true, true, new GeometryFactory());
		return result;
	}

	public static ShapefileReader getShapeFileReader(File shpFile) throws ShapefileException, MalformedURLException, IOException {
		if (shpFile == null) return null;
		ShapefileReader result = null;
		
		result = new ShapefileReader(new ShpFiles(shpFile), true, true, new GeometryFactory());
		return result;
	}
}
