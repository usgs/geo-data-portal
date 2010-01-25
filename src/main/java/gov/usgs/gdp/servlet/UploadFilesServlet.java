package gov.usgs.gdp.servlet;

import gov.usgs.gdp.bean.ErrorBean;
import gov.usgs.gdp.bean.MessageBean;
import gov.usgs.gdp.bean.FilesBean;
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
	@SuppressWarnings("unchecked")
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String xmlOutput = "";
		String command = (String) request.getSession().getAttribute("command");
		Cookie userDirectory = CookieHelper.getCookie(request, "userDirectory");

		if ("upload".equals(command)) {
			// Create the user directory.
			if (userDirectory == null) {
				userDirectory = createUserDirectory();
				if (userDirectory == null) {
					ErrorBean error = new ErrorBean(ErrorBean.ERR_USER_DIR_CREATE);
					xmlOutput = error.toXml();
					RouterServlet.sendXml(xmlOutput, response);
					return;
				}
			}
			
			boolean filesUploaded;
			try {
				filesUploaded = uploadFiles(request, userDirectory.getValue());
			} catch (Exception e) {
				ErrorBean error = new ErrorBean(ErrorBean.ERR_FILE_UPLOAD);
				error.setException(e);
				xmlOutput = error.toXml();
				RouterServlet.sendXml(xmlOutput, response);
				return;
			}
			if (filesUploaded) {
				log.debug("Files successfully uploaded.");
//					messageBean.addMessage("File(s) successfully uploaded.");
			} else {
				ErrorBean error = new ErrorBean(ErrorBean.ERR_FILE_UPLOAD);
				xmlOutput = error.toXml();
				RouterServlet.sendXml(xmlOutput, response);
				return;
			}
		}
		
		// What is directory name for the files being uploaded
	    //String userDirectory = (String) request.getSession().getAttribute("userTempDir");
		
	    // Pull in the uploaded Files bean from the user's session
	    List<FilesBean> uploadedFilesBean = 
	    	(request.getSession().getAttribute("uploadedFilesBeanList") == null) 
	    	? new ArrayList<FilesBean>() : (List<FilesBean>) request.getSession().getAttribute("uploadedFilesBean");
	    
	    MessageBean errorBean = new MessageBean();
	    MessageBean messageBean = new MessageBean();
	    /*
	    if ("delete".equals(action)) { // Delete Files
	    	String filename = (request.getParameter("file") == null) ? "" : request.getParameter("file");
	    	if ("".equals(filename) || filename == null) {
	    		log.debug("There was no filename passed to be deleted");
	    		errorBean.addMessage("Client did not pass a filename to be deleted. Please try again or contact system administrator for assistance");
	    		request.setAttribute("errorBean", errorBean);
	    		RequestDispatcher rd = request.getRequestDispatcher("/jsp/fileUpload.jsp");
	    		rd.forward(request, response);
	    	} 
	    	
    		try {
	    		if (FileHelper.deleteFile(filename)) {
	    			messageBean.addMessage("File \"" + filename + " was deleted.");
	    			log.debug("File \"" + filename + " was deleted.");
	    		} else {
	    			errorBean.addMessage("File \"" + filename + " could not be deleted (doesn't exist?).");
	    			log.debug("File \"" + filename + " could not be deleted (doesn't exist?).");
	    		}
    		} catch (SecurityException e) {
    			log.debug("Unable to delete file: " + e.getMessage());
				errorBean.addMessage("Unable to delete file: " + e.getMessage());
    		}
	    } else if ("upload".equals(action)){ // Upload files to server
	    	try {
				if (uploadFiles(request, userDirectory)) {
					log.debug("Files successfully uploaded.");
					messageBean.addMessage("File(s) successfully uploaded.");
				} else {
					log.debug("Unable to upload files.");
					errorBean.addMessage("Unable to upload files - No message provided");
				}
			} catch (Exception e) {
				log.debug("Unable to upload files: " + e.getMessage());
				errorBean.addMessage("Unable to upload files: " + e.getMessage());
			}
	    }
	    
	    // Rescan the user directory for updates
	    uploadedFilesBean = populateUploadedFilesBean(userDirectory);
	    
	    // Place the bean in the user's session
		request.getSession().setAttribute("uploadedFilesBeanList", uploadedFilesBean);
		request.setAttribute("errorBean", errorBean);
		request.setAttribute("messageBean", messageBean);*/
		
		// Away we go		
		RequestDispatcher rd = request.getRequestDispatcher("/jsp/fileUpload.jsp");
		rd.forward(request, response);
		
	}

	/** 
	 * Scans the upload directory to build a List of type FilesBean 
	 * @param userDirectory
	 * @return
	 */
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

	private Cookie createUserDirectory() {
		Cookie result = null;
		boolean wasCreated = false;
		String applicationTempDir = System.getProperty("applicationTempDir");
        String seperator = FileHelper.getSeparator();
        String userSubDir = Long.toString(new Date().getTime());
        String userTempDir = applicationTempDir + seperator + userSubDir;
        
        wasCreated = FileHelper.createDir(userTempDir);
        if (wasCreated) {
        	log.debug("User subdirectory created at: " + userTempDir);
			String cookieDaysString = PropertyFactory.getProperty("cookie.lifespan");
			int cookieHours = 0;
			try {
				cookieHours = Integer.parseInt(cookieDaysString);
			} catch (NumberFormatException e) {
				log.debug("Properties did not contain a value for the amount of days a cookie is set for. (\"cookie.lifespan\") Using: 2");
				cookieHours = CookieHelper.ONE_HOUR * 48;
			}
			result = new Cookie("userDirectory", userTempDir);
			result.setMaxAge(cookieHours);
    		return result;
        }
        
    	log.debug("User subdirectory could not be created at: " + userTempDir);
    	log.debug("User will be unable to upload files for this session.");
    	return null;
	}
}
