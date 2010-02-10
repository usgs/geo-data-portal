package gov.usgs.gdp.servlet;

import gov.usgs.gdp.bean.AckBean;
import gov.usgs.gdp.bean.ErrorBean;
import gov.usgs.gdp.bean.FilesBean;
import gov.usgs.gdp.bean.XmlReplyBean;
import gov.usgs.gdp.helper.CookieHelper;
import gov.usgs.gdp.helper.FileHelper;
import gov.usgs.gdp.helper.PropertyFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
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
		XmlReplyBean xmlOutput = null;
		String command = request.getParameter("command");
		String userDirectory = request.getParameter("userdirectory");
		//Cookie userDirectory = CookieHelper.getCookie(request, "userDirectory");
		
		if ("upload".equals(command)) {
			// Create the user directory.
			if (userDirectory == null || !FileHelper.doesDirectoryOrFileExist(userDirectory)) {
				String userSubDir = createUserDirectory();
				if ("".equals(userSubDir)) {
					xmlOutput = new XmlReplyBean(AckBean.ACK_FAIL, new ErrorBean(ErrorBean.ERR_USER_DIR_CREATE));
					RouterServlet.sendXml(xmlOutput, response);
					return;
				}
				String applicationTempDir = System.getProperty("applicationTempDir");
		        String seperator = FileHelper.getSeparator();
				userDirectory = applicationTempDir + seperator + userSubDir;
			}
			
			boolean filesUploaded = false;
			try {
				filesUploaded = uploadFiles(request, userDirectory);
			} catch (Exception e) {
				xmlOutput = new XmlReplyBean(AckBean.ACK_FAIL, new ErrorBean(ErrorBean.ERR_FILE_UPLOAD, e));
				RouterServlet.sendXml(xmlOutput, response);
				return;
			}
			
			if (filesUploaded) {
				log.debug("Files successfully uploaded.");
				RequestDispatcher rd = request.getRequestDispatcher("/FileSelectionServlet?command=listfiles&userdirectory=" + userDirectory);
				rd.forward(request, response);
			} else {
				xmlOutput = new XmlReplyBean(AckBean.ACK_FAIL, new ErrorBean(ErrorBean.ERR_FILE_UPLOAD));
				return;
			}
		}
	}

	/** 
	 * Scans the upload directory to build a List of type FilesBean 
	 * @param userDirectory
	 * @return
	 */
	@SuppressWarnings("unused")
	private List<FilesBean> populateUploadedFilesBean(String userDirectory) {
		List<FilesBean> result = new ArrayList<FilesBean>();
		Collection<File> uploadedFiles = FileHelper.getFileCollection(userDirectory, true);
		result = FilesBean.getFilesBeanSetList(uploadedFiles);
		return result;
	}

	/** 
	 * Save the uploaded files to a specified directory
	 * @param request
	 * @param directory
	 * @return
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	private boolean uploadFiles(HttpServletRequest request, String uploadDirectory) throws Exception{
		log.debug("User uploading file(s).");
		
		boolean result = false;
		
		// Utility method that determines whether the request contains multipart content (files)
		boolean isMultiPart = ServletFileUpload.isMultipartContent(request);
		if (!isMultiPart) {
			log.debug("ServletFileUpload.isMultipartContent(request) was false. Could not upload files.");
			return false;
		}
		
		// Create a factory for disk-based file items
		FileItemFactory factory = new DiskFileItemFactory();

		// Constructs an instance of this class which
		// uses the supplied factory to create FileItem instances. 
		ServletFileUpload upload = new ServletFileUpload(factory);
		
		List<FileItem> items = null;
		
    	Object interimItems = upload.parseRequest(request);
    	if (interimItems instanceof List<?>) {
    		items = (List<FileItem>) interimItems;
    	}
		
		result = FileHelper.saveFileItems(uploadDirectory, items);
		
		return result;
	}

	private String createUserDirectory() {
		String applicationTempDir = System.getProperty("applicationTempDir");
        String seperator = FileHelper.getSeparator();
        String userSubDir = Long.toString(new Date().getTime());
        String userTempDir = applicationTempDir + seperator + userSubDir;
        
        boolean  wasCreated = FileHelper.createDir(userTempDir);
        if (wasCreated) {
        	log.debug("User subdirectory created at: " + userTempDir);
			return userSubDir;
        }
        
    	log.debug("User subdirectory could not be created at: " + userTempDir);
    	log.debug("User will be unable to upload files for this session.");
    	return "";
	}
}
