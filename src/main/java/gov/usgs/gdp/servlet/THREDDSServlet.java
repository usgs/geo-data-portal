package gov.usgs.gdp.servlet;

import gov.usgs.gdp.bean.AckBean;
import gov.usgs.gdp.bean.AttributeBean;
import gov.usgs.gdp.bean.DataSetBean;
import gov.usgs.gdp.bean.ErrorBean;
import gov.usgs.gdp.bean.FilesBean;
import gov.usgs.gdp.bean.GridBean;
import gov.usgs.gdp.bean.PassThroughXmlResponseBean;
import gov.usgs.gdp.bean.ShapeFileSetBean;
import gov.usgs.gdp.bean.XmlBean;
import gov.usgs.gdp.bean.XmlReplyBean;
import gov.usgs.gdp.helper.CookieHelper;
import gov.usgs.gdp.helper.FileHelper;
import gov.usgs.gdp.helper.THREDDSServerHelper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;

import thredds.catalog.InvAccess;
import ucar.unidata.io.http.HTTPRandomAccessFile;

/**
 * Servlet implementation class THREDDSServlet
 */
public class THREDDSServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log = Logger.getLogger(THREDDSServlet.class);
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public THREDDSServlet() {
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
		String command = request.getParameter("command");
		XmlReplyBean xmlReply = null;
		
		if ("getcatalog".equals(command)) {
			String url = request.getParameter("url");
			if (url == null || "".equals(url)) {
				xmlReply = new XmlReplyBean(AckBean.ACK_FAIL, new ErrorBean(ErrorBean.ERR_MISSING_THREDDS));
				RouterServlet.sendXml(xmlReply, response);
				return;
			}
			HttpClient client = new HttpClient();
			HttpMethod method = new GetMethod(url);
			try {
				int statusCode = client.executeMethod(method);

				if (statusCode != HttpStatus.SC_OK) {
					System.err.println("Method failed: " + method.getStatusLine());
				}
				// Read the response body.
				byte[] responseBody = method.getResponseBody();
				// Deal with the response.
				// Use caution: ensure correct character encoding and is not binary data
				String xmlResponse = new String(responseBody);
				method.releaseConnection();
				RouterServlet.sendXml(xmlResponse, response);
				return;
				
			} catch (HttpException e) {
				System.err.println("Fatal protocol violation: " + e.getMessage());
				e.printStackTrace();
			} catch (IOException e) {
				System.err.println("Fatal transport error: " + e.getMessage());
				e.printStackTrace();
			} finally {
				// Release the connection.
				method.releaseConnection();
			}  


		}
		
		if ("getdatasetlist".equals(command)) {
			log.debug("User has chosen to get datasets from server");
			
			// Grab what we need to work with for this request
			String hostname = request.getParameter("hostname");
			String portString = request.getParameter("port");
			String uri = request.getParameter("uri");
			if (portString == null || "".equals(portString)) portString = "80";
			int port = 80;
			if (hostname == null || "".equals(hostname)
					|| uri == null || "".equals(uri)) {
				xmlReply = new XmlReplyBean(AckBean.ACK_FAIL, new ErrorBean(ErrorBean.ERR_MISSING_PARAM));
				RouterServlet.sendXml(xmlReply, response);
				return;
			}
			
			// Check that a port was provided and correct			
			try {
				if (portString != null && !"".equals(portString) && !"null".equals(portString)) {
					port = Integer.parseInt(portString);
				}
			} catch (NumberFormatException e) {
				xmlReply = new XmlReplyBean(AckBean.ACK_FAIL, new ErrorBean(ErrorBean.ERR_PORT_INCORRECT));
				RouterServlet.sendXml(xmlReply, response);
				return;
			}

			/*List<XmlBean> datasetBeanList = (List<XmlBean>) THREDDSServerHelper.getDatasetListFromServer(hostname, port, uri);
			if (datasetBeanList == null || datasetBeanList.isEmpty()) {
				xmlReply = new XmlReplyBean(AckBean.ACK_FAIL, new ErrorBean(ErrorBean.ERR_MISSING_DATASET));
				RouterServlet.sendXml(xmlReply, response);
				return;
			}*/
			/*return;
			xmlReply = new XmlReplyBean(AckBean.ACK_OK, datasetBeanList);
			RouterServlet.sendXml(xmlReply, response);
			return;*/
			
		}
		
		if ("getgridlist".equals(command)) {
			log.debug("User has chosen to list shapefile attributes");
			
			// Grab what we need to work with for this request
			String datasetUrl = request.getParameter("dataseturl");
			if (datasetUrl == null || "".equals(datasetUrl)) {
				xmlReply = new XmlReplyBean(AckBean.ACK_FAIL, new ErrorBean(ErrorBean.ERR_MISSING_PARAM));
				RouterServlet.sendXml(xmlReply, response);
				return;
			}

			List<XmlBean> gridBeanList = THREDDSServerHelper.getGridBeanListFromServer(datasetUrl);
			XmlReplyBean xrb = new XmlReplyBean(AckBean.ACK_OK, gridBeanList);
			RouterServlet.sendXml(xrb, response);
			return;
			
		}
	}

}
