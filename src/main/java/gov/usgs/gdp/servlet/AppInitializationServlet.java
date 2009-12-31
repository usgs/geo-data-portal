package gov.usgs.gdp.servlet;

import gov.usgs.gdp.io.FileHelper;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 * Servlet implementation class AppInitializationServlet
 */
public class AppInitializationServlet extends HttpServlet {
	private static org.apache.log4j.Logger log = Logger.getLogger(AppInitializationServlet.class);
	private static final long serialVersionUID = 1L;
	private String tmpDir  = "";
	private String seperator = "";
	private String applicationTempDir = "";
	
    @Override
    public void init(ServletConfig config) throws ServletException {
    	super.init(config);
    	log.debug("Application is starting");
    	
    	tmpDir = FileHelper.getSystemTemp();
    	this.seperator = FileHelper.getSeparator();
    	log.debug("Current system temp directory is: " + tmpDir);
    	
    	// Create application temporary directory
    	applicationTempDir = generateApplicationTempDirName();
    	boolean dirCreated = createApplicationTempDir(applicationTempDir);
    	if (dirCreated) {
    		System.setProperty("applicationTempDir", applicationTempDir);
    		log.debug("Current application temp directory is: " + applicationTempDir);
    	} else {
    		log.debug("ERROR: Could not create application temp directory: " + applicationTempDir);
    		log.debug("\tIf this directory is not created manually, there may be issues during application run");
    	}
    	
    	// Place example files in temporary directory 
    	
    	
    	log.debug("Saving example shapefiles ");
    	log.debug("Application has started");
    }
    
    private String generateApplicationTempDirName() {
    	String result = "";
		
		Date currentDate = new Date();
	    String currentMilliseconds = Long.toString(currentDate.getTime());
	    result = tmpDir + this.seperator + currentMilliseconds;
		
		return result;
	}

	private boolean createApplicationTempDir(String directory) {
		boolean result = false;
		result = FileHelper.createDir(directory);
		return result;
	}

	@Override
    public void destroy() {
    	super.destroy();
    	log.debug("Application is ending.");
    	boolean result = FileHelper.deleteDirRecursively(applicationTempDir);
    	if (result) {
    		log.debug("Application temp directory " + applicationTempDir + " successfully deleted.");
    	} else {
    		log.debug("WARNING: Application temp directory " + applicationTempDir + " could not be deleted.");
    		log.debug("\t If this directory exists, you may want to delete it to free up space on your storage device.");
    	}
    	log.debug("Application has ended.");
    }
    
    /**
     * @see HttpServlet#HttpServlet()
     */
    public AppInitializationServlet() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// This servlet does not receive commands
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// This servlet does not receive commands
	}

}
