package gov.usgs.gdp.servlet;

import gov.usgs.gdp.bean.*;
import gov.usgs.gdp.helper.FileHelper;

import java.io.IOException;
import java.util.*;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.*;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;

/**
 * Servlet implementation class UploadFilesServlet
 * @author isuftin
 *
 */
public class UploadFilesServlet extends HttpServlet {

    private static org.apache.log4j.Logger log = Logger.getLogger(UploadFilesServlet.class);
    private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public UploadFilesServlet() {
        super();
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Long start = new Date().getTime();
        XmlReplyBean xmlOutput = null;
        String command = request.getParameter("command");

        if ("upload".equals(command)) {
            String applicationUserspaceDir = System.getProperty("applicationUserSpaceDir");
            String userDirectory = "";

            try {
                userDirectory = uploadFiles(request, applicationUserspaceDir);
            } catch (Exception e) {
                xmlOutput = new XmlReplyBean(AckBean.ACK_FAIL, new ErrorBean(ErrorBean.ERR_FILE_UPLOAD, e));
                RouterServlet.sendXml(xmlOutput, start, response);
                return;
            }

            if ("".equals(userDirectory)) { // User directory could not be created
                xmlOutput = new XmlReplyBean(AckBean.ACK_FAIL, new ErrorBean(ErrorBean.ERR_USER_DIR_CREATE));
                RouterServlet.sendXml(xmlOutput, start, response);
                return;
            }

            log.debug("Files successfully uploaded.");
            RequestDispatcher rd = request.getRequestDispatcher("/FileSelectionServlet?command=listfiles&userdirectory=" + userDirectory);
            rd.forward(request, response);
        } else if ("createuserdirectory".equals(command)) {
        	String dir = createUserDirectory();
        	
        	XmlReplyBean xmlReply;
        	if ("".equals(dir)) {
        		xmlReply = new XmlReplyBean(AckBean.ACK_FAIL);
        	}
        	else {
        		Cookie c = new Cookie("gdp-user-directory", dir);
        		c.setMaxAge(-1); // set cookie to be deleted when web browser exits
        		c.setPath("/");  // set cookie's visibility to the whole app
        		response.addCookie(c);
        		xmlReply = new XmlReplyBean(AckBean.ACK_OK);
        	}
        	
			RouterServlet.sendXml(xmlReply, start, response);
        }
    }

    /**
     * Save the uploaded files to a specified directory
     * @param request
     * @param directory
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    private String uploadFiles(HttpServletRequest request, String applicationTempDir) throws FileUploadException, Exception  {
        log.debug("User uploading file(s).");

        // Create a factory for disk-based file items
        FileItemFactory factory = new DiskFileItemFactory();

        // Constructs an instance of this class which
        // uses the supplied factory to create FileItem instances.
        ServletFileUpload upload = new ServletFileUpload(factory);
        
        // TODO: don't hardcode for second cookie
        String userDirectory = request.getCookies()[1].getValue();

        Object interimItems = upload.parseRequest(request);
        List<FileItem> items = (List<FileItem>) interimItems;

        // Save the file(s) to the user directory
        if (FileHelper.saveFileItems(applicationTempDir + userDirectory, items)) {
            return userDirectory;
        }
        return "";
    }

    /**
     * Creates a directory for the user at a specified path
     *
     * @param path The path the user directory should have
     * @return The user directory created
     */
    private String createUserDirectory(String path) {
        String applicationUserSpaceDir = System.getProperty("applicationUserSpaceDir");
        String seperator = FileHelper.getSeparator();
        String userTempDir = applicationUserSpaceDir + seperator + path;
        boolean wasCreated = FileHelper.createDir(userTempDir);
        if (wasCreated) {
            log.debug("User subdirectory created at: " + userTempDir);
            return path;
        }

        log.debug("User subdirectory could not be created at: " + path);
        log.debug("User will be unable to upload files for this session.");
        return "";
    }

    /**
     * Creates a unique user directory
     *
     * @return The user directory created
     */
    private String createUserDirectory() {
        String userSubDir = Long.toString(new Date().getTime());
        return createUserDirectory(userSubDir);
    }
}
