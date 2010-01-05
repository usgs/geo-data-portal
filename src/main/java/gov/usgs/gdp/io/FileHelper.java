package gov.usgs.gdp.io;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;

public class FileHelper {

	
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
	 * Recursively deletes a directory from the filesystem.
	 * @param directory
	 * @return
	 */
	public static  boolean deleteDirRecursively(File directory) {
		try {
			if (!directory.exists()) return false;
			FileUtils.deleteDirectory(directory);
			return true;
		} catch (IOException e) {
			return false;
		} catch (IllegalArgumentException e1) {
			return false;
		}

	}

	/**
	 * Recursively deletes a directory from the filesystem.
	 * @param directory
	 * @return
	 */
	public static  boolean deleteDirRecursively(String directory) {
		boolean result = false;
		File dir = new File(directory);
		if (!dir.exists()) return false;
		result = FileHelper.deleteDirRecursively(dir);
		return result;
	}
	
	/**
	 * Deletes a file.
	 * 
	 * @param filePath
	 * @return
	 */
	public static boolean deleteFile(String filePath) {
		return FileUtils.deleteQuietly(new File(filePath));
	}
	
	public static File findFile(String file, String rootPath) {
		File result = null;
		Collection fileCollection = FileUtils.listFiles(new File(rootPath), new String[] {file.substring(file.indexOf('.') + 1)}, true);
		if (fileCollection.isEmpty()) return result;
		Iterator fileCollectionIterator = fileCollection.iterator();
		while (fileCollectionIterator.hasNext()) {
			File testFile = (File) fileCollectionIterator.next();
			if (file.equals(testFile.getName())) {
				result = testFile;
			}
		}
		return result;
	}
	
	/**
	 * Get recursive directory listing
	 * 
	 * @param filePath
	 * @return
	 */
	public static List<String> getFileList(String filePath, boolean recursive) {
		List<String> result = null;
		
		result = FileHelper.getFileList(filePath, null, recursive);
		
		return result;
	}

	/**
	 * Get recursive directory listing
	 * 
	 * @param filePath
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<String> getFileList(String filePath, String[] extensions, boolean recursive) {
		if (filePath == null) return null;
		List<String> result = null;
		Collection<File> fileList = FileUtils.listFiles((new File(filePath)), extensions, recursive);
		if (fileList != null) {
			result = new ArrayList<String>();
			for (File file : fileList) {
				result.add(file.getName());
			}
		}
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
	 * @see System.getProperty("java.io.tmpdir")
	 * @return
	 */
	public static  String getSystemTemp() {
		String result = "";
		
		result = System.getProperty("java.io.tmpdir");
		
		return result;
	}
	
	public static File loadFile(String filePath) {
		File result = null;
		
		result = new File(filePath);
		
		return result;
	}
	
}