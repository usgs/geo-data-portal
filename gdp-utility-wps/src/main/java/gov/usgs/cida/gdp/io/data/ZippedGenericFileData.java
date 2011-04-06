package gov.usgs.cida.gdp.io.data;

import org.n52.wps.io.data.GenericFileData;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.log4j.Logger;
import org.n52.wps.io.data.GenericFileDataConstants;

/**
 * This class extends the GenericFileData class so we can unzip files with arbitrary
 * directory depths.  
 * 
 * @author isuftin
 */
public class ZippedGenericFileData extends GenericFileData {

    private static Logger LOGGER = Logger.getLogger(ZippedGenericFileData.class);

    public ZippedGenericFileData(InputStream stream, String mimeType) {
        super(stream, mimeType);
    }

    @Override
    public String writeData(File workspaceDir) {
        String fileName = null;
        if (GenericFileDataConstants.getIncludeFilesByMimeType(this.mimeType) != null) {
            try {
                fileName = unzipData(this.dataStream, this.fileExtension, workspaceDir);
            } catch (IOException e) {
                LOGGER.error("Could not unzip the archive to " + workspaceDir);
            }
        } else {
            try {
                fileName = justWriteData(this.dataStream, this.fileExtension, workspaceDir);
            } catch (IOException e) {
                LOGGER.error("Could not write the input to " + workspaceDir);
            }
        }
        return fileName;
    }

    private String justWriteData(InputStream is, String extension, File writeDirectory) throws IOException {
        int bufferLength = 2048;
        byte buffer[] = new byte[bufferLength];
        String fileName = null;
        String baseFileName = new Long(System.currentTimeMillis()).toString();

        fileName = baseFileName + "." + extension;
        File currentFile = new File(writeDirectory, fileName);
        currentFile.createNewFile();

        fileName = currentFile.getAbsolutePath();

        FileOutputStream fos = new FileOutputStream(currentFile);
        BufferedOutputStream bos = new BufferedOutputStream(fos, bufferLength);

        int cnt;
        while ((cnt = is.read(buffer, 0, bufferLength)) != -1) {
            bos.write(buffer, 0, cnt);
        }

        bos.flush();
        bos.close();

        System.gc();

        return fileName;
    }

    private String unzipData(InputStream is, String extension, File writeDirectory) throws IOException {
        int bufferLength = 2048;
        byte buffer[] = new byte[bufferLength];
        String baseFileName = new Long(System.currentTimeMillis()).toString();

        ZipInputStream zipInputStream = new ZipInputStream(
                new BufferedInputStream(is));
        ZipEntry entry;

        String returnFile = null;

        while ((entry = zipInputStream.getNextEntry()) != null) {
            String currentExtension = entry.getName();
            if (!currentExtension.endsWith(File.separator)) {  
                int beginIndex = currentExtension.lastIndexOf(".") + 1;
                currentExtension = currentExtension.substring(beginIndex);
             
                String fileName = baseFileName + "." + currentExtension;
                File currentFile = new File(writeDirectory, fileName);
            
                currentFile.createNewFile();
                FileOutputStream fos = new FileOutputStream(currentFile);
                BufferedOutputStream bos = new BufferedOutputStream(fos,
                        bufferLength);

                int cnt;
                while ((cnt = zipInputStream.read(buffer, 0, bufferLength)) != -1) {
                    bos.write(buffer, 0, cnt);
                }

                bos.flush();
                bos.close();

                if (currentExtension.equalsIgnoreCase(extension)) {
                    returnFile = currentFile.getAbsolutePath();
                }
            }
            System.gc();
        }
        zipInputStream.close();
        return returnFile;
    }
}
