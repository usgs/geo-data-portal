package gov.usgs.gdp.servlet;

import gov.usgs.gdp.bean.AckBean;
import gov.usgs.gdp.bean.XmlReplyBean;
import gov.usgs.gdp.helper.FileHelper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class FileSelectionServlet
 */
public class GeoServerServlet extends HttpServlet {
       
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
			/*Cookie userDirectoryCookie = CookieHelper.getCookie(request, "userDirectory");
			String userDirectory = "";
			if (userDirectoryCookie != null) {
				if (FileHelper.doesDirectoryOrFileExist(userDirectoryCookie.getValue())) {
					userDirectory = userDirectoryCookie.getValue();
				}
			}
			
			List<FilesBean> filesBeanList = FilesBean.getFilesBeanSetList(System.getProperty("applicationTempDir"), userDirectory);
			if (filesBeanList == null) {
				xmlReply = new XmlReplyBean(AckBean.ACK_FAIL, new ErrorBean(ErrorBean.ERR_FILE_NOT_FOUND));
				RouterServlet.sendXml(xmlReply, response);
				return;
			}*/
			
			String geoServerURL = new String("http://localhost:8080/geoserver/");
			String tempDir = System.getProperty("applicationTempDir");
			String shapefileLoc = tempDir + "Sample_Files/Shapefiles/" + shapefileName + ".shp";
			
			String[] dir = tempDir.split(FileHelper.getSeparator());
			// set the workspace to the name of the temp directory
			String workspace = dir[dir.length - 1];

			String workspaceXML = createWorkspaceXML(workspace);
			String dataStoreXML = createDataStoreXML(shapefileName, workspace, shapefileLoc);
			String featureTypeXML = createFeatureTypeXML(shapefileName, workspace);
			
			URL workspacesURL = new URL(geoServerURL + "rest/workspaces/");
			URL dataStoresURL = new URL(workspacesURL + workspace + "/datastores/");
			URL featureTypesURL = new URL(dataStoresURL + shapefileName +  "/featuretypes.xml");
			
			sendXMLPacket(workspacesURL, workspaceXML);
			sendXMLPacket(dataStoresURL, dataStoreXML);
			sendXMLPacket(featureTypesURL, featureTypeXML);
			
			xmlReply = new XmlReplyBean(AckBean.ACK_OK, new AckBean(AckBean.ACK_OK));
			RouterServlet.sendXml(xmlReply, response);
			return;
		}
	}
	
	void sendXMLPacket(URL url, String xml) throws IOException {
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
