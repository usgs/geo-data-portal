package gov.usgs.gdp.servlet;

import gov.usgs.gdp.io.FileHelper;

import java.io.IOException;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 * Servlet implementation class RouterServlet
 */
public class RouterServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(RouterServlet.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public RouterServlet() {
        super();
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
		// Check for stale session
		String tomcatStarted = System.getProperty("tomcatStarted");
		String sessionStarted = Long.toString(request.getSession().getCreationTime());
		if (Long.parseLong(sessionStarted) < Long.parseLong(tomcatStarted)) {
			log.debug("User has stale session. Re-initializing user session.");
			request.getSession().invalidate();
		}
		
		// First check that session information is synchronized
		String applicationTempDir = System.getProperty("applicationTempDir");
		
		String location	= (request.getParameter("location") == null) ? "" : request.getParameter("location").toLowerCase();
		String action	= (request.getParameter("action") == null) ? "" : request.getParameter("action").toLowerCase();
		String forwardTo = "";
		
		// If no example File List exists, create one
		List<String> exampleFileList = (List<String>) request.getSession().getAttribute("exampleFileList");
		if (exampleFileList == null || exampleFileList.isEmpty()) {
			exampleFileList = FileHelper.getFileList(applicationTempDir + java.io.File.separator + "Sample_Files", true);
			if (exampleFileList == null || exampleFileList.isEmpty()) log.debug("Could not find sample files. User will not have access to them.");
			request.getSession().setAttribute("exampleFileList", exampleFileList);
		}
		
		if ("geotoolsprocessing".equals(location)) {
			forwardTo = "/GeoToolsServlet?action=" + action;
		} else if ("uploadfiles".equals(location)) {
			forwardTo = "/ParseFile";
		}
		
		RequestDispatcher rd = request.getRequestDispatcher(forwardTo);
		rd.forward(request, response);
	}

}
