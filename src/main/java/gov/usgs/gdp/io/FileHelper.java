package gov.usgs.gdp.io;


import gov.usgs.gdp.analysis.GeoToolsFileAnalysis;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.geotools.data.FileDataStore;

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
		result = (new File(directory)).mkdir();
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
	
	public static boolean deleteFile(File file) {
		return FileUtils.deleteQuietly(file);
	}
	
	public static File findFile(String file, String rootPath) {
		File result = null;
		Collection<File> fileCollection = FileUtils.listFiles(new File(rootPath), new String[] {file.substring(file.indexOf('.') + 1)}, true);
		if (fileCollection.isEmpty()) return result;
		Iterator<File> fileCollectionIterator = fileCollection.iterator();
		while (fileCollectionIterator.hasNext()) {
			File testFile = fileCollectionIterator.next();
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
		if (filePath == null)
			return null;
		List<String> result = null;
		Collection<File> fileList = null;
		try {
			fileList = FileUtils.listFiles((new File(filePath)), extensions,
					recursive);
		} catch (IllegalArgumentException e) {
			return null;
		}
		result = new ArrayList<String>();
		
		for (File file : fileList) {
			result.add(file.getName());
		}
		
		return result;
	}
	
	/**
	 * Returns a Collection of type File
	 * 
	 * @param filePath
	 * @param recursive
	 * @return
	 */
	public static Collection<File> getFileCollection(String filePath, boolean recursive) {
		Collection<File> result = null;
		
		result = FileHelper.getFileCollection(filePath, null, recursive);
		
		return result;
	}

	/**
	 * Returns a Collection of type File
	 * 
	 * @param filePath
	 * @param extensions
	 * @param recursive
	 * @return
	 */
	public static Collection<File> getFileCollection(String filePath, String[] extensions, boolean recursive) {
		if (filePath == null) return null;
		Collection<File> result = null;
		try {
			result = FileUtils.listFiles((new File(filePath)), extensions,
					recursive);
		} catch (IllegalArgumentException e) {
			return null;
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

	public static List<FileDataStore> getShapeFileDataStores(
			List<String> shpFiles) {
		List<FileDataStore> result = new ArrayList<FileDataStore>();
		for (String file : shpFiles) {
			FileDataStore fds = GeoToolsFileAnalysis.getFileDataStore(new File(file.trim()));
			if (fds != null) result.add(fds);
		}
		return result;
	}

	
}