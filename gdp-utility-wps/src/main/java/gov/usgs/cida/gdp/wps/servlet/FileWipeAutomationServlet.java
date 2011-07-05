package gov.usgs.cida.gdp.wps.servlet;

import gov.usgs.cida.gdp.constants.AppConstant;
import gov.usgs.cida.gdp.dataaccess.GeoserverManager;
import gov.usgs.cida.gdp.utilities.FileHelper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpServlet;
import javax.xml.xpath.XPathExpressionException;

import org.slf4j.LoggerFactory;

/**
 * Servlet implementation class FileWipeAutomationServlet
 */
public class FileWipeAutomationServlet implements ServletContextListener {

    static org.slf4j.Logger log = LoggerFactory.getLogger(FileWipeAutomationServlet.class);
    private static final long serialVersionUID = 1L;
    private Timer task;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public FileWipeAutomationServlet() {
        super();
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        initializeFilewipeTimer();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        this.task.cancel();
        this.task.purge();
        log.info("File Wipe system stopped.");
    }
    
    /**
     * Initializes a timer that will check the file system every hour and wipes any files
     * over 48 hours long.
     */
    private void initializeFilewipeTimer() {
        log.info("File Wipe system starting.");

        long fileAgeLong = Long.parseLong(AppConstant.FILE_WIPE_MILLIS.getValue());
        File uploadDirName = new File(AppConstant.SHAPEFILE_LOCATION.getValue());
        File userSpaceDir = new File(AppConstant.USERSPACE_LOCATION.getValue());
        File workSpaceDir = new File(AppConstant.WORK_LOCATION.getValue());

        // Ensure the directories exist - this won't recreate them if they already exist
        FileHelper.createDir(uploadDirName);
        FileHelper.createDir(userSpaceDir);
        FileHelper.createDir(workSpaceDir);

        // Set up the tast to run every hour, starting 1 hour from now
        task = new Timer("File-Wipe-Timer",true);
//        task.scheduleAtFixedRate(new ScanFileTask(workSpaceDir, fileAgeLong), 0l, 3600000l);
        task.scheduleAtFixedRate(new ScanFileTask(workSpaceDir, fileAgeLong), 0l, 30000l); // Half minute test timer
        
        log.info("File Wipe system started.");
    }

    private class ScanFileTask extends TimerTask {
        private long hoursToWipe;
        private File workspaceDir;

        @Override
        public void run() {
            log.info("Running File Wipe Task... ");
            Collection<File> filesDeleted = new ArrayList<File>();

            try {
                GeoserverManager gm = new GeoserverManager(AppConstant.WFS_ENDPOINT.getValue(),
                        AppConstant.WFS_USER.getValue(), AppConstant.WFS_PASS.getValue());
                
                gm.deleteOutdatedDataStores(hoursToWipe, "upload", "waters", "draw");
            } catch (IOException ex) {
                Logger.getLogger(FileWipeAutomationServlet.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathExpressionException ex) {
                Logger.getLogger(FileWipeAutomationServlet.class.getName()).log(Level.SEVERE, null, ex);
            }

            if (getWorkspaceDir() != null && getWorkspaceDir().exists()) {
                log.info("Checking work space directory " + getWorkspaceDir().getPath() + " for files older than " + Long.valueOf(this.hoursToWipe) + "ms");
                filesDeleted = FileHelper.wipeOldFiles(getWorkspaceDir(), Long.valueOf(this.hoursToWipe), false);
                if (!filesDeleted.isEmpty()) {
                    log.info("Finished deleting workspace files. " + filesDeleted.size() + " deleted.");
                }
            }

        }

        public ScanFileTask(File workspaceDir, long hoursToWipe) {
            this.workspaceDir = workspaceDir;
            this.hoursToWipe = hoursToWipe;
        }

        public ScanFileTask() {
            this.hoursToWipe = Long.getLong(AppConstant.FILE_WIPE_MILLIS.toString());
        }

        /**
         * @return the hoursToWipe
         */
        public long getHoursToWipe() {
            return this.hoursToWipe;
        }

        /**
         * @param hoursToWipe the hoursToWipe to set
         */
        public void setHoursToWipe(@SuppressWarnings("hiding") long hoursToWipe) {
            this.hoursToWipe = hoursToWipe;
        }

        /**
         * @return the workspaceDir
         */
        public File getWorkspaceDir() {
            return workspaceDir;
        }

        /**
         * @param workspaceDir the workspaceDir to set
         */
        public void setWorkspaceDir(File workspaceDir) {
            this.workspaceDir = workspaceDir;
        }

    }
}
