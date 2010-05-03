package gov.usgs.gdp.servlet;

import java.io.IOException;
import java.util.Date;

import gov.usgs.gdp.helper.FileHelper;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.log4j.Logger;

/**
 * Application Lifecycle Listener implementation class SessionListener
 *
 */
public class SessionListener implements HttpSessionListener {
	private static org.apache.log4j.Logger log = Logger.getLogger(SessionListener.class);

    /**
     * Default constructor. 
     */
    public SessionListener() {
        // TODO Auto-generated constructor stub
    }

	/**
     * @see HttpSessionListener#sessionCreated(HttpSessionEvent)
     */
    @Override
    public void sessionCreated(HttpSessionEvent arg0) {
    	log.debug("Session is starting. Initializing.");
        String applicationTempDir = System.getProperty("applicationTempDir");
        String seperator = FileHelper.getSeparator();
        String userSubDir = Long.toString(new Date().getTime());
        String userTempDir = applicationTempDir + seperator + userSubDir;
        arg0.getSession().setAttribute("sessionAppTempDir", applicationTempDir);
        arg0.getSession().setAttribute("userSubDir", userSubDir);
        arg0.getSession().setAttribute("userTempDir", userTempDir);
        if (FileHelper.createDir(userTempDir)) {
        	log.debug("User subdirectory created at: " + userTempDir);
        } else {
        	log.debug("User subdirectory could not be created at: " + userTempDir);
        	log.debug("User will be unable to upload files for this session.");
        }
    }

	/**
     * @see HttpSessionListener#sessionDestroyed(HttpSessionEvent)
     */
    @Override
    public void sessionDestroyed(HttpSessionEvent arg0) {
    	log.debug("Session is dying. Cleaning up.");
    	String userTempDir = (String) arg0.getSession().getAttribute("userTempDir");
    	try {
			if (FileHelper.deleteDirRecursively(userTempDir)) {
				log.debug("User subdirectory deleted at: " + userTempDir);
			} else {
				log.debug("User subdirectory could not be deleted at: " + userTempDir);
				log.debug("This should be manually removed at a later point or will automatically be removed when application ends.");
			}
		} catch (IOException e) {
			log.debug("User subdirectory could not be deleted at: " + userTempDir);
			log.debug("This should be manually removed at a later point or will automatically be removed when application ends.");
		}
    }
	
}
