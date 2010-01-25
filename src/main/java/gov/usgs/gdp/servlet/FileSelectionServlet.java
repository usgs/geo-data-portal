package gov.usgs.gdp.servlet;

import gov.usgs.gdp.bean.AvailableFilesBean;
import gov.usgs.gdp.bean.ErrorBean;
import gov.usgs.gdp.helper.CookieHelper;

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

		if ("listfiles".equals(command)) {
			String xmlReply = "";
			Cookie userDirectoryCookie = CookieHelper.getCookie(request, "userDirectory");
			String userDirectory = "";
			if (userDirectoryCookie != null) {
				userDirectoryCookie.getValue();
			}
			String appTempDir = System.getProperty("applicationTempDir");
			AvailableFilesBean afb = AvailableFilesBean.getAvailableFilesBean(appTempDir, userDirectory);
			
			// Couldn't pull any files. Send an error to the caller.
			if (afb == null || afb.getExampleFileList() == null 
					|| afb.getExampleFileList().isEmpty()
					|| afb.getShapeSetList() == null
					|| afb.getShapeSetList().isEmpty()) {
				ErrorBean error = new ErrorBean(2);
				xmlReply = error.toXml();
				RouterServlet.sendXml(xmlReply, response);
				return;
			}
			
			xmlReply = afb.toXml();
			RouterServlet.sendXml(xmlReply, response);
			return;
			
		}
		/*if ("summarize".equals(action)) {
			forwardTo = "/SummaryServlet?action=summarize";
		}*/
		
		

	}

}
