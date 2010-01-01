package gov.usgs.gdp.io;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class FileHelper {

	public static File loadFile(String filePath) {
		File result = null;
		
		result = new File(filePath);
		
		return result;
	}

	/**
	 * Recursively deletes a directory from the filesystem.
	 * @param directory
	 * @return
	 */
	public static  boolean deleteDirRecursively(String directory) {
		boolean result = false;
		File dir = new File(directory);
		result = FileHelper.deleteDirRecursively(dir);
		return result;
	}
	
	/**
	 * Recursively deletes a directory from the filesystem.
	 * @param directory
	 * @return
	 */
	public static  boolean deleteDirRecursively(File directory) {
		try {
			FileUtils.deleteDirectory(directory);
		} catch (IOException e) {
			return false;
		}
		// The directory is now empty so delete it
		return true;
	}

	/**
	 * Copies a File object to a given location
	 * Is able to handle 
	 * 
	 * @param inFile
	 * @param outFileString
	 * @return
	 */
	public static  boolean copyFileToFile(final File inFile, final String outFileString) {
		try {
			if (inFile.isDirectory()) {
				FileUtils.copyDirectory(inFile, (new File(outFileString + inFile.getName())));
			} else {
				FileUtils.copyFile(inFile, (new File(outFileString + inFile.getName())));
			}
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	/**
	 * Creates a directory in the filesystem
	 * 
	 * @param directory
	 * @param removeAtSysExit
	 * @return
	 */
	public  static  boolean  createDir(String directory) {
		boolean result = false;
		try {
			result = (new File(directory)).mkdir();
		} catch (SecurityException e) {
			System.out.println("Could not create directory: " + e.getMessage());
		}
		return result;
	}
	
	/**
	 * @see System.getProperty("java.io.tmpdir")
	 * @return
	 */
	public static  String getSystemTemp() {
		String result = "";
		
		result = System.getProperty("java.io.tmpdir");
		
		return result;
	}
	
	/**
	 * @see java.io.File.pathSeparator
	 * @return
	 */
	public static  String getSystemPathSeparator() {
		String result = "";
		
		result = java.io.File.pathSeparator;
		
		return result;
	}
	
	/**
	 * @see java.io.File.separator
	 * @return
	 */
	public static  String getSeparator() {
		String result = "";
		
		result = java.io.File.separator;
		
		return result;
		
	}
	
}