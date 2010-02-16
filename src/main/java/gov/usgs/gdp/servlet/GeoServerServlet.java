package gov.usgs.gdp.servlet;

import gov.usgs.gdp.bean.AckBean;
import gov.usgs.gdp.bean.AvailableFilesBean;
import gov.usgs.gdp.bean.ErrorBean;
import gov.usgs.gdp.bean.FilesBean;
import gov.usgs.gdp.bean.MessageBean;
import gov.usgs.gdp.bean.XmlReplyBean;
import gov.usgs.gdp.helper.FileHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.IllegalFormatConversionException;

import javax.servlet.ServletException;
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

		if ("createdatastore".equals(command)) {
			
			String shapefileName = request.getParameter("shapefile");
			String userDirectory = request.getParameter("userdirectory");
			
			String appTempDir = System.getProperty("applicationTempDir");
			AvailableFilesBean afb = null;
			try {
				afb = AvailableFilesBean.getAvailableFilesBean(appTempDir, userDirectory);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
				sendReply(response, AckBean.ACK_FAIL, "Invalid directories.");
				return;
			}
			
			// Couldn't pull any files. Send an error to the caller.
			if (afb == null) {
				sendReply(response, AckBean.ACK_FAIL, "Could not find any files to work with.");
				return;
			}
			
			
			// search for the requested shapefile
			String directory = null;
			for (FilesBean fb : afb.getUserFileList()) {
				if (fb.getName().equals(shapefileName))
					directory = userDirectory;
			}
			
			// if the file wasn't found in the user directory, search the sample files
			if (directory == null) {
				for (FilesBean fb : afb.getExampleFileList()) {
					if (fb.getName().equals(shapefileName))
						directory = appTempDir + "Sample_Files/Shapefiles/";
				}
			}
			
			// Couldn't pull any files. Send an error to the caller.
			if (directory == null) {
				sendReply(response, AckBean.ACK_FAIL, "Could not find any files to work with.");
				return;
			}
			
			
			String shapefileLoc = directory + shapefileName + ".shp";
			
			String[] dir = directory.split(FileHelper.getSeparator());
			// set the workspace to the name of the temp directory
			String workspace = dir[dir.length - 1];

			// create the workspace if it doesn't already exist
			URL workspacesURL = new URL(geoServerURL + "rest/workspaces/");
			if (!workspaceExists(workspace)) {
				String workspaceXML = createWorkspaceXML(workspace);
				sendPacket(workspacesURL, "POST", "text/xml", workspaceXML);
			}

			URL dataStoresURL = new URL(workspacesURL + workspace + "/datastores/");
			String dataStoreXML = createDataStoreXML(shapefileName, workspace, shapefileLoc);
			if (!dataStoreExists(workspace, shapefileName)) {
				//deleteLayer(workspace, shapefileName);
				//deleteFeatureType(workspace, shapefileName);
				//deleteDateStore(workspace, shapefileName);
			
				// POST the datastore to create it if it doesn't exist
				sendPacket(dataStoresURL, "POST", "text/xml", dataStoreXML);
			
				// create featuretype based on the datastore
				String featureTypeXML = createFeatureTypeXML(shapefileName, workspace);
				URL featureTypesURL = new URL(dataStoresURL + shapefileName +  "/featuretypes.xml");
				sendPacket(featureTypesURL, "POST", "text/xml", featureTypeXML);
			} else {
				// otherwise PUT it to make sure the shapefiles exist
				sendPacket(new URL(dataStoresURL + shapefileName + ".xml"), "PUT", "text/xml", dataStoreXML);
			}
			
			// send back ack with workspace and layer names
			sendReply(response, AckBean.ACK_OK, workspace, shapefileName);

		} else if ("createstyledmap".equals(command)) {
			DateFormat df = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
			Date date;
			try {
				date = df.parse("15 Jan 1895 00:00:00 GMT");
			} catch (ParseException e) {
				e.printStackTrace();
				sendReply(response, AckBean.ACK_FAIL, "Could not parse requested date.");
				return;
			}
			
			String sld = createStyle(new File("/Users/razoerb/Desktop/temp.csv"), date, "weight_sum");
			
			if (sld == null) {
				sendReply(response, AckBean.ACK_FAIL, "Could not create map style.");
				return;
			}
			
			sendPacket(new URL(geoServerURL + "rest/styles?name=colors&overwrite=true"), 
					"POST", "application/vnd.ogc.sld+xml", sld, "name", "colors");
			
			sendReply(response, AckBean.ACK_OK);
		}
	}
	
	boolean workspaceExists(String workspace) throws IOException {
		try {
			sendPacket(new URL(geoServerURL + "rest/workspaces/" + workspace + ".xml"), "GET", null, null);
		} catch (FileNotFoundException e) {
			return false;
		}
		
		return true;
	}
	
	boolean dataStoreExists(String workspace, String dataStore) throws IOException {
		try {
			URL url = new URL(geoServerURL + "rest/workspaces/" + workspace + "/datastores/" + dataStore + ".xml");
			sendPacket(url, "GET", null, null);
		} catch (FileNotFoundException e) {
			return false;
		}
		
		return true;
	}
	
	/*void deleteFeatureType(String workspace, String dataStore) throws IOException {
		URL url = new URL(geoServerURL + "rest/workspaces/" + workspace + "/datastores/" + dataStore +
						  "/featuretypes/" + dataStore);
		HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
		httpConnection.setRequestMethod("DELETE");
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(httpConnection.getInputStream()));
		String line;
		while ((line = reader.readLine()) != null) {
			//System.out.println(line);
		}
		reader.close();
	}
	
	void deleteDateStore(String workspace, String dataStore) throws IOException {
		URL url = new URL(geoServerURL + "rest/workspaces/" + workspace + "/datastores/" + dataStore);
		HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
		httpConnection.setRequestMethod("DELETE");
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(httpConnection.getInputStream()));
		String line;
		while ((line = reader.readLine()) != null) {
			//System.out.println(line);
		}
		reader.close();
	}*/
	
	void sendPacket(URL url, String requestMethod, String contentType, String content, 
			String... requestProperties) throws IOException {
		
		HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
		httpConnection.setDoOutput(true);
		httpConnection.setRequestMethod(requestMethod);
		
		if (contentType != null)
			httpConnection.addRequestProperty("Content-Type", contentType);
		
		for (int i = 0; i < requestProperties.length; i += 2) {
			httpConnection.addRequestProperty(requestProperties[i], requestProperties[i+1]);
		}
		
		if (content != null) {
			OutputStreamWriter workspacesWriter = new OutputStreamWriter(httpConnection.getOutputStream());
			workspacesWriter.write(content);
			workspacesWriter.close();
		}
		
		// For some reason this has to be here for the packet above to be sent //
		BufferedReader reader = new BufferedReader(new InputStreamReader(httpConnection.getInputStream()));
		String line;
		while ((line = reader.readLine()) != null) {
			//System.out.println(line);
		}
		reader.close();
	}
	
	void sendReply(HttpServletResponse response, int status, String... messages) throws IOException {
		XmlReplyBean xmlReply = new XmlReplyBean(status, new MessageBean(messages));
		RouterServlet.sendXml(xmlReply, response);
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
	
	String createStyle(File data, Date date, String stat) throws IOException {
		
		final String fieldSep = ",";
		DateFormat df = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");

		ArrayList<Float> requestedStats = new ArrayList<Float>();
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(data));
			String line;
			
			// Figure out how many values per GRIDCODE there are
			line = reader.readLine();
			String GridCodes[] = line.split(fieldSep);
			if (!"ALL".equals(GridCodes[GridCodes.length - 1])) {
				System.out.println("ERROR: Last GRIDCODE is not ALL");
				return null;
			}
			
			int statsPerGC = 0;
			String gc = GridCodes[GridCodes.length - 1];
			while ("ALL".equals(gc)) {
				statsPerGC++;
				gc = GridCodes[GridCodes.length - 1 - statsPerGC];
			}
			
			System.out.println("Stats per GRIDCODE: " + statsPerGC);
			
			// Find location of chosen stat
			line = reader.readLine();
			String stats[] = line.split(fieldSep);
			if (!"timestep".equals(stats[0])) {
				System.out.println("ERROR: First value is not timestep");
				return null;
			}
			
			int firstStatIndex = 1;
			String val = stats[firstStatIndex];
			while (!stat.equals(val)) {
				firstStatIndex++;
				val = stats[firstStatIndex];
				
				if (firstStatIndex > statsPerGC) {
					System.out.println("ERROR: stat doesn't exist");
					return null;
				}
			}
			
			System.out.println("First stat index: " + firstStatIndex);
			
			// Find chosen date
			line = reader.readLine();
			String firstValue = line.split(fieldSep)[0];
			while (reader.ready()) {
				
				if ("ALL".equals(firstValue)) {
					System.out.println("ERROR: date not found");
					return null;
				}

				if (df.parse(firstValue).compareTo(date) == 0) {
					break;
				}
				
				line = reader.readLine();
				firstValue = line.split(fieldSep)[0];
			}
			
			String values[] = line.split(fieldSep);
			//							 don't read in totals at end
			for (int i = firstStatIndex; i < values.length - statsPerGC; i += statsPerGC) {
				requestedStats.add(Float.parseFloat(values[i]));
			}
		}
		catch (IOException e) {
			System.err.println("Error parsing file");
			e.printStackTrace();
		}
		catch (ParseException e) {
			System.err.println("Error parsing date");
			e.printStackTrace();
		}
		
		float maxVal = Float.NEGATIVE_INFINITY;
		float minVal = Float.POSITIVE_INFINITY;
		for (Float f : requestedStats) {
			if (f < minVal) minVal = f;
			if (f > maxVal) maxVal = f;
		}
		float spread = maxVal - minVal;
		if (spread == 0) spread = 1;
		
		String style = new String(
				"<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>" +
				"<StyledLayerDescriptor version=\"1.0.0\"" +
				"    xsi:schemaLocation=\"http://www.opengis.net/sld StyledLayerDescriptor.xsd\"" +
				"    xmlns=\"http://www.opengis.net/sld\"" +
				"    xmlns:ogc=\"http://www.opengis.net/ogc\"" +
				"    xmlns:xlink=\"http://www.w3.org/1999/xlink\"" +
				"    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">" +
				"  <NamedLayer>" +
				"    <Name>Colors</Name>" +
				"    <UserStyle>" +
				"      <Title>Per-GRIDCODE coloring</Title>" +
				"      <FeatureTypeStyle>");
		
		int gridCode = 1;
		String color;
		for (Float f : requestedStats) {

			float temp = -(f - minVal) / spread + 1;
			temp = temp * 255;
			String blgr = String.format("%02x", (int) temp);

			color = "FF" + blgr + blgr;
			
			style += "   <Rule>" +
				"          <ogc:Filter>" +
				"            <ogc:PropertyIsEqualTo>" +
				"              <ogc:PropertyName>GRIDCODE</ogc:PropertyName>" +
				"              <ogc:Literal>" + gridCode + "</ogc:Literal>" +
				"            </ogc:PropertyIsEqualTo>" +
				"          </ogc:Filter>" +
				"          <PolygonSymbolizer>" +
				"            <Fill>" +
				"              <CssParameter name=\"fill\">#" + color + "</CssParameter>" +
				"            </Fill>" +
				"			 <Stroke>" +
	            "			   <CssParameter name=\"stroke\">#000000</CssParameter>" +
	            "			   <CssParameter name=\"stroke-width\">1</CssParameter>" +
	            "			 </Stroke>" +
				"          </PolygonSymbolizer>" +
				"        </Rule>";
			
			gridCode++;
		}
		
		style += "	   </FeatureTypeStyle>" +
				"    </UserStyle>" +
				"  </NamedLayer>" +
				"</StyledLayerDescriptor>" ;
		
		return style;
	}
}
