package gov.usgs.cida.gdp.utilities;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.slf4j.LoggerFactory;

/**
 * Utility class that helps with multiple FileIO operations
 * 
 * @author isuftin
 *
 */
public class FileHelper {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(FileHelper.class);

    /**
     * @see FileHelper#base64Encode(byte[]) 
     * @param input
     * @return
     * @throws IOException
     */
    public static byte[] base64Encode(final File input) throws IOException {
        byte[] result = null;

        result = FileHelper.base64Encode(FileHelper.getByteArrayFromFile(input));

        return result;
    }

    /**
     * Provides Base64 encoding and decoding as defined by RFC 2045.
     * 
     * @param input
     * @return
     */
    public static byte[] base64Encode(final byte[] input) {
        if (input == null) {
            return (byte[]) Array.newInstance(byte.class, 0);
        }
        byte[] result = null;

        Base64 encoder = new Base64();
        result = encoder.encode(input);

        return result;
    }

    /**
     * Reads a file into a byte array
     *
     * @param file
     * @return
     * @throws IOException
     */
    public static byte[] getByteArrayFromFile(File file) throws IOException {
        if (file == null) return (byte[]) Array.newInstance(byte.class, 0);
        InputStream is = new FileInputStream(file);
        
        try {
            // Get the size of the file
            long length = file.length();

            // Maximum size of file cannot be larger than the Integer.MAX_VALUE
            if (length > Integer.MAX_VALUE) {
                throw new IOException("File is too large: " + file.length() + " bytes. Maximum length: " + Integer.MAX_VALUE);
            }

            // Create the byte array to hold the data
            byte[] bytes = new byte[(int) length];

            // Read in the bytes
            int offset = 0;
            int numRead = 0;
            while (offset < bytes.length
                    && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
                offset += numRead;
            }

            // Ensure all the bytes have been read in
            if (offset < bytes.length) {
                throw new IOException("Could not completely read file " + file.getName());
            }


            return bytes;
        } finally {
            // Close the input stream and return bytes
            is.close();
        }
    }

    /**
     * Performs a safe renaming of a file. First copies old file to new file, then if new file exists, removes old file.
     *
     * @param fromFile
     * @param toFileName
     * @return true if succeeded, false if not
     * @throws IOException
     */
    public static boolean renameFile(final File fromFile, final String toFileName) throws IOException {
        File toFile = new File(fromFile.getParent() + File.separator + toFileName);
        FileUtils.copyFile(fromFile, toFile);
        if (!toFile.exists()) {
            return false;
        }
        return fromFile.delete();
    }

    public static boolean copyFileToFile(final File inFile, final String outFileString) throws IOException {
        return FileHelper.copyFileToFile(inFile, outFileString, false);
    }

    /**
     * Copies a File object to a given location
     * Is able to handle
     *
     * @param inFile
     * @param outFileString
     * @param deleteOriginalFile - effectively makes this function as a MOVE command instead of a COPY command
     * @return
     * @throws IOException
     */
    public static boolean copyFileToFile(final File inFile, final String outPath, boolean deleteOriginalFile) throws IOException {
        if (inFile.isDirectory()) {
            FileUtils.copyDirectory(inFile, (new File(outPath + File.separator + inFile.getName())));
        } else {
            FileUtils.copyFile(inFile, (new File(outPath + File.separator + inFile.getName())));
        }
        if (deleteOriginalFile) {
            FileUtils.deleteQuietly(inFile);
        }
        return true;
    }

    /**
     * Delete files older than a given Long instance
     * @param directory directory within which to search. 
     * @param cutoffTime
     * @return
     */
    /**
     * Delete files older than a given Long instance
     * 
     * @param directory Directory within which to search.
     * @param cutoffTime
     * @param deleteDirectory Also delete the directory given in the directory param
     * @return
     */
    public static Collection<File> wipeOldFiles(File directory, Long cutoffTime, boolean deleteDirectory) {
        if (directory == null || !directory.exists()) {
            return new ArrayList<File>();
        }

        Collection<File> result = new ArrayList<File>();
        Collection<File> oldFiles = FileHelper.getFilesOlderThan(directory, cutoffTime, Boolean.TRUE);
        for (File file : oldFiles) {
            String logString = "Deleting File: \"" + file.toString() + "\" ... ";

            if (file.canWrite() && file.delete()) {
                logString += "done. ";
                result.add(file);
                if (file.getParentFile().isDirectory()) {
                    if (file.getParentFile() != directory && file.getParentFile().delete()) {
                        log.info("Deleting Directory: \"" + file.getParent() + "\" ...  done");
                    } else if (file.getParentFile() == directory && deleteDirectory) {
                        log.info("Deleting Directory: \"" + file.getParent() + "\" ...  done");
                    }
                }
            } else {
                logString += "FAILED!";
            }
            log.info(logString);
        }

        return result;
    }

    /**
     * Create a repository directory structure
     * @param baseFilePath - point in the fs at which to begin structuring the repository directory from
     * @return
     */
    public static File createFileRepositoryDirectory(final String baseFilePath) {
        String basePath = baseFilePath + System.getProperty("file.separator");
        String directoryName = PropertyFactory.getProperty("upload.directory.name");
        if (baseFilePath == null) {
            return null;
        }
        if (directoryName == null || "".equals(directoryName)) {
            directoryName = "upload-repository";
        }
        String directory = basePath + directoryName;
        if (FileHelper.doesDirectoryOrFileExist(directory)) {
            return new File(directory);
        }

        FileHelper.createDir(directory);

        File result = new File(directory);
        if (!result.exists()) {
            return null;
        }
        return result;
    }

    public static boolean createDir(File directory) {
        return FileHelper.createDir(directory.toString());
    }

    /**
     * Creates a directory in the filesystem
     *
     * @param directory
     * @param removeAtSysExit
     * @return boolean true if already exists or created, false if directory could not be created
     */
    public static boolean createDir(String directory) {
        boolean result = false;
        if (FileHelper.doesDirectoryOrFileExist(directory)) {
            return true;
        }
        result = new File(directory).mkdirs();
        return result;
    }

    /**
     * Get the types of files that are available to output to the user
     * @return
     */
    public static List<String> getOutputFileTypesAvailable() {
        return PropertyFactory.getValueList("out.file.type");
    }

    /**
     * Recursively deletes a directory from the filesystem.
     * @param directory
     * @return
     */
    public static boolean deleteDirRecursively(File directory) throws IOException {
        if (!directory.exists()) {
            return false;
        }
        FileUtils.deleteDirectory(directory);
        return true;
    }

    /**
     * Recursively deletes a directory from the filesystem.
     * @param directory
     * @return
     */
    public static boolean deleteDirRecursively(String directory) throws IOException {
        boolean result = false;
        File dir = new File(directory);
        if (!dir.exists()) {
            return false;
        }
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
        if ("".equals(filePath)) {
            return false;
        }
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
        if (file == null) {
            return false;
        }
        return file.delete();
    }

    /**
     * Tests whether or not a directory or file exists given the passed String
     * representing a file/directory location
     * 
     * @param filePath
     * @return
     */
    public static boolean doesDirectoryOrFileExist(String filePath) {
        return new File(filePath).exists();
    }

    @SuppressWarnings("unchecked")
    public static File findFile(String file, String rootPath) {
        File result = null;
        Collection<File> fileCollection = FileUtils.listFiles(new File(rootPath), new String[]{file.substring(file.lastIndexOf('.') + 1)}, true);
        if (fileCollection.isEmpty()) {
            return result;
        }
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
    public static List<String> getFileList(String filePath, boolean recursive) throws IllegalArgumentException {
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
        if (filePath == null) {
            return null;
        }
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
        if (filePath == null) {
            return null;
        }

        Collection<File> result = null;
        Object interimResult = FileUtils.listFiles((new File(filePath)), extensions, recursive);
        if (interimResult instanceof Collection<?>) {
            result = (Collection<File>) interimResult;
        }
        return result;
    }

    /**
     * @see System.getProperty("java.io.tmpdir")
     * @return
     */
    public static String getSystemTemp() {
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
    public static boolean saveFileItems(String directory, List<FileItem> items) throws Exception {
        //TODO: Figure out a good way of testing this function

        // Process the uploaded items
        Iterator<FileItem> iter = items.iterator();

        // Check for upload directory existence. Create if it does not exist. 
        FileHelper.createDir(directory);

        while (iter.hasNext()) {
            FileItem item = iter.next();

            // This substring process is a fix for Windows uploaders
            // This gets only the filename from a full pathname. For some reason, Windows uploads includes the entire pathname (C:\something\something\filename)
            String fileName = (item.getName() != null && item.getName().contains("\\")) ? item.getName().substring(item.getName().lastIndexOf("\\") + 1) : item.getName();

            String tempFile = directory + java.io.File.separator + fileName;
            if (fileName != null && !"".equals(fileName)) {
                File uploadedFile = new File(tempFile);
                item.write(uploadedFile);
                if (fileName.toLowerCase().contains(".zip")) {
                    unzipFile(directory, uploadedFile);
                    uploadedFile.delete();
                }
            }
        }
        return true;
    }

    /**
     * Takes a zip file and unzips it to a outputDirectory
     * 
     * @param outputDirectory
     * @param zipFile
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static boolean unzipFile(String outputDirectory, File zipFile) throws FileNotFoundException, IOException {
        FileInputStream fis = new FileInputStream(zipFile);
        ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
        ZipEntry entry = null;
        BufferedOutputStream dest = null;

        final int BUFFER = 2048;
        while ((entry = zis.getNextEntry()) != null) {
            String fileName = entry.getName();
            log.debug("Unzipping: " + entry.getName());

            int count = 0;
            byte data[] = new byte[BUFFER];
            // Get the final filename (even if it's within directories in the ZIP file)
            String destinationFileName = entry.getName().contains("/") ? entry.getName().substring(entry.getName().lastIndexOf('/')) : entry.getName();
            FileOutputStream fos = new FileOutputStream(outputDirectory + java.io.File.separator + destinationFileName);
            dest = new BufferedOutputStream(fos, BUFFER);
            while ((count = zis.read(data, 0, BUFFER)) != -1) {
                dest.write(data, 0, count);
            }
            dest.flush();
            dest.close();
        }
        zis.close();
        return true;
    }

    /**
     * Creates a unique user directory
     *
     * @return The user directory created
     */
    public static String createUserDirectory(String applicationUserSpaceDir) {
        String userSubDir = Long.toString(new Date().getTime());

        //String applicationUserSpaceDir = System.getProperty("applicationUserSpaceDir");
        String seperator = File.separator;
        String userTempDir = applicationUserSpaceDir + seperator + userSubDir;
        boolean wasCreated = FileHelper.createDir(userTempDir);
        if (wasCreated) {
            log.debug("User subdirectory created at: " + userTempDir);
            return userSubDir;
        }

        log.debug("User subdirectory could not be created at: " + userSubDir);
        log.debug("User will be unable to upload files for this session.");
        return "";
    }

    public static boolean updateTimestamp(final String path, final boolean recursive) throws IOException {
        if (path == null || "".equals(path)) {
            return false;
        }
        if (!FileHelper.doesDirectoryOrFileExist(path)) {
            return false;
        }

        FileUtils.touch(new File(path));
        if (recursive) {
            @SuppressWarnings("unchecked")
            Iterator<File> files = FileUtils.iterateFiles(new File(path), null, true);
            while (files.hasNext()) {
                File file = files.next();
                FileUtils.touch(file); // update date on file
            }
        }
        return true;
    }

    /**
     * Returns files and directories older that a specified date
     *
     * @param filePath System path to the directory
     * @param msPerDay
     * @param recursive
     * @return
     */
    @SuppressWarnings("unchecked")
    static Collection<File> getFilesOlderThan(File filePath, Long age, Boolean recursive) {
        if (filePath == null || !filePath.exists()) {
            return new ArrayList<File>();
        }
        Iterator<File> files = null;

        if (recursive.booleanValue()) {
            files = FileUtils.iterateFiles(filePath, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
        } else {
            files = FileUtils.iterateFiles(filePath, TrueFileFilter.INSTANCE, null);
        }

        Collection<File> result = new ArrayList<File>();
        Date date = new Date();
        while (files.hasNext()) {
            File file = files.next();

            if (file.lastModified() < date.getTime() - age.longValue()) {
                result.add(file);
            }
        }

        return result;
    }
}
