package gov.usgs.gdp.servlet;

import gov.usgs.gdp.bean.ErrorBean;
import gov.usgs.gdp.bean.ShapeFileSetBean;
import gov.usgs.gdp.io.FileHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.geotools.data.FeatureSource;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeType;

/**
 * Servlet implementation class FileProcessServlet
 */
public class FileProcessServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public FileProcessServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String action = (request.getParameter("action") == null) ? "" : request.getParameter("action").toLowerCase();
		
		ErrorBean errorBean = new ErrorBean();
		String forwardTo = "";
		
		if (action == null || "".equals(action)) {
			errorBean.getErrors().add("Unable to parse action.");
		} else if ("step1".equals(action)) {
			List<ShapeFileSetBean> shapeFileSetBeanList = (List<ShapeFileSetBean>) request.getSession().getAttribute("shapeFileSetBeanList");
			if (shapeFileSetBeanList == null) {
				errorBean.getErrors().add("Unable to retrieve shape file set lists.");
				forwardTo = "/jsp/fileSelection.jsp";
			} else {
				
				String[] checkboxItems = request.getParameterValues("fileName");
				if (checkboxItems != null) {
					
					// Get the subset of ShapeFile sets the user wants to work on
					List<ShapeFileSetBean> shpFilesSetSubList = new ArrayList<ShapeFileSetBean>();
					for (String item : checkboxItems) {
						for (ShapeFileSetBean shapeFileSetBean : shapeFileSetBeanList) {
							if (shapeFileSetBean.getName().equals(item)) {
								shpFilesSetSubList.add(shapeFileSetBean);
							}
						}
					}
					
					// Populate the attribute values of each ShapeFileSet
					for (ShapeFileSetBean shapeFileSetBean : shpFilesSetSubList) {
						shapeFileSetBean.setAttributeList(ShapeFileSetBean.getAttributeListFromBean(shapeFileSetBean));
					}
					forwardTo = "/jsp/attributeSelection.jsp";
					
				} else {
					errorBean.getErrors().add("You must select at least one file to process.");
					forwardTo = "/jsp/fileSelection.jsp";
				}
			}
		}

		request.setAttribute("errorBean", errorBean);
		RequestDispatcher rd = request.getRequestDispatcher(forwardTo);
		rd.forward(request, response);
	}


}
