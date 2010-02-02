package gov.usgs.gdp.servlet;

import gov.usgs.gdp.bean.AckBean;
import gov.usgs.gdp.bean.ErrorBean;
import gov.usgs.gdp.bean.FilesBean;
import gov.usgs.gdp.bean.MessageBean;
import gov.usgs.gdp.bean.XmlReplyBean;
import gov.usgs.gdp.helper.CookieHelper;
import gov.usgs.gdp.helper.FileHelper;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class FileSelectionServlet
 */
public class GeoServerServlet extends HttpServlet {
	
	private static final String geoServerURL = new String("http://localhost:8080/geoserver/");
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GeoServerServlet() {
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
		String command = (request.getParameter("command") == null) ? "" : request.getParameter("command");
		XmlReplyBean xmlReply = null;
		if ("createdatastore".equals(command)) {
			
			String shapefileName = request.getParameter("shapefile");
			
			Cookie userDirectoryCookie = CookieHelper.getCookie(request, "userDirectory");
			String userDirectory = "";
			if (userDirectoryCookie != null && FileHelper.doesDirectoryOrFileExist(userDirectoryCookie.getValue())) {
				userDirectory = userDirectoryCookie.getValue();
			} else {
				userDirectory = System.getProperty("applicationTempDir");
			}
			
			// TODO: see if we can put sample shape files in root of temp dir, not Sample_Files/...
			String shapefileLoc = userDirectory + "Sample_Files/Shapefiles/" + shapefileName + ".shp";
			
			String[] dir = userDirectory.split(FileHelper.getSeparator());
			// set the workspace to the name of the temp directory
			String workspace = dir[dir.length - 1];

			URL workspacesURL = new URL(geoServerURL + "rest/workspaces/");
			if (!workspaceExists(workspace)) {
				String workspaceXML = createWorkspaceXML(workspace);
				sendXMLPostPacket(workspacesURL, workspaceXML);
			}
			
			if (!dataStoreExists(workspace, shapefileName)) {
				String dataStoreXML = createDataStoreXML(shapefileName, workspace, shapefileLoc);
				URL dataStoresURL = new URL(workspacesURL + workspace + "/datastores/");
				sendXMLPostPacket(dataStoresURL, dataStoreXML);
				
				String featureTypeXML = createFeatureTypeXML(shapefileName, workspace);
				URL featureTypesURL = new URL(dataStoresURL + shapefileName +  "/featuretypes.xml");
				sendXMLPostPacket(featureTypesURL, featureTypeXML);
			}
			
			String layerLoc = new String(geoServerURL + "wms/reflect?" + 
										 "layers=" + workspace + ":" + shapefileName +
										 "&format=application/openlayers");
			
			// send back ack and location of layer
			xmlReply = new XmlReplyBean(AckBean.ACK_OK, new MessageBean(layerLoc));
			RouterServlet.sendXml(xmlReply, response);
			return;
		}
	}
	
	boolean workspaceExists(String workspace) throws IOException {
		try {
			sendGetPacket(new URL(geoServerURL + "rest/workspaces/" + workspace + ".xml"));
		} catch (FileNotFoundException e) {
			return false;
		}
		
		return true;
	}
	
	boolean dataStoreExists(String workspace, String dataStore) throws IOException {
		try {
			sendGetPacket(new URL(geoServerURL + "rest/workspaces/" + workspace + "/datastores/" + dataStore + ".xml"));
		} catch (FileNotFoundException e) {
			return false;
		}
		
		return true;
	}
	
	void sendGetPacket(URL url) throws IOException {
		HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
		httpConnection.setDoOutput(true);
		httpConnection.setRequestMethod("GET");
		
		// For some reason this has to be here for the packet above to be sent //
		BufferedReader reader = new BufferedReader(new InputStreamReader(httpConnection.getInputStream()));
		String line;
		while ((line = reader.readLine()) != null) {
			//System.out.println(line);
		}
		reader.close();
		/////////////////////////////////////////////////////////////////////////
	}
	
	void sendXMLPostPacket(URL url, String xml) throws IOException {
		HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
		httpConnection.setDoOutput(true);
		httpConnection.setRequestMethod("POST");
		httpConnection.addRequestProperty("Content-Type", "text/xml");
		OutputStreamWriter workspacesWriter = new OutputStreamWriter(httpConnection.getOutputStream());
		workspacesWriter.write(xml);
		workspacesWriter.close();
		
		// For some reason this has to be here for the packet above to be sent //
		BufferedReader reader = new BufferedReader(new InputStreamReader(httpConnection.getInputStream()));
		String line;
		while ((line = reader.readLine()) != null) {
			System.out.println(line);
		}
		reader.close();
		/////////////////////////////////////////////////////////////////////////
	}
	
	String createWorkspaceXML(String workspace) {
		return new String("<workspace><name>" + workspace + "</name></workspace>");
	}
	
	String createDataStoreXML(String name, String workspace, String url) {
		
		return new String(
				"<dataStore>" +
				"  <name>" + name + "</name>" +
				"  <type>Shapefile</type>" +
				"  <enabled>true</enabled>" +
				"  <workspace>" +
				"    <name>" + workspace + "</name>" +
				"  </workspace>" +
				"  <connectionParameters>" +
				"    <entry key=\"memory mapped buffer\">true</entry>" +
				"    <entry key=\"create spatial index\">true</entry>" +
				"    <entry key=\"charset\">ISO-8859-1</entry>" +
				"    <entry key=\"url\">file:" + url + "</entry>" +
				"    <entry key=\"namespace\">http://" + workspace + "</entry>" +  // default namespace = "http://" + workspace
				"  </connectionParameters>" +
				"</dataStore>");
	}
	
	String createFeatureTypeXML(String name, String workspace) {
		
		return new String(
				"<featureType>" +
				"  <name>" + name + "</name>" +
				"  <nativeName>" + name + "</nativeName>" +
				"  <namespace>" +
				"    <name>" + workspace + "</name>" +
				"  </namespace>" +
				"  <title>" + name + "</title>" +
				"  <enabled>true</enabled>" +
				"  <store class=\"dataStore\">" +
				"    <name>" + name + "</name>" +
				"    <atom:link xmlns:atom=\"http://www.w3.org/2005/Atom\" rel=\"alternate\" " +
				"			href=\"http://localhost:8080/geoserver/rest/workspaces/usgs/datastores/counties.xml\" " +
				"			type=\"application/xml\"/>" +
				"  </store>" +
				"</featureType>");
	}
}
