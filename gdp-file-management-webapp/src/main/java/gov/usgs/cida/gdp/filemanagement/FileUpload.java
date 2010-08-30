/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.usgs.cida.gdp.filemanagement;

import gov.usgs.cida.gdp.utilities.FileHelper;
import java.util.List;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.LoggerFactory;

/**
 *
 * @author admin
 */
public class FileUpload {
    private static org.slf4j.Logger log = LoggerFactory.getLogger(FileUpload.class);


    /**
     * Save the uploaded files to a specified directory
     * @param request
     * @param directory
     * @return
     * @throws Exception
     */
    static public String uploadFiles(HttpServletRequest request, String applicationTempDir) throws Exception {
        log.debug("User uploading file(s).");

        // Create a factory for disk-based file items
        FileItemFactory factory = new DiskFileItemFactory();

        // Constructs an instance of this class which
        // uses the supplied factory to create FileItem instances.
        ServletFileUpload upload = new ServletFileUpload(factory);

        Cookie[] cookies = request.getCookies();
        String userDirectory = "";
        for (int cookieIndex = 0;cookieIndex < cookies.length;cookieIndex++) {
        	if ("gdp-user-directory".equals(cookies[cookieIndex].getName().toLowerCase())) {
        		userDirectory = cookies[cookieIndex].getValue();
        	}
        }

        Object interimItems = upload.parseRequest(request);
        @SuppressWarnings("unchecked")
        List<FileItem> items = (List<FileItem>) interimItems;

        // Save the file(s) to the user directory
        if (FileHelper.saveFileItems(applicationTempDir + userDirectory, items)) {
            return userDirectory;
        }
        return "";
    }
}
