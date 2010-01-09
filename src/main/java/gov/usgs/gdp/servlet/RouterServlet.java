package gov.usgs.gdp.servlet;

import gov.usgs.gdp.bean.FilesBean;
import gov.usgs.gdp.bean.ShapeFileSetBean;
import gov.usgs.gdp.io.FileHelper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
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
		String action	= (request.getParameter("action") == null) ? "" : request.getParameter("action").toLowerCase();
		String forwardTo = "";
		
		// If no example File List exists, create one
		List<FilesBean> exampleFilesBeanList = (List<FilesBean>) request.getSession().getAttribute("exampleFileBeanList");
		if (exampleFilesBeanList == null || exampleFilesBeanList.isEmpty()) {
			exampleFilesBeanList = FilesBean.getFilesBeanSetList(FileHelper.getFileCollection(applicationTempDir + java.io.File.separator + "Sample_Files", true));		
			if (exampleFilesBeanList == null || exampleFilesBeanList.isEmpty()) {
				log.debug("Could not find sample files. User will not have access to them.");
				exampleFilesBeanList = new ArrayList<FilesBean>();
			} 
			request.getSession().setAttribute("exampleFileBeanList", exampleFilesBeanList);
		}		
		
		// If no uploaded FileBean List exists, create one
		List<FilesBean> uploadedFilesBeanList = (List<FilesBean>) request.getSession().getAttribute("uploadedFilesBeanList");
		if (uploadedFilesBeanList == null) {
			uploadedFilesBeanList = new ArrayList<FilesBean>();
			request.getSession().setAttribute("uploadedFilesBeanList", uploadedFilesBeanList);
		}
		
		//Create shapefile sets for the user to choose from
		List<ShapeFileSetBean> shapeFileSetBeanList = new ArrayList<ShapeFileSetBean>();
		for (FilesBean exampleFilesBean : exampleFilesBeanList) {
			File projectionFile = null;
			File shapeFile = null;
			File dbFile = null;			
			File shapeFileIndexFile = null;
			for (File file : exampleFilesBean.getFiles()) {
				if (file.getName().toLowerCase().contains(".shp")) shapeFile = file;
				if (file.getName().toLowerCase().contains(".prj")) projectionFile = file;
				if (file.getName().toLowerCase().contains(".dbf")) dbFile = file;
				if (file.getName().toLowerCase().contains(".shx")) shapeFileIndexFile = file;
			}
			
			if (projectionFile != null && shapeFile != null && dbFile != null) {
				ShapeFileSetBean shapeFileSetBean = new ShapeFileSetBean();
				shapeFileSetBean.setName(shapeFile.getName().substring(0, shapeFile.getName().indexOf(".")));
				shapeFileSetBean.setDbfFile(dbFile);
				shapeFileSetBean.setShapeFile(shapeFile);
				shapeFileSetBean.setProjectionFile(projectionFile);
				shapeFileSetBean.setShapeFileIndexFile(shapeFileIndexFile);
				shapeFileSetBeanList.add(shapeFileSetBean);
			}
		}
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
		
		RequestDispatcher rd = request.getRequestDispatcher(forwardTo);
		rd.forward(request, response);
	}

}
