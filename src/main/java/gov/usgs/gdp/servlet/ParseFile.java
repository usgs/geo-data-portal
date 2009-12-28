package gov.usgs.gdp.servlet;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

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

/**
 * Servlet implementation class ParseFile
 */
public class ParseFile extends HttpServlet {
	private static org.apache.log4j.Logger log = Logger.getLogger(ParseFile.class);
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ParseFile() {
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
		boolean isMultiPart = ServletFileUpload.isMultipartContent(request);
		String emailAddress = "";
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
				
				if (item.isFormField()) { 
					// Did user enter an E-Mail address
					String fieldName = item.getFieldName();
					if ("emailAddress".equals(fieldName)) {
						emailAddress = item.getString();
						if (emailAddress == null) emailAddress = "";
					} 
				} else {
					String fieldName = item.getFieldName();
				    String fileName = item.getName();
				    String contentType = item.getContentType();
				    boolean isInMemory = item.isInMemory();
				    long sizeInBytes = item.getSize();
				    log.debug("FieldName: " + fieldName);
				    log.debug("FileName: " + fileName);
				    log.debug("ContentType: " + contentType);
				    log.debug("IsInMemory: " + Boolean.toString(isInMemory));
				    log.debug("SizeInBytes: " + sizeInBytes);
				    String tempFile = "/tmp/" + fileName;
				    Date currentDate = new Date();
				    String currentMilliseconds = Long.toString(currentDate.getTime());
				    File uploadedFile = new File(tempFile + currentMilliseconds);
				    try {
						item.write(uploadedFile);
					} catch (Exception e) {
						log.error(e.getMessage());
					}
				}
				
			}
		} catch (FileUploadException e) {
			log.error(e.getMessage());
		}
	}

}
