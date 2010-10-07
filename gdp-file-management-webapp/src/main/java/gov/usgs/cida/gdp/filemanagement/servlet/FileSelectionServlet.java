package gov.usgs.cida.gdp.filemanagement.servlet;

import gov.usgs.cida.gdp.utilities.FileHelper;
import gov.usgs.cida.gdp.utilities.XmlUtils;
import gov.usgs.cida.gdp.utilities.bean.Acknowledgement;
import gov.usgs.cida.gdp.utilities.bean.AvailableFiles;
import gov.usgs.cida.gdp.utilities.bean.Error;
import gov.usgs.cida.gdp.utilities.bean.Message;
import gov.usgs.cida.gdp.utilities.bean.XmlReply;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.LoggerFactory;

/**
 * Servlet implementation class FileSelectionServlet
 */
public class FileSelectionServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static org.slf4j.Logger log = LoggerFactory.getLogger(FileSelectionServlet.class);
    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Long start = Long.valueOf(new Date().getTime());

        String command = (request.getParameter("command") == null) ? "" : request.getParameter("command");

        XmlReply xmlReply = null;

        if ("listfiles".equals(command)) {
            String userDirectory = request.getParameter("userdirectory") == null ? "" : request.getParameter("userdirectory");
            String userSpaceDir = System.getProperty("applicationUserSpaceDir");
            String tempDir = System.getProperty("applicationTempDir");
            // Test to see if the directory does exist. If so,
            // update the time on those files to today to escape the
            // timed deletion process
            if (userDirectory != null && !"".equals(userSpaceDir + userDirectory)) {
                if (FileHelper.doesDirectoryOrFileExist(userSpaceDir + userDirectory)) {
                    FileHelper.updateTimestamp(userSpaceDir + userDirectory, false); // Update the timestamp
                } else {
                    userDirectory = "";
                }
            }

            AvailableFiles afb = null;
            try {
                afb = AvailableFiles.getAvailableFilesBean(tempDir, userSpaceDir + userDirectory);
            } catch (IllegalArgumentException e) {
                xmlReply = new XmlReply(Acknowledgement.ACK_FAIL, new Error(Error.ERR_FILE_LIST, e));
                XmlUtils.sendXml(xmlReply, start, response);
                return;
            }

            if (afb == null || afb.getExampleFileList() == null
                    || afb.getExampleFileList().isEmpty()
                    || afb.getShapeSetList() == null
                    || afb.getShapeSetList().isEmpty()) {
                // Couldn't pull any files. Send an error to the caller.
                xmlReply = new XmlReply(Acknowledgement.ACK_FAIL, new Message("Could not find any files to work with."));
                XmlUtils.sendXml(xmlReply, start, response);
                return;
            }

            xmlReply = new XmlReply(Acknowledgement.ACK_OK, afb);
            XmlUtils.sendXml(xmlReply, start, response);
            return;
        }

    }
}
