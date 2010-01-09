package gov.usgs.gdp.servlet;

import gov.usgs.gdp.analysis.NetCDFUtility;
import gov.usgs.gdp.bean.ErrorBean;
import gov.usgs.gdp.bean.ShapeFileSetBean;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import thredds.catalog.InvAccess;
import thredds.catalog.InvCatalog;
import thredds.catalog.InvCatalogFactory;
import thredds.catalog.InvDataset;
import thredds.catalog.ServiceType;
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
					request.getSession().setAttribute("shapeFileSetBeanSubsetList", shpFilesSetSubList);
					forwardTo = "/jsp/attributeSelection.jsp";
					
				} else {
					errorBean.getErrors().add("You must select at least one file to process.");
					forwardTo = "/jsp/fileSelection.jsp";
				}
			}
		} else if ("step2".equals(action)) { // Attributes chosen, set up feature list 
			String[] attributeSelections = request.getParameterValues("attributeSelection");
			List<ShapeFileSetBean> shapeFileSetBeanSubsetList = (List<ShapeFileSetBean>) request.getSession().getAttribute("shapeFileSetBeanSubsetList");
			
			// Set the chosen attribute on the ShapeFileSetBeans
			for (String attributeSelection : attributeSelections) {
				String attributeAppliesTo = attributeSelection.substring(0, attributeSelection.indexOf("::"));
				String attribute = attributeSelection.substring(attributeSelection.indexOf("::") + 2);
				for (ShapeFileSetBean shapeFileSetBean : shapeFileSetBeanSubsetList) {
					if (shapeFileSetBean.getName().equals(attributeAppliesTo)) {
						shapeFileSetBean.setChosenAttribute(attribute);
					}
				}
			}
			
			// Pull Feature Lists
			for (ShapeFileSetBean shapeFileSetBean : shapeFileSetBeanSubsetList) {
				shapeFileSetBean.setFeatureList(ShapeFileSetBean.getFeatureListFromBean(shapeFileSetBean));				
			}
			
			request.getSession().setAttribute("shapeFileSetBeanSubsetList", shapeFileSetBeanSubsetList);			
			forwardTo = "/jsp/featureSelection.jsp";
		} else if ("step3".equals(action)) { 
			// Set the chosen feature to work with on the bean
			String[] featureSelections = request.getParameterValues("featureSelection");			
			List<ShapeFileSetBean> shapeFileSetBeanSubsetList = (List<ShapeFileSetBean>) request.getSession().getAttribute("shapeFileSetBeanSubsetList");
			
			// Set the chosen feature on the ShapeFileSetBeans			
			for (String featureSelection : featureSelections) {
				String featureAppliesTo = featureSelection.substring(0, featureSelection.indexOf("::"));
				String feature = featureSelection.substring(featureSelection.indexOf("::") + 2);
				for (ShapeFileSetBean shapeFileSetBean : shapeFileSetBeanSubsetList) {
					if (shapeFileSetBean.getName().equals(featureAppliesTo)) {
						shapeFileSetBean.setChosenFeature(feature);
					}
				}
			}
			request.getSession().setAttribute("shapeFileSetBeanSubsetList", shapeFileSetBeanSubsetList);			
			forwardTo = "/jsp/THREDDSSelection.jsp";
		} else if ("step4".equals(action)) {
			List<ShapeFileSetBean> shapeFileSetBeanSubsetList = (List<ShapeFileSetBean>) request.getSession().getAttribute("shapeFileSetBeanSubsetList");
			String THREDDSUrl = request.getParameter("THREDDSUrl");
			List<InvAccess> openDapResources = new LinkedList<InvAccess>();
			if (THREDDSUrl == null || "".equals(THREDDSUrl)) {
				errorBean.getErrors().add("You must select a THREDDS URL to work with..");
				request.setAttribute("errorBean", errorBean);
				RequestDispatcher rd = request.getRequestDispatcher("/jsp/THREDDSSelection.jsp");
        		rd.forward(request, response);
        		return;  
			}
			
			// Grab the THREDDS catalog
			URI catalogURI = URI.create(THREDDSUrl);
			InvCatalogFactory factory = new InvCatalogFactory("default", true);
			InvCatalog catalog = factory.readXML(catalogURI);
			StringBuilder buff = new StringBuilder();
            if (!catalog.check(buff)) {
            	errorBean.getErrors().add(buff.toString());
            	request.setAttribute("errorBean", errorBean);
        		RequestDispatcher rd = request.getRequestDispatcher("/jsp/THREDDSSelection.jsp");
        		rd.forward(request, response);
        		return;
            }
            
            // Grab resources from the THREDDS catalog
        	openDapResources = NetCDFUtility.getOpenDapResources(catalog);
        	if (openDapResources == null) {
        		errorBean.getErrors().add("Could not pull information from THREDDS Server");
        		request.setAttribute("errorBean", errorBean);
        		RequestDispatcher rd = request.getRequestDispatcher("/jsp/THREDDSSelection.jsp");
        		rd.forward(request, response);
        		return;                	
        	}
        	
        	List<String> datasetList = new ArrayList<String>();
        	for (InvAccess opendapResource : openDapResources) {
        		datasetList.add(opendapResource.getStandardUrlName() + "::" + opendapResource.getDataset().getName());
            }
        	for (ShapeFileSetBean shapeFileSetBean : shapeFileSetBeanSubsetList) {
            	shapeFileSetBean.setTHREDDSCatalog(catalog);
            	shapeFileSetBean.setDatasetList(datasetList);
            }
        	request.getSession().setAttribute("shapeFileSetBeanSubsetList", shapeFileSetBeanSubsetList);			
			forwardTo = "/jsp/DataSetSelection.jsp";
		}

		request.setAttribute("errorBean", errorBean);
		RequestDispatcher rd = request.getRequestDispatcher(forwardTo);
		rd.forward(request, response);
	}


}
