package gov.usgs.gdp.servlet;

import gov.usgs.gdp.bean.AckBean;
import gov.usgs.gdp.bean.ErrorBean;
import gov.usgs.gdp.bean.XmlReplyBean;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
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
		
		if (ServletFileUpload.isMultipartContent(request)) {
			log.debug("User is attempting to upload files.");
			RequestDispatcher rd = request.getRequestDispatcher("/UploadFilesServlet?command=upload");
			rd.forward(request, response);
			return;
		}
		
		if (!requestParameters.containsKey("command")) {
			log.debug("User did not send command");
			ErrorBean errorBean = new ErrorBean(ErrorBean.ERR_NO_COMMAND);
			XmlReplyBean xmlReply = new XmlReplyBean(AckBean.ACK_FAIL, errorBean);
			RouterServlet.sendXml(xmlReply, response);
			return;
		}
		
		String command = request.getParameter("command");
		
		if ("listfiles".equals(command)) {
			log.debug("User is attempting to list files");
			RequestDispatcher rd = request.getRequestDispatcher("/FileSelectionServlet?command=listfiles");
			rd.forward(request, response);
			return;
		}
		
		if ("listattributes".equals(command)) {
			log.debug("User is attempting to list attributes");
			String shapefile = request.getParameter("shapefile");
			RequestDispatcher rd = request.getRequestDispatcher("/FileAttributeServlet?command=listattributes&shapefile=" + shapefile);
			rd.forward(request, response);
			return;
		}
		
		if ("listfeatures".equals(command)) {
			log.debug("User is attempting to list features");
			String shapefile = request.getParameter("shapefile");
			String attribute = request.getParameter("attribute");
			RequestDispatcher rd = request.getRequestDispatcher("/FileFeatureServlet?command=listfeatures&shapefile=" + shapefile + "&attribute=" + attribute);
			rd.forward(request, response);
			return;
		}
		
		if ("listthredds".equals(command)) {
			log.debug("User is attempting to list THREDDS servers");
			RequestDispatcher rd = request.getRequestDispatcher("/THREDDSCheckServlet?command=listthredds");
			rd.forward(request, response);
			return;
		}
		
		if ("checkserver".equals(command)) {
			log.debug("User is attempting to check server status");
			String hostname = request.getParameter("hostname");
			String port = request.getParameter("port");
			String uri = request.getParameter("uri");
			RequestDispatcher rd = request.getRequestDispatcher("/THREDDSCheckServlet?command=checkserver&hostname=" + hostname + "&port=" + port + "&uri=" + uri);
			rd.forward(request, response);
			return;
		}
		
		if ("createdatastore".equals(command)) {
			log.debug("User is attempting to create data store");
			String shapefile = request.getParameter("shapefile");
			RequestDispatcher rd = request.getRequestDispatcher("/GeoServerServlet?command=createdatastore&shapefile=" + shapefile);
			rd.forward(request, response);
			return;
		}
		
		if ("getdatasetlist".equals(command)) {
			log.debug("User is attempting to list datasets");
			String hostname = request.getParameter("dataseturl");
			String port = request.getParameter("port");
			String uri = request.getParameter("uri");
			RequestDispatcher rd = request.getRequestDispatcher("/THREDDSServlet?command=getdatasetlist&hostname=" + hostname + "&port=" + port + "&uri=" + uri);
			rd.forward(request, response);
			return;
		}
		
		if ("getgridlist".equals(command)) {
			log.debug("User is attempting to get a grid list");
			String dataseturl = request.getParameter("dataseturl");
			RequestDispatcher rd = request.getRequestDispatcher("/THREDDSServlet?command=getgridlist&dataseturl=" + dataseturl);
			rd.forward(request, response);
			return;
		}
		
		if ("getcatalog".equals(command)) {
			log.debug("User is attempting to grab catalog from remote THREDDS server");
			String url = request.getParameter("url");
			RequestDispatcher rd = request.getRequestDispatcher("/THREDDSServlet?command=getcatalog&url=" + url);
			rd.forward(request, response);
			return;
		}
		
		if ("gettimerange".equals(command)) {
			String datasetUrl = request.getParameter("dataseturl");
			String gridSelection = request.getParameter("grid");
			RequestDispatcher rd = request.getRequestDispatcher("/THREDDSServlet?command=gettimerange&dataseturl=" + datasetUrl + "&grid=" + gridSelection);
			rd.forward(request, response);
			return;
		}
		
		if ("submitforprocessing".equals(command)) {
			RequestDispatcher rd = request.getRequestDispatcher("/THREDDSServlet?command=submitforprocessing");
			rd.forward(request, response);
			return;
		}
		
	}

	public static void sendXml(String xml, HttpServletResponse response) throws IOException {
		 ServletOutputStream stream = null;
		 BufferedInputStream bis = new BufferedInputStream(new ByteArrayInputStream(xml.getBytes()));
		 try {
			stream = response.getOutputStream();
			response.setContentType("text/xml");
			response.setCharacterEncoding("utf-8");
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
	
	public static void sendXml(XmlReplyBean xmlReply, HttpServletResponse response) throws IOException {
		 String xml = xmlReply.toXml();
		 log.debug(xml);
		 ServletOutputStream stream = null;
		 BufferedInputStream bis = new BufferedInputStream(new ByteArrayInputStream(xml.getBytes()));
		 try {
			stream = response.getOutputStream();
			response.setContentType("text/xml");
			response.setCharacterEncoding("utf-8");
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
