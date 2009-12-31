package gov.usgs.gdp.io;

import java.io.File;

public class FileHelper {

	/**
	 * Deletes a directory in the filesystem<br />
	 * Directory needs to be empty.
	 * @param directory
	 * @return 
	 */
	public static final boolean deleteDir(String directory) {
		boolean result = false;
		File dir = new File(directory);
		result = FileHelper.deleteDir(dir);
		return result;
	}
	
	public static final boolean deleteDir(File directory) {
		boolean result = false;
		 if (directory.isDirectory()) {
			 result = directory.delete();
		 }
		return result;
	}
	
	/**
	 * Recursively deletes a directory from the filesystem.
	 * @param directory
	 * @return
	 */
	public static final boolean deleteDirRecursively(String directory) {
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
	public static final boolean deleteDirRecursively(File directory) {
		if (directory.isDirectory()) {
		       String[] children = directory.list();
		       for (int i=0; i<children.length; i++) {
		           boolean success = FileHelper.deleteDirRecursively(new File(directory, children[i]));
		           if (!success) {
		               return false;
		           }
		       }
		   }
		
		// The directory is now empty so delete it
		return directory.delete();
	}
	
	/**
	 * Creates a directory in the filesystem
	 * @param directory
	 * @return
	 */
	public  static final boolean  createDir(String directory) {
		boolean result = false;
		result = (new File(directory)).mkdir();
		return result;
	}
	
	/**
	 * @see System.getProperty("java.io.tmpdir")
	 * @return
	 */
	public static final String getSystemTemp() {
		String result = "";
		
		result = System.getProperty("java.io.tmpdir");
		
		return result;
	}
	
	/**
	 * @see java.io.File.pathSeparator
	 * @return
	 */
	public static final String getSystemPathSeparator() {
		String result = "";
		
		result = java.io.File.pathSeparator;
		
		return result;
	}
	
	/**
	 * @see java.io.File.separator
	 * @return
	 */
	public static final String getSeparator() {
		String result = "";
		
		result = java.io.File.separator;
		
		return result;
		
	}
	
}