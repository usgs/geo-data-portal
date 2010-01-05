package gov.usgs.gdp.geotools;

import gov.usgs.gdp.io.FileHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.geotools.data.shapefile.ShpFiles;
import org.geotools.data.shapefile.dbf.DbaseFileHeader;
import org.geotools.data.shapefile.dbf.DbaseFileReader;
import org.geotools.data.shapefile.shp.ShapefileException;
import org.geotools.data.shapefile.shp.ShapefileReader;

import com.vividsolutions.jts.geom.GeometryFactory;

public class GeoToolsFileAnalysis {
	DbaseFileReader dbFileReader;
	ShapefileReader shpFileReader;
	
	public GeoToolsFileAnalysis() {
		// Placeholder
	}
	
	/**
	 * Reads in a File and transforms it to a DbaseFileReader object
	 * @param file
	 * @return
	 */
	public static DbaseFileReader readInDBaseFile(File file, boolean useMemoryMappedBuffer, Charset charset) {
		DbaseFileReader result = null;
		try {
			FileChannel in =  new FileInputStream(file).getChannel();
			result = new DbaseFileReader(in, useMemoryMappedBuffer, charset);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
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
	
	public List<String> getDBaseFileSummary() {
		if (getDbFileReader() == null) return null; 
		return GeoToolsFileAnalysis.getDBaseFileSummary(getDbFileReader());
	}
	
	public static  List<String> getDBaseFileSummary(File file) {
		if (file == null) return null; 
		return GeoToolsFileAnalysis.getDBaseFileSummary(GeoToolsFileAnalysis.readInDBaseFile(file, false, Charset.defaultCharset()));
	}
	
	
	
	public static List<String> getDBaseFileSummary(DbaseFileReader dbaseFileReader) {
		if (dbaseFileReader == null) return null;
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
		
		//Object[] fields = new Object[dbaseFileReader.getHeader().getNumFields()];
		while (dbaseFileReader.hasNext()) {
			DbaseFileReader.Row row = null;
			try {
				row = dbaseFileReader.readRow();
				if (row != null) {
					result.add(row.toString());
				}
			} catch (IOException e) {
				// Do nothing at this point. It was a file read error.
			}
		}
		
		return result;
	}
	
	public static List<String> getDBaseFileSummary(String filePath) {
		if ("".equals(filePath) || filePath == null) return null;
		return GeoToolsFileAnalysis.getDBaseFileSummary(GeoToolsFileAnalysis.readInDBaseFile(FileHelper.loadFile(filePath), false, Charset.defaultCharset()));
	}
	
	/**
	 * Read in a SHAPE file from the file structure
	 * 
	 * @param file
	 * @return
	 */
	public static ShapefileReader getShapeFileReader(String file) {
		ShapefileReader result = null;
		
		try {
			result = new ShapefileReader(new ShpFiles(file), true, true, new GeometryFactory());
		} catch (ShapefileException e) {
			return result;
		} catch (MalformedURLException e) {
			return result;
		} catch (IOException e) {
			return result;
		}
		return result;
	}
	
	/**
	 * 
	 * @param file
	 * @return
	 */
	public static List<String> getShapeFileSummary(ShapefileReader file) {
		List<String> result = null;
		// Load in the files and try them out....
		try {			
			while (file.hasNext()) {
				if (result == null) result = new ArrayList<String>();
				ShapefileReader.Record nextRecord = file.nextRecord();
				result.add("Record number: " + nextRecord.toString());
			}
			file.close();
			return result;
		} catch (IOException e) {
			return result;
		}
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

	public void setShpFileReader(ShapefileReader shpFileReader) {
		this.shpFileReader = shpFileReader;
	}
}
