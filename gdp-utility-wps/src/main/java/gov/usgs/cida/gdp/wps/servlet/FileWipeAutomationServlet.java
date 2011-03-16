package gov.usgs.cida.gdp.wps.servlet;

import gov.usgs.cida.gdp.constants.AppConstant;
import gov.usgs.cida.gdp.dataaccess.ManageGeoserverWorkspace;
import gov.usgs.cida.gdp.utilities.FileHelper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * Servlet implementation class FileWipeAutomationServlet
 */
public class FileWipeAutomationServlet extends HttpServlet {

    static org.slf4j.Logger log = LoggerFactory.getLogger(FileWipeAutomationServlet.class);
    private static final long serialVersionUID = 1L;
    private Timer task;
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        
        initializeFilewipeTimer();
    }

    /**
     * @see HttpServlet#HttpServlet()
     */
    public FileWipeAutomationServlet() {
        super();
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
        task.scheduleAtFixedRate(new ScanFileTask(userSpaceDir, uploadDirName, workSpaceDir, fileAgeLong), 0l, 3600000l);

        // One minute test timer
        //task.scheduleAtFixedRate(new ScanFileTask(userSpaceDir, uploadDirName, 60000l), 0l, 60000l);
        log.info("File Wipe system started.");
        log.info("Will check " + uploadDirName.getPath() + " for files older than " + (fileAgeLong /  3600000) + " hour(s), every hour.");
        log.info("Will check " + userSpaceDir.getPath() +  " for files older than " + (fileAgeLong /  3600000) + " hour(s), every hour.");
    }

    class ScanFileTask extends TimerTask {
        private long hoursToWipe;
        private File userspaceDir;
        private File workspaceDir;
        private File repositoryDir;

        @Override
        public void run() {
            log.info("Running File Wipe Task... ");
            Collection<File> filesDeleted = new ArrayList<File>();
            
            if (getUserspaceDir() != null && getUserspaceDir().exists()) {
                filesDeleted = FileHelper.wipeOldFiles(getUserspaceDir(), Long.valueOf(this.hoursToWipe), false);
                if (!filesDeleted.isEmpty()) {
                    log.info("Finished deleting userspace files. " + filesDeleted.size() + " deleted.");
                }
            }

            if (getWorkspaceDir() != null && getWorkspaceDir().exists()) {
                filesDeleted = FileHelper.wipeOldFiles(getWorkspaceDir(), Long.valueOf(this.hoursToWipe), false);
                if (!filesDeleted.isEmpty()) {
                    log.info("Finished deleting workspace files. " + filesDeleted.size() + " deleted.");
                }
            }

            try {
                ManageGeoserverWorkspace mgsw = new ManageGeoserverWorkspace(AppConstant.WFS_ENDPOINT.getValue());
                mgsw.scanGeoserverWorkspacesForOutdatedDatastores(hoursToWipe, AppConstant.WFS_USER.getValue(), AppConstant.WFS_PASS.getValue(), "upload", "waters");
            } catch (IOException ex) {
                Logger.getLogger(FileWipeAutomationServlet.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SAXException ex) {
                Logger.getLogger(FileWipeAutomationServlet.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ParserConfigurationException ex) {
                Logger.getLogger(FileWipeAutomationServlet.class.getName()).log(Level.SEVERE, null, ex);
            } catch (XPathExpressionException ex) {
                Logger.getLogger(FileWipeAutomationServlet.class.getName()).log(Level.SEVERE, null, ex);
            }

            if (getRepositoryDir() != null && getRepositoryDir().exists()) {
                filesDeleted = FileHelper.wipeOldFiles(getRepositoryDir(), Long.valueOf(this.hoursToWipe), false);
                if (!filesDeleted.isEmpty()) {
                    log.info("Finished deleting repository directory files. " + filesDeleted.size() + " deleted.");
//                    try {
//                        new ManageGeoserverWorkspace().updateGeoServer(AppConstant.WFS_ENDPOINT.getValue());
//                        log.info("GeoServer has been reloaded after datastores were deleted.");
//                    } catch (IOException ex) {
//                        Logger.getLogger(FileWipeAutomationServlet.class.getName()).log(Level.WARNING, null, ex);
//                        log.warn("GeoServer could not be reloaded: \n" + ex);
//
//                    }
                }
            }

        }

        public ScanFileTask(File userspaceDir, File repositoryDir, File workspaceDir, long hoursToWipe) {
            this.userspaceDir = userspaceDir;
            this.repositoryDir = repositoryDir;
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
         * @return the userspaceDir
         */
        public File getUserspaceDir() {
            return this.userspaceDir;
        }

        /**
         * @param userspaceDir the userspaceDir to set
         */
        public void getUserspaceDir(@SuppressWarnings("hiding") File userspaceDir) {
            this.userspaceDir = userspaceDir;
        }

        /**
         * @return the repositoryDir
         */
        public File getRepositoryDir() {
            return this.repositoryDir;
        }

        /**
         * @param repositoryDir the repositoryDir to set
         */
        public void setRepositoryDir(@SuppressWarnings("hiding") File repositoryDir) {
            this.repositoryDir = repositoryDir;
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

    @Override
    public void destroy() {
        super.destroy();
        this.task.cancel();
        this.task.purge();
        log.info("File Wipe system stopped.");
    }


}
