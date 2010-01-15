package gov.usgs.gdp.servlet;

import gov.usgs.gdp.io.FileHelper;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
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
    	
    	// Get the temp directory for the system
    	this.seperator = FileHelper.getSeparator();
    	this.tmpDir = FileHelper.getSystemTemp() + this.seperator + "GDP-APP-TEMP" + this.seperator;
		log.debug("Current system temp directory is: " + this.tmpDir);
		
    	boolean doesPreviousTempDirExist = FileHelper.doesDirectoryOrFileExist(this.tmpDir);
    	if (doesPreviousTempDirExist) {
    		if(deleteApplicationTempDirs()) {
    			log.debug("Temporary files from application's previous run have been removed");
    		} else {
    			log.debug("Application could not delete temp directories created on previous run.");
    		}
    	}
    	
    	// Now that the previous temp dirs are gone, create application temporary directory
    	this.applicationTempDir = generateApplicationTempDirName();
    	boolean dirCreated = createApplicationTempDir(this.applicationTempDir);
    	if (!dirCreated) dirCreated = FileHelper.doesDirectoryOrFileExist(this.applicationTempDir);
		System.setProperty("applicationTempDir", this.applicationTempDir);

    	if (dirCreated) {
    		log.debug("Current application temp directory is: " + this.applicationTempDir);
    	} else {
    		log.debug("ERROR: Could not create application temp directory: " + this.applicationTempDir);
    		log.debug("\tIf this directory is not created manually, there may be issues during application run");
    	}
    	
    	// Place example files in temporary directory 
    	try {
    		ClassLoader cl = Thread.currentThread().getContextClassLoader(); 
			URL sampleFileLocation = cl.getResource("Sample_Files/");
			if (sampleFileLocation != null) {
				log.debug("Saving example files to temp directory.");
				File sampleFiles = new File(sampleFileLocation.toURI());
				boolean filesCopied = false;
				try {
					filesCopied = FileHelper.copyFileToFile(sampleFiles, this.applicationTempDir + this.seperator);
				} catch (IOException e) {
					log.debug(e.getMessage());
				}
				
				if (filesCopied) {
					log.debug("Example files saved to: " + this.applicationTempDir + this.seperator + "Sample_Files/");					
				} else {
					log.debug("Sample files were not written to the application temp directory");
					log.debug("These files will not be available for processing.");
				}
				
			}
		}  catch (URISyntaxException e1) {
			log.debug("Unable to read from src/main/resources/Sample_Files");
			log.debug("Sample files were not written to the application temp directory");
		}
    	
    	Date created = new Date();
    	System.setProperty("tomcatStarted", Long.toString(created.getTime()));
    	log.debug("Application has started");
    }
    
    private String generateApplicationTempDirName() {
    	String result = "";
		
		Date currentDate = new Date();
	    String currentMilliseconds = Long.toString(currentDate.getTime());
	    result = this.tmpDir + FileHelper.getSeparator() + currentMilliseconds + FileHelper.getSeparator();
		
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
    	boolean result = false;
		result = deleteApplicationTempDirs();
    	if (result) {
    		log.debug("Application temp directory " + this.applicationTempDir + " successfully deleted.");
    	} else {
    		log.debug("WARNING: Application temp directory " + this.applicationTempDir + " could not be deleted.");
    		log.debug("\t If this directory exists, you may want to delete it to free up space on your storage device.");
    	}
    	log.debug("Application has ended.");
    }
    
	public boolean deleteApplicationTempDirs() {
		boolean result = false;
		try {
			String tempDir = FileHelper.getSystemTemp() + FileHelper.getSeparator();
			result = FileHelper.deleteDirRecursively(tempDir + "GDP-APP-TEMP" + FileHelper.getSeparator());
			if (result) {
				log.debug("Temporary files from application's previous run have been removed");
			} else {
				log.debug("Application could not delete temp directories created on previous run.");
			}
		} catch (IOException e) {
			log.debug("Application did not find or could not delete temp directories created on previous run.");
		}
		return result;
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
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// This servlet does not receive commands
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// This servlet does not receive commands
	}

}
