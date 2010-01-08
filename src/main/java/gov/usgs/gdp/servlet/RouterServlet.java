package gov.usgs.gdp.servlet;

import gov.usgs.gdp.bean.FilesBean;
import gov.usgs.gdp.io.FileHelper;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
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
		String forwardTo = "";
		
		// If no example File List exists, create one
		FilesBean exampleFiles = new FilesBean();
		Collection<File> files = (Collection<File>) request.getSession().getAttribute("exampleFileList");
		if (files == null || files.isEmpty()) {
			files = FileHelper.getFileCollection(applicationTempDir + java.io.File.separator + "Sample_Files", true);
			//Collections.sort(files);
			if (files == null || files.isEmpty()) log.debug("Could not find sample files. User will not have access to them.");
			if (files != null) exampleFiles.setFiles(files);
			request.getSession().setAttribute("exampleFileBean", exampleFiles);
		}
		
		// Where is the user trying to get to?
		if ("filesprocessing".equals(location)) {
			forwardTo = "/jsp/fileSelection.jsp";
		} else if ("uploadfiles".equals(location)) {
			forwardTo = "/UploadFilesServlet";
		}  else if ("summarize".equals(location)) {
			forwardTo = "/FileSelectionServlet?action=summarize";
		}
		
		RequestDispatcher rd = request.getRequestDispatcher(forwardTo);
		rd.forward(request, response);
	}

}
