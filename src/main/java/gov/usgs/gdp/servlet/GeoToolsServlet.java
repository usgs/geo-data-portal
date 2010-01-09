package gov.usgs.gdp.servlet;

import gov.usgs.gdp.analysis.GeoToolsFileAnalysis;
import gov.usgs.gdp.bean.SummaryBean;
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
		
		List<SummaryBean> summaryResults = null; 
		if ("summarize".equals(action)) {
			summaryResults = summarize(request);
			request.setAttribute("summaryBeanList", summaryResults);
		} 
		
		RequestDispatcher rd = request.getRequestDispatcher("/jsp/showSummaryResults.jsp");
		rd.forward(request, response);
	}

	
	
	private List<SummaryBean> summarize(HttpServletRequest request) {
		String applicationTempDir = System.getProperty("applicationTempDir");
		List<SummaryBean> result = new ArrayList<SummaryBean>();
		
		String[] checkboxItems = request.getParameterValues("fileName");
		if (checkboxItems != null) {
			
			for (String checkboxItem : checkboxItems) {
				String fileName	= checkboxItem;
				List<String> summaryResults = new ArrayList<String>();
				String suffix = checkboxItem.substring(checkboxItem.indexOf('.') + 1).toLowerCase();
				
				if ("dbf".equals(suffix)) {										
					File dbFile = FileHelper.findFile(checkboxItem, applicationTempDir);
					if (dbFile == null || dbFile.length() == 0) {						
						summaryResults.add("Unable to load: " + checkboxItem);
					} else {
						log.debug("File " + checkboxItem + " being summarized.");
						summaryResults.addAll(0, GeoToolsFileAnalysis.getDBaseFileSummary(dbFile));
					}
				} else if ("shp".equals(suffix)) {
					File shpFile = FileHelper.findFile(checkboxItem, applicationTempDir);
					if (shpFile == null || shpFile.length() == 0) {
						summaryResults.add("Unable to load :" + checkboxItem);
					} else {
						log.debug("File " + checkboxItem + " being summarized.");
						summaryResults.addAll(0, GeoToolsFileAnalysis.getShapeFileHeaderSummary(shpFile));
						summaryResults.addAll(1, GeoToolsFileAnalysis.getShapeFileSummary(shpFile));
					}
				}
				SummaryBean summaryBean = new SummaryBean(checkboxItem, summaryResults);
				result.add(summaryBean);
			}
		} 
		return result;
	}

}
