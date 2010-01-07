package gov.usgs.gdp.servlet;

import gov.usgs.gdp.analysis.GeoToolsFileAnalysis;
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
 * Servlet implementation class GeoToolsServlet
 */
public class GeoToolsServlet extends HttpServlet {
	private static org.apache.log4j.Logger log = Logger.getLogger(GeoToolsServlet.class);
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GeoToolsServlet() {
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
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String action	= (request.getParameter("action") == null) ? "" : request.getParameter("action").toLowerCase();
		String function = (request.getParameter("function") == null) ? "" : request.getParameter("function").toLowerCase();
		
		List<List<String>> summaryResults = null; 
		if ("processfiles".equals(action)) {
			if ("summarize".equals(function)) {
				summaryResults = summarize(request);
				request.setAttribute("summaryResults", summaryResults);
			}
		} 
		
		RequestDispatcher rd = request.getRequestDispatcher("/jsp/geotoolsProcessing.jsp");
		rd.forward(request, response);
	}

	
	
	private List<List<String>> summarize(HttpServletRequest request) {
		String applicationTempDir = System.getProperty("applicationTempDir");
		List<List<String>> result = null;
		
		String[] checkboxItems = request.getParameterValues("fileName");
		if (checkboxItems != null) {
			
			if (result == null) {
				result = new ArrayList<List<String>>();
			}
			
			for (String checkboxItem : checkboxItems) {
				List<String> fileListing = new ArrayList<String>();
				String suffix = checkboxItem.substring(checkboxItem.indexOf('.') + 1).toLowerCase();
				if ("dbf".equals(suffix)) {										
					File dbFile = FileHelper.findFile(checkboxItem, applicationTempDir);
					if (dbFile == null || dbFile.length() == 0) {
						fileListing.add("Unable to load: " + checkboxItem);
						result.add(fileListing);
					} else {
						log.debug("File " + checkboxItem + " being summarized.");
						fileListing.add("File: " + checkboxItem);
						fileListing.addAll(1, GeoToolsFileAnalysis.getDBaseFileSummary(dbFile));
						result.add(fileListing);
					}
				} else if ("shp".equals(suffix)) {
					File shpFile = FileHelper.findFile(checkboxItem, applicationTempDir);
					if (shpFile == null || shpFile.length() == 0) {
						fileListing.add("Unable to load :" + checkboxItem);
						result.add(fileListing);
					} else {
						log.debug("File " + checkboxItem + " being summarized.");
						fileListing.add("File: " + checkboxItem);
						fileListing.addAll(1, GeoToolsFileAnalysis.getShapeFileHeaderSummary(shpFile));
						fileListing.addAll(2, GeoToolsFileAnalysis.getShapeFileSummary(shpFile));
						result.add(fileListing);
					}
				}
			}
		}
		return result;
	}

}
