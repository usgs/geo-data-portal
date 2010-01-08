package gov.usgs.gdp.servlet;

import gov.usgs.gdp.bean.FilesBean;
import gov.usgs.gdp.io.FileHelper;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;
import org.geotools.data.shapefile.ShpFiles;
import org.geotools.data.shapefile.shp.ShapefileException;
import org.geotools.data.shapefile.shp.ShapefileReader;

/**
 * Servlet implementation class UploadFilesServlet
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
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Pull in parameters
		String action 	= (request.getParameter("action") == null) ? "" : request.getParameter("action").toLowerCase();
		
		// What is directory name for the files being uploaded
		String seperator = FileHelper.getSeparator();
	    String userDirectory = (String) request.getSession().getAttribute("userTempDir")+ seperator;
		
	    // Pull in the uploaded Files bean from the user's session
	    FilesBean uploadedFilesBean = (request.getSession().getAttribute("uploadedFilesBean") == null) ? new FilesBean() : (FilesBean) request.getSession().getAttribute("uploadedFilesBean");
	    
	    if ("delete".equals(action)) {
	    	String filename = (request.getParameter("file") == null) ? "" : request.getParameter("file");
	    	if ("".equals(filename) || filename == null) {
	    		log.debug("There was no filename passed to be deleted");
	    	} else {
	    		FileHelper.deleteFile(filename);
	    	}
	    } else if ("upload".equals(action)){
	    	if (uploadFiles(request, userDirectory)) {
	    		log.debug("Files successfully uploaded.");
	    	} else {
	    		log.debug("Files were unable to be uploaded");
	    	}
	    }
	    
	    // Rescan the user directory for updates
	    uploadedFilesBean = populateUploadedFilesBean(userDirectory);
		
	    // Place the bean in the user's session
		request.getSession().setAttribute("uploadedFilesBean", uploadedFilesBean);
		
		// Away we go
		RequestDispatcher rd = request.getRequestDispatcher("/jsp/fileUpload.jsp");
		rd.forward(request, response);
		
	}

	private FilesBean populateUploadedFilesBean(String userDirectory) {
		FilesBean result = new FilesBean();
		Collection<File> uploadedFiles = FileHelper.getFileCollection(userDirectory, true);
		if (uploadedFiles != null) result.setFiles(uploadedFiles);
		return result;
	}

	private boolean uploadFiles(HttpServletRequest request, String userDirectory) {
		boolean isMultiPart = ServletFileUpload.isMultipartContent(request);

		log.debug("Form was sent with multipart content: " + Boolean.toString(isMultiPart));
		
		// Create a factory for disk-based file items
		FileItemFactory factory = new DiskFileItemFactory();

		// Create a new file upload handler
		ServletFileUpload upload = new ServletFileUpload(factory);
		
		
	    
		List<FileItem> items = null;
		
	    try {
			items = upload.parseRequest(request);
			// process the uploaded items
			Iterator<FileItem> iter = items.iterator();
			while (iter.hasNext()) {
				FileItem item = iter.next();
				
			    String fileName = item.getName();
			    String tempFile = userDirectory + fileName;
			    
			    File uploadedFile = new File(tempFile);
			    try {
					item.write(uploadedFile);
				} catch (Exception e) {
					return false;
				}
			
			}
		} catch (FileUploadException e) {
			return false;
		}
		return true;
	}


}
