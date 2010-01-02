package gov.usgs.gdp.servlet;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
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
import org.geotools.factory.GeoTools;
import org.geotools.data.shapefile.ShpFiles;
import org.geotools.data.shapefile.shp.ShapefileException;
import org.geotools.data.shapefile.shp.ShapefileReader;

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
		// Pull in parameters
		String action 	= (request.getParameter("action") == null) ? "" : request.getParameter("action").toLowerCase();
		String method 	= (request.getParameter("method") == null) ? "" : request.getParameter("method").toLowerCase();
		
		boolean isMultiPart = ServletFileUpload.isMultipartContent(request);

		log.debug("Form was sent with multipart content: " + Boolean.toString(isMultiPart));
		
		// Create a factory for disk-based file items
		FileItemFactory factory = new DiskFileItemFactory();

		// Create a new file upload handler
		ServletFileUpload upload = new ServletFileUpload(factory);
		
		// Create a directory name for the files being uploaded
	    String applicationTempDir = System.getProperty("applicationTempDir");
	    
		List<String> uploadedFiles = new ArrayList<String>();
		List<FileItem> items = null;
		
	    try {
			items = upload.parseRequest(request);
			// process the uploaded items
			Iterator<FileItem> iter = items.iterator();
			while (iter.hasNext()) {
				FileItem item = iter.next();
				
			    String fileName = item.getName();
			    String tempFile = applicationTempDir + "/" + fileName;
			    
			    File uploadedFile = new File(tempFile);
			    try {
					item.write(uploadedFile);
					uploadedFiles.add(tempFile);
				} catch (Exception e) {
					log.error(e.getMessage());
				}
			
			}
		} catch (FileUploadException e) {
			log.error(e.getMessage());
		}
		
		getGeotoolsSummary(uploadedFiles);
		
		boolean directoryRemoved = deleteDir(new File(applicationTempDir));
	    log.debug("Directory deleted: " + Boolean.toString(directoryRemoved));
	}

	@SuppressWarnings("deprecation")
	private void getGeotoolsSummary(List<String> uploadedFiles)
			throws IOException, ShapefileException, MalformedURLException {
		// Load in the files and try them out....
		for (String uploadedFile : uploadedFiles) {
			if (uploadedFile.toLowerCase().contains(".shp")) {
				ShapefileReader reader = new ShapefileReader(new ShpFiles(uploadedFile),true,true);
				log.debug(reader.getHeader().toString());
				int counter = 1;
				while (reader.hasNext()) {
					ShapefileReader.Record nextRecord = reader.nextRecord();
					log.debug("Record number: " + Integer.toString(counter)
							+ ", MaxX: " + nextRecord.maxX
							+ ", MaxY: " + nextRecord.maxY
							+ ", MinX: " + nextRecord.minX
							+ ", MinY: " + nextRecord.minY
							+ ", Offset: " + nextRecord.offset()
							+ ", ShapeType: " + nextRecord.type.name);
					counter++;
				}
				
				reader.close();
			}
		}
	}
	
	/**
	 * Deletes all files and subdirectories under dir.
	 * Returns true if all deletions were successful.
     * If a deletion fails, the method stops attempting to delete and returns false.
	 * @param dir
	 * @return
	 */
	public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
    
        // The directory is now empty so delete it
        return dir.delete();
    }

}
