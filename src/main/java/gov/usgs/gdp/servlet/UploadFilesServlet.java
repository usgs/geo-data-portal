package gov.usgs.gdp.servlet;

import gov.usgs.gdp.bean.MessageBean;
import gov.usgs.gdp.bean.FilesBean;
import gov.usgs.gdp.io.FileHelper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
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
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@SuppressWarnings("unchecked")
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		// Pull in parameters
		String action 	= (request.getParameter("action") == null) ? "" : request.getParameter("action").toLowerCase();
		
		// What is directory name for the files being uploaded
	    String userDirectory = (String) request.getSession().getAttribute("userTempDir");
		
	    // Pull in the uploaded Files bean from the user's session
	    List<FilesBean> uploadedFilesBean = 
	    	(request.getSession().getAttribute("uploadedFilesBeanList") == null) 
	    	? new ArrayList<FilesBean>() : (List<FilesBean>) request.getSession().getAttribute("uploadedFilesBean");
	    
	    MessageBean errorBean = new MessageBean();
	    MessageBean messageBean = new MessageBean();
	    
	    if ("delete".equals(action)) { // Delete Files
	    	String filename = (request.getParameter("file") == null) ? "" : request.getParameter("file");
	    	if ("".equals(filename) || filename == null) {
	    		log.debug("There was no filename passed to be deleted");
	    		errorBean.addMessage("Client did not pass a filename to be deleted. Please try again or contact system administrator for assistance");
	    	} else {
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
		request.setAttribute("messageBean", messageBean);
		
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
	 */
	@SuppressWarnings("unchecked")
	private boolean uploadFiles(HttpServletRequest request, String directory) throws Exception {
		boolean result = false;
		// Utility method that determines whether the request contains multipart content (files)
		// true if the request is multipart; false otherwise.
		boolean isMultiPart = ServletFileUpload.isMultipartContent(request);
		if (!isMultiPart) return false;
		
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
		
		result = FileHelper.saveFileItems(directory, items);
		
		return result;
	}

	


}
