package gov.usgs.gdp.servlet;

import gov.usgs.gdp.bean.AckBean;
import gov.usgs.gdp.bean.XmlReplyBean;

import java.io.IOException;
import java.io.OutputStream;
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
		XmlReplyBean xmlReply = null;
		if ("createdatastore".equals(command)) {
			
			String shapefile = request.getParameter("shapefile");
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
			
			String shapefileLoc = System.getProperty("applicationTempDir") + shapefile;
			System.out.println(shapefileLoc);

			String workspace = "blah";
			
			String geoServerLoc = new String("http://localhost:8080/geoserver/rest/workspaces/");
			URL geoServerURL = new URL(geoServerLoc + workspace + "/datastores/" + workspace + ".xml");
			HttpURLConnection geoServerConnection = (HttpURLConnection) geoServerURL.openConnection();
			geoServerConnection.setRequestMethod("POST");
			geoServerConnection.addRequestProperty("Content-Type", "text/xml");
			
			//OutputStream geoServerConnOS = geoServerConnection.getOutputStream();
			//geoServerConnOS.write(b);
			
			
		  /*<dataStore>
			  <name>counties</name>
			  <type>Shapefile</type>
			  <enabled>true</enabled>
			  <workspace>
			    <name>usgs</name>
			    <atom:link xmlns:atom="http://www.w3.org/2005/Atom" rel="alternate" href="http://localhost:8080/geoserver/rest/workspaces/WorkspaceInfoImpl-3c812e7b:1267b99a288:-8000.xml" type="application/xml"/>
			  </workspace>
			  <connectionParameters>
			    <entry key="memory mapped buffer">true</entry>
			    <entry key="create spatial index">true</entry>
			    <entry key="charset">ISO-8859-1</entry>
			    <entry key="url">file:/Users/razoerb/Documents/workspace/GDP/src/main/resources/Sample_Files/Shapefiles/usa_counties.shp</entry>
			    <entry key="namespace">http://www.usgs.gov</entry>
			  </connectionParameters>
			  <featureTypes>
			    <atom:link xmlns:atom="http://www.w3.org/2005/Atom" rel="alternate" href="http://localhost:8080/geoserver/rest/workspaces/usgs/datastores/counties/featuretypes.xml" type="application/xml"/>
			  </featureTypes>
			</dataStore>*/
			
		  /*<featureType>
			  <name>usa_counties</name>
			  <nativeName>usa_counties</nativeName>
			  <namespace>
			    <name>usgs</name>
			    <atom:link xmlns:atom="http://www.w3.org/2005/Atom" rel="alternate" href="http://localhost:8080/geoserver/rest/namespaces/usgs.xml" type="application/xml"/>
			  </namespace>
			  <title>usa_counties</title>
			  <enabled>true</enabled>
			  <store class="dataStore">
			    <name>counties</name>
			    <atom:link xmlns:atom="http://www.w3.org/2005/Atom" rel="alternate" href="http://localhost:8080/geoserver/rest/workspaces/usgs/datastores/counties.xml" type="application/xml"/>
			  </store>
			</featureType>*/		
			
			
			xmlReply = new XmlReplyBean(AckBean.ACK_OK, new AckBean(AckBean.ACK_OK));
			RouterServlet.sendXml(xmlReply, response);
			return;
		}
	}
}
