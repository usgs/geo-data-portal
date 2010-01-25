package gov.usgs.gdp.servlet;

import gov.usgs.gdp.bean.ErrorBean;
import gov.usgs.gdp.bean.FilesBean;
import gov.usgs.gdp.bean.MessageBean;
import gov.usgs.gdp.bean.ShapeFileSetBean;
import gov.usgs.gdp.helper.FileHelper;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
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
		
		Map<String, String> requestParameters = request.getParameterMap();
		
		String xmlOutput = "";
		if (ServletFileUpload.isMultipartContent(request)) {
			// User is uploading files...
			RequestDispatcher rd = request.getRequestDispatcher("/UploadFilesServlet?command=upload");
			rd.forward(request, response);
			return;
		}
		
		if (!requestParameters.containsKey("command")) {
			ErrorBean errorBean = new ErrorBean(ErrorBean.ERR_NO_COMMAND);
			xmlOutput = errorBean.toXml();
			RouterServlet.sendXml(xmlOutput, response);
			return;
		}
		
		String command = request.getParameter("command");
		
		if ("listfiles".equals(command)) {
			// Forward the user to their destination						
			RequestDispatcher rd = request.getRequestDispatcher("/FileSelectionServlet?command=listfiles");
			rd.forward(request, response);
			return;
		}
		
		// Check for stale session
		String tomcatStarted = System.getProperty("tomcatStarted");
		String sessionStarted = Long.toString(request.getSession().getCreationTime());
		if (Long.parseLong(sessionStarted) < Long.parseLong(tomcatStarted)) {
			log.debug("User has stale session. Re-initializing user session.");
			request.getSession().invalidate();
			RequestDispatcher rd = request.getRequestDispatcher("/jsp/index.jsp");
			MessageBean errorBean = new MessageBean();
			errorBean.addMessage("Your session has become stale. Session restarted.");
			rd.forward(request, response);
			return;
		}
		
		// First check that session information is synchronized
		String applicationTempDir 				= System.getProperty("applicationTempDir");
		String location							= (request.getParameter("location") == null) ? "" : request.getParameter("location").toLowerCase();
		String action							= (request.getParameter("action") == null) ? "" : request.getParameter("action").toLowerCase();
		String file 							= (request.getParameter("file") == null) ? "" : request.getParameter("file");
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
		} else if ("downloadfile".equals(location)) {
			forwardTo = "/FileUploadServlet?file=" + file;
		} else if ("sessionrestart".equals(location)) {
			forwardTo = "/Router";
		}
		
		// Forward the user to their destination
		RequestDispatcher rd = request.getRequestDispatcher(forwardTo);
		rd.forward(request, response);
	}

	public static void sendXml(String xml, HttpServletResponse response) throws IOException {
		 String xmlHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
		 xml = xmlHeader + xml;
		 ServletOutputStream stream = null;
		 BufferedInputStream bis = new BufferedInputStream(new ByteArrayInputStream(xml.getBytes()));
		 try {
			stream = response.getOutputStream();
			response.setContentType("text/xml");
			response.setContentLength(xml.length());
			int readBytes = 0;
			while ((readBytes = bis.read()) != -1) {
				stream.write(readBytes);
			}
			stream.flush();
		} finally {
			if (stream != null) stream.close();
			bis.close();
		}
		
	}

	


}
