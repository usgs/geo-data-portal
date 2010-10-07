package gov.usgs.cida.gdp.outputprocessing.servlet;

import gov.usgs.cida.gdp.outputprocessing.bean.OutputFileType;
import gov.usgs.cida.gdp.outputprocessing.bean.OutputStatistics;
import gov.usgs.cida.gdp.utilities.FileHelper;
import gov.usgs.cida.gdp.utilities.XmlUtils;
import gov.usgs.cida.gdp.utilities.bean.Acknowledgement;
import gov.usgs.cida.gdp.utilities.bean.Error;
import gov.usgs.cida.gdp.utilities.bean.XmlReply;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.LoggerFactory;

/**
 *
 * @author admin
 */
public class OutputInfoServlet extends HttpServlet {
   private static final long serialVersionUID = 1L;
    private static org.slf4j.Logger log = LoggerFactory.getLogger(OutputInfoServlet.class);


    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        doPost(request, response);
    } 

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        Long start = Long.valueOf(new Date().getTime());
		String command = request.getParameter("command");
		XmlReply xmlReply = null;

        if ("getoutputstats".equals(command)) {
                OutputStatistics outputStats = OutputStatistics.getOutputStatisticsBean();
                xmlReply = new XmlReply(Acknowledgement.ACK_OK, outputStats);
                XmlUtils.sendXml(xmlReply, start, response);
                return;
        }

        if ("getoutputtypelist".equals(command)) {
                log.debug("User has chosen to get output file type list");
                List<String> availableFileTypes = FileHelper.getOutputFileTypesAvailable();
                if (availableFileTypes == null || availableFileTypes.isEmpty()) {
                        xmlReply = new XmlReply(Acknowledgement.ACK_FAIL, new Error(Error.ERR_OUTFILES_UNAVAILABLE));
                        XmlUtils.sendXml(xmlReply, start, response);
                        return;
                }

                OutputFileType oftb = new OutputFileType(availableFileTypes);
                xmlReply = new XmlReply(Acknowledgement.ACK_OK, oftb);
                XmlUtils.sendXml(xmlReply, start, response);
                return;
        }
    }

}
