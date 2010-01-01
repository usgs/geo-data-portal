package gov.usgs.gdp.geotools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

import org.geotools.data.shapefile.dbf.DbaseFileHeader;
import org.geotools.data.shapefile.dbf.DbaseFileReader;

public class FileAnalysis {
	DbaseFileReader fileReader;
	
	public FileAnalysis() {
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
	
	/**
	 * Pull a DBase File Header object from a passed in DbaseFileReader object
	 * @param fileReader
	 * @return
	 */
	private DbaseFileHeader getDBaseFileHeader(DbaseFileReader localFileReader) {
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
