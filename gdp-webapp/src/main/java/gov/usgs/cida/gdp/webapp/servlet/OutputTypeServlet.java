/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.usgs.cida.gdp.webapp.servlet;

import gov.usgs.cida.gdp.utilities.FileHelper;
import gov.usgs.cida.gdp.utilities.XmlUtils;
import gov.usgs.cida.gdp.utilities.bean.AckBean;
import gov.usgs.cida.gdp.utilities.bean.ErrorBean;
import gov.usgs.cida.gdp.utilities.bean.XmlReplyBean;
import gov.usgs.cida.gdp.webapp.bean.OutputFileTypeBean;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;

/**
 *
 * @author admin
 */
public class OutputTypeServlet extends HttpServlet {
   private static final long serialVersionUID = 1L;
    private static org.apache.log4j.Logger log = Logger.getLogger(OutputTypeServlet.class);


    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
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
		XmlReplyBean xmlReply = null;

		if ("getoutputtypelist".equals(command)) {
			log.debug("User has chosen to get output file type list");
			List<String> availableFileTypes = FileHelper.getOutputFileTypesAvailable();
			if (availableFileTypes == null || availableFileTypes.isEmpty()) {
				xmlReply = new XmlReplyBean(AckBean.ACK_FAIL, new ErrorBean(ErrorBean.ERR_OUTFILES_UNAVAILABLE));
				XmlUtils.sendXml(xmlReply, start, response);
				return;
			}

			OutputFileTypeBean oftb = new OutputFileTypeBean(availableFileTypes);
			xmlReply = new XmlReplyBean(AckBean.ACK_OK, oftb);
			XmlUtils.sendXml(xmlReply, start, response);
			return;
		}
    }

}
