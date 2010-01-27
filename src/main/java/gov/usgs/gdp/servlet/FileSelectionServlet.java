package gov.usgs.gdp.servlet;

import gov.usgs.gdp.bean.AckBean;
import gov.usgs.gdp.bean.AvailableFilesBean;
import gov.usgs.gdp.bean.ErrorBean;
import gov.usgs.gdp.bean.MessageBean;
import gov.usgs.gdp.bean.XmlReplyBean;
import gov.usgs.gdp.helper.CookieHelper;
import gov.usgs.gdp.helper.FileHelper;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class FileSelectionServlet
 */
public class FileSelectionServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public FileSelectionServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String command = (request.getParameter("command") == null) ? "" : request.getParameter("command");
		XmlReplyBean xmlReply = null;
		if ("listfiles".equals(command)) {
			Cookie userDirectoryCookie = CookieHelper.getCookie(request, "userDirectory");
			// This line is needed because the last request may be from another servlet
			// that puts the cookie in the response, but it hits this servlet before
			// it hits the client that consumes it
			if (userDirectoryCookie == null) userDirectoryCookie = (Cookie) request.getAttribute("c00kie");
			String userDirectory = "";
			if (userDirectoryCookie != null) {
				if (FileHelper.doesDirectoryOrFileExist(userDirectoryCookie.getValue())) {
					userDirectory = userDirectoryCookie.getValue();
				}
			}
			
			String appTempDir = System.getProperty("applicationTempDir");
			AvailableFilesBean afb = null;
			try {
				afb = AvailableFilesBean.getAvailableFilesBean(appTempDir, userDirectory);
			} catch (IllegalArgumentException e) {
				xmlReply = new XmlReplyBean(AckBean.ACK_FAIL, new ErrorBean(ErrorBean.ERR_FILE_LIST, e));
				RouterServlet.sendXml(xmlReply, response);
				return;
			}
			
			// Couldn't pull any files. Send an error to the caller.
			if (afb == null || afb.getExampleFileList() == null 
					|| afb.getExampleFileList().isEmpty()
					|| afb.getShapeSetList() == null
					|| afb.getShapeSetList().isEmpty()) {
				xmlReply = new XmlReplyBean(AckBean.ACK_FAIL, new MessageBean("Could not find any files to work with."));
				RouterServlet.sendXml(xmlReply, response);
				return;
			}
			
			xmlReply = new XmlReplyBean(AckBean.ACK_OK, afb);
			RouterServlet.sendXml(xmlReply, response);
			return;
			
		}
		/*if ("summarize".equals(action)) {
			forwardTo = "/SummaryServlet?action=summarize";
		}*/
		
		

	}

}
