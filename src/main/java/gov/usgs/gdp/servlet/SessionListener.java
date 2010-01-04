package gov.usgs.gdp.servlet;

import java.util.Date;

import gov.usgs.gdp.io.FileHelper;

import javax.servlet.http.HttpSessionActivationListener;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.log4j.Logger;

/**
 * Application Lifecycle Listener implementation class SessionListener
 *
 */
public class SessionListener implements HttpSessionListener, HttpSessionAttributeListener, HttpSessionActivationListener, HttpSessionBindingListener {
	private static org.apache.log4j.Logger log = Logger.getLogger(SessionListener.class);

    /**
     * Default constructor. 
     */
    public SessionListener() {
        // TODO Auto-generated constructor stub
    }

	/**
     * @see HttpSessionAttributeListener#attributeRemoved(HttpSessionBindingEvent)
     */
    public void attributeRemoved(HttpSessionBindingEvent arg0) {
        // TODO Auto-generated method stub
    }

	/**
     * @see HttpSessionAttributeListener#attributeAdded(HttpSessionBindingEvent)
     */
    public void attributeAdded(HttpSessionBindingEvent arg0) {
        // TODO Auto-generated method stub
    }

	/**
     * @see HttpSessionBindingListener#valueUnbound(HttpSessionBindingEvent)
     */
    public void valueUnbound(HttpSessionBindingEvent arg0) {
        // TODO Auto-generated method stub
    }

	/**
     * @see HttpSessionActivationListener#sessionDidActivate(HttpSessionEvent)
     */
    public void sessionDidActivate(HttpSessionEvent arg0) {
        // TODO Auto-generated method stub
    }

	/**
     * @see HttpSessionActivationListener#sessionWillPassivate(HttpSessionEvent)
     */
    public void sessionWillPassivate(HttpSessionEvent arg0) {
        // TODO Auto-generated method stub
    }

	/**
     * @see HttpSessionBindingListener#valueBound(HttpSessionBindingEvent)
     */
    public void valueBound(HttpSessionBindingEvent arg0) {
        // TODO Auto-generated method stub
    }

	/**
     * @see HttpSessionAttributeListener#attributeReplaced(HttpSessionBindingEvent)
     */
    public void attributeReplaced(HttpSessionBindingEvent arg0) {
        // TODO Auto-generated method stub
    }

	/**
     * @see HttpSessionListener#sessionCreated(HttpSessionEvent)
     */
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
    public void sessionDestroyed(HttpSessionEvent arg0) {
    	log.debug("Session is dying. Cleaning up.");
    	String userTempDir = (String) arg0.getSession().getAttribute("userTempDir");
    	if (FileHelper.deleteDirRecursively(userTempDir)) {
    		log.debug("User subdirectory deleted at: " + userTempDir);
    	} else {
        	log.debug("User subdirectory could not be deleted at: " + userTempDir);
        	log.debug("This should be manually removed at a later point or will automatically be removed when application ends.");
        }
    }
	
}
