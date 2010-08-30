package gov.usgs.cida.gdp.filemanagement.servlet;

import gov.usgs.cida.gdp.filemanagement.bean.UploadFileCheckBean;
import gov.usgs.cida.gdp.utilities.FileHelper;
import gov.usgs.cida.gdp.utilities.XmlUtils;
import gov.usgs.cida.gdp.utilities.bean.AckBean;
import gov.usgs.cida.gdp.utilities.bean.ErrorBean;
import gov.usgs.cida.gdp.utilities.bean.XmlReplyBean;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.LoggerFactory;

/**
 * Servlet implementation class SendFileServlet
 */
public class SendFileServlet extends HttpServlet {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(SendFileServlet.class);
    private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public SendFileServlet() {
        super();
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {

        Long start = Long.valueOf(new Date().getTime());

        String command = request.getParameter("command");

        // User wishes to grab a file. Send this file if available.
        if ("getfile".equals(command)) {
            String file = request.getParameter("file");
            String baseFilePath = System.getProperty("applicationTempDir");
            baseFilePath = baseFilePath + FileHelper.getSeparator();
            String fullFilePath = baseFilePath + "upload-repository" + FileHelper.getSeparator() + file;
            File fileToUpload = null;

            if (!FileHelper.doesDirectoryOrFileExist(fullFilePath)) {
                XmlReplyBean xmlOutput = new XmlReplyBean(AckBean.ACK_FAIL, new ErrorBean(ErrorBean.ERR_FILE_NOT_FOUND));
                XmlUtils.sendXml(xmlOutput, start, response);
                return;
            }
            fileToUpload = new File(fullFilePath);

            // Set the headers.
            response.setContentType("application/x-download");
            response.setHeader("Content-Disposition", "attachment; filename=" + fileToUpload.getName());
            response.setCharacterEncoding("UTF-8");

            // Send the file.
            ServletOutputStream out = null;
            BufferedInputStream buf = null;
            try {
                out = response.getOutputStream();
                response.setContentLength((int) fileToUpload.length());
                FileInputStream input = new FileInputStream(fileToUpload);
                buf = new BufferedInputStream(input);
                int readBytes = 0;
                while ((readBytes = buf.read()) != -1) {
                    out.write(readBytes);
                }
                out.close();
                buf.close();
            } catch (IOException ioe) {
                throw new ServletException(ioe.getMessage());
            } finally {
                if (out != null) {
                    out.close();
                }
                if (buf != null) {
                    buf.close();
                }
            }
            return;
        }

        // Checks the upload repository for finished process file availability
        if ("checkuploadfile".equals(command)) {
            String file = request.getParameter("file");
            String baseFilePath = System.getProperty("applicationTempDir");
            baseFilePath = baseFilePath + FileHelper.getSeparator();
            String fullFilePath = baseFilePath + "upload-repository" + FileHelper.getSeparator() + file;
            boolean fileExists = FileHelper.doesDirectoryOrFileExist(fullFilePath);
            boolean hasBytes = false;
            File tempFile = new File(fullFilePath);
            hasBytes = tempFile.length() > 0;
            boolean fileExistsAndHasBytes = fileExists & hasBytes;
            UploadFileCheckBean ufcb = new UploadFileCheckBean(file, fileExistsAndHasBytes);

            XmlReplyBean xmlReply = new XmlReplyBean(AckBean.ACK_OK, ufcb);
            XmlUtils.sendXml(xmlReply, start, response);
            return;
        }

        //TODO- Do not know why this code is here. Check to see if it's used
        BufferedInputStream buf = null;
        ServletOutputStream stream = null;
        try {
            stream = response.getOutputStream();
            String file = (request.getParameter("file") == null) ? "" : request.getParameter("file");
            File sendFile = new File(file);
            response.addHeader("Content-Disposition", "attachment; filename=" + file);
            response.setContentLength((int) sendFile.length());
            FileInputStream input = new FileInputStream(file);

            // read from the file; write to the ServletOutputStream
            int readBytes = 0;
            buf = new BufferedInputStream(input);
            while ((readBytes = buf.read()) != -1) {
                stream.write(readBytes);
            }
        } catch (IOException ioe) {
            log.debug(ioe.getMessage());
            throw new ServletException(ioe.getMessage());
        } finally {
            if (stream != null) {
                stream.close();
            }
            if (buf != null) {
                buf.close();
            }
        }


    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}
