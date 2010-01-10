package gov.usgs.gdp.servlet;

import gov.usgs.gdp.bean.FilesBean;
import gov.usgs.gdp.bean.ShapeFileSetBean;
import gov.usgs.gdp.io.FileHelper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
	@SuppressWarnings("unchecked")
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
		String applicationTempDir 				= System.getProperty("applicationTempDir");
		String location							= (request.getParameter("location") == null) ? "" : request.getParameter("location").toLowerCase();
		String action							= (request.getParameter("action") == null) ? "" : request.getParameter("action").toLowerCase();
		List<FilesBean> exampleFilesBeanList 	= (List<FilesBean>) request.getSession().getAttribute("exampleFileBeanList");
		List<FilesBean> uploadedFilesBeanList 	= (List<FilesBean>) request.getSession().getAttribute("uploadedFilesBeanList");	
		String forwardTo 						= "";
		
		// If no example File List exists, create one
		if (exampleFilesBeanList == null || exampleFilesBeanList.isEmpty()) {
			exampleFilesBeanList = FilesBean.getFilesBeanSetList(FileHelper.getFileCollection(applicationTempDir + java.io.File.separator + "Sample_Files", true));		
			if (exampleFilesBeanList == null || exampleFilesBeanList.isEmpty()) {
				log.debug("Could not find sample files. User will not have access to them.");
				exampleFilesBeanList = new ArrayList<FilesBean>();
			} 
			request.getSession().setAttribute("exampleFileBeanList", exampleFilesBeanList);
		}		
		
		// If no uploaded FileBean List exists, create one		
		if (uploadedFilesBeanList == null) {
			uploadedFilesBeanList = new ArrayList<FilesBean>();
			request.getSession().setAttribute("uploadedFilesBeanList", uploadedFilesBeanList);
		}
		
		List<ShapeFileSetBean> shapeFileSetBeanList = new ArrayList<ShapeFileSetBean>();
		
		//Create shapefile sets from the example files for the user to choose from
		for (FilesBean exampleFilesBean : exampleFilesBeanList) {
			ShapeFileSetBean shapeFileSetList = ShapeFileSetBean.getShapeFileSetBeanFromFilesBean(exampleFilesBean);
			if (shapeFileSetList != null) shapeFileSetBeanList.add(shapeFileSetList);
		}

		//Create shapefile sets from the upload files for the user to choose from
		for (FilesBean uploadedFilesBean : uploadedFilesBeanList) {
			ShapeFileSetBean shapeFileSetList = ShapeFileSetBean.getShapeFileSetBeanFromFilesBean(uploadedFilesBean);
			if (shapeFileSetList != null) shapeFileSetBeanList.add(shapeFileSetList);
		}
		
		// Set up the session
		request.getSession().setAttribute("shapeFileSetBeanList", shapeFileSetBeanList);
		
		// Where is the user trying to get to?
		if ("filesprocessing".equals(location)) {
			forwardTo = "/jsp/fileSelection.jsp";
		} else if ("uploadfiles".equals(location)) {
			forwardTo = "/UploadFilesServlet";
		}  else if ("summarize".equals(location)) {
			forwardTo = "/FileSelectionServlet?action=summarize";
		} else if ("processfiles".equals(location)) {
			forwardTo = "/FileProcessServlet?action=" + action;
		}
		
		// Forward the user to their destination
		RequestDispatcher rd = request.getRequestDispatcher(forwardTo);
		rd.forward(request, response);
	}

}
