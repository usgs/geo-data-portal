package gov.usgs.gdp.helper;


import gov.usgs.gdp.analysis.GeoToolsFileAnalysis;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileUtils;
import org.geotools.data.FileDataStore;

/**
 * Utility class that helps with multiple FileIO operations
 * 
 * @author isuftin
 *
 */
public class FileHelper {

	/**
	 * Copies a File object to a given location
	 * Is able to handle 
	 * 
	 * @param inFile
	 * @param outFileString
	 * @return
	 * @throws IOException 
	 */
	public static  boolean copyFileToFile(final File inFile, final String outFileString) throws IOException {
		if (inFile.isDirectory()) {
			FileUtils.copyDirectory(inFile, (new File(outFileString + inFile.getName())));
		} else {
			FileUtils.copyFile(inFile, (new File(outFileString + inFile.getName())));
		}
		return true;
	}
	
	public static File createFileRepositoryDirectory(final String baseFilePath) {
		String basePath = baseFilePath;
		String directoryName = PropertyFactory.getProperty("upload.directory.name");
		if (basePath == null) basePath = "";
		if (directoryName == null || "".equals(directoryName)) directoryName = "upload-repository";
		String directory = basePath + directoryName;
		if (!FileHelper.createDir(directory)) return null;
		File result = new File(directory);
		if (!result.exists()) return null;
		return result;
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
		result = new File(directory).mkdirs();
		return result;
	}
	
	/**
	 * Get the types of files that are available to output to the user
	 * @return
	 */
	public static List<String> getOutputFileTypesAvailable() {
		List<String> fileTypes = PropertyFactory.getValueList("out.file.type");
		return fileTypes;
	}
	
	/**
	 * Recursively deletes a directory from the filesystem.
	 * @param directory
	 * @return
	 */
	public static  boolean deleteDirRecursively(File directory) throws IOException {
			if (!directory.exists()) return false;
			FileUtils.deleteDirectory(directory);
			return true;
	}

	/**
	 * Recursively deletes a directory from the filesystem.
	 * @param directory
	 * @return
	 */
	public static  boolean deleteDirRecursively(String directory) throws IOException {
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
	public static boolean deleteFileQuietly(String filePath) {
		return FileUtils.deleteQuietly(new File(filePath));
	}
	
	public static boolean deleteFileQuietly(File file) {
		return FileUtils.deleteQuietly(file);
	}
	
	/**
	 * @see FileHelper.deleteFile
	 * 
	 * @param filePath
	 * @return
	 * @throws SecurityException
	 */
	public static boolean deleteFile(String filePath) throws SecurityException {
		if ("".equals(filePath)) return false;
		return FileHelper.deleteFile(new File(filePath));
	}
	
	/**
	 * Deletes a file from the file system
	 * 
	 * @param file - method returns false if File object passed in was null 
	 * @return true if file was deleted, false if not 
	 * @throws SecurityException
	 */
	public static boolean deleteFile(File file) throws SecurityException {
		if (file == null) return false;
		return file.delete();
	}
	
	public static boolean doesDirectoryOrFileExist(String filePath) {
		boolean result = false;
		File testFile = new File(filePath);
		result = testFile.exists();
		return result;
	}
	
	@SuppressWarnings("unchecked")
	public static File findFile(String file, String rootPath) {
		File result = null;
		Collection<File> fileCollection = FileUtils.listFiles(new File(rootPath), new String[] {file.substring(file.lastIndexOf('.') + 1)}, true);
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
	public static List<String> getFileList(String filePath, boolean recursive) throws IllegalArgumentException{
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
	public static List<String> getFileList(String filePath, String[] extensions, boolean recursive) throws IllegalArgumentException {
		if (filePath == null)
			return null;
		List<String> result = null;
		Collection<File> fileList = null;
		fileList = FileUtils.listFiles((new File(filePath)), extensions, recursive);
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
	@SuppressWarnings("unchecked")
	public static Collection<File> getFileCollection(String filePath, boolean recursive) throws IllegalArgumentException {
		return (Collection<File>) FileHelper.getFileCollection(filePath, null, recursive);
	}

	/**
	 * Returns a Collection of type File
	 * 
	 * @param filePath
	 * @param extensions
	 * @param recursive
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Collection<?> getFileCollection(String filePath, String[] extensions, boolean recursive) throws IllegalArgumentException {
		if (filePath == null) return null;
		Collection<File> result = null;		
		Object interimResult = FileUtils.listFiles((new File(filePath)), extensions, recursive);
		if (interimResult instanceof Collection<?>) {
			result = (Collection<File>) interimResult;
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

	/**
	 * Saves a List of type FileItem to a directory
	 * 
	 * @param directory
	 * @param items
	 * @return
	 * @throws Exception
	 */
	public static boolean saveFileItems(String directory, List<FileItem> items) throws Exception{
		// Process the uploaded items
		Iterator<FileItem> iter = items.iterator();
		while (iter.hasNext()) {
			FileItem item = iter.next();
			
		    String fileName = item.getName();
		    String tempFile = directory + java.io.File.separator + fileName;
		    if (fileName != null && !"".equals(fileName) ) {
			    File uploadedFile = new File(tempFile);
			    item.write(uploadedFile);
		    }
		}
		return true;
	}
	
	public static List<FileDataStore> getShapeFileDataStores(
			List<String> shpFiles) throws IOException {
		List<FileDataStore> result = new ArrayList<FileDataStore>();
		for (String file : shpFiles) {
			FileDataStore fds = GeoToolsFileAnalysis.getFileDataStore(new File(file.trim()));
			if (fds != null) result.add(fds);
		}
		return result;
	}

    public static boolean updateTimestamp(final String path, final boolean recursive) throws IOException {
        if (path == null || "".equals(path)) return false;
        if (!FileHelper.doesDirectoryOrFileExist(path)) return false;
        FileUtils.touch(new File(path));
        if (recursive) {
            Iterator<File> files = FileUtils.iterateFiles(new File(path), null, true);
            while (files.hasNext()) {
                File file =  files.next();
                FileUtils.touch(file);
            }

        }
        return true;
    }

	
}