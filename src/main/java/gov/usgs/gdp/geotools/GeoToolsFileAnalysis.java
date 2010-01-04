package gov.usgs.gdp.geotools;

import gov.usgs.gdp.io.FileHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.geotools.data.shapefile.dbf.DbaseFileHeader;
import org.geotools.data.shapefile.dbf.DbaseFileReader;

public class GeoToolsFileAnalysis {
	DbaseFileReader fileReader;
	
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
	 * @param fileReader
	 * @return null if no internal DbaseFileReader object is defined
	 */
	public DbaseFileHeader getDBaseFileHeader() {
		DbaseFileHeader result = null;
		
		DbaseFileReader localFileReader = getFileReader();
		if (localFileReader != null) result = getDBaseFileHeader(localFileReader);
		
		return result;
	}
	
	public List<String> getDBaseFileSummary() {
		if (getFileReader() == null) return null; 
		return GeoToolsFileAnalysis.getDBaseFileSummary(getFileReader());
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
	 * Pull a DBase File Header object from a passed in DbaseFileReader object
	 * @param fileReader
	 * @return
	 */
	public DbaseFileHeader getDBaseFileHeader(DbaseFileReader localFileReader) {
		DbaseFileHeader result = null;
		
		result = localFileReader.getHeader();
		
		return result;
	}

	public DbaseFileReader getFileReader() {
		return this.fileReader;
	}

	public void setFileReader(DbaseFileReader localFileReader) {
		this.fileReader = localFileReader;
	}
}
