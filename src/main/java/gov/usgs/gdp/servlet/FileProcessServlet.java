package gov.usgs.gdp.servlet;

import gov.usgs.gdp.analysis.NetCDFUtility;
import gov.usgs.gdp.bean.ErrorBean;
import gov.usgs.gdp.bean.ShapeFileSetBean;
import gov.usgs.gdp.bean.THREDDSInfoBean;
import gov.usgs.gdp.io.FileHelper;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.geotools.data.FeatureSource;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

import thredds.catalog.InvAccess;
import thredds.catalog.InvCatalog;
import thredds.catalog.InvCatalogFactory;
import ucar.ma2.Array;
import ucar.ma2.IndexIterator;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import ucar.ma2.Section;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.ProxyReader;
import ucar.nc2.Variable;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.dataset.CoordinateAxis1D;
import ucar.nc2.dataset.VariableDS;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.dt.grid.GeoGrid;
import ucar.nc2.dt.grid.GridCoordSys;
import ucar.nc2.dt.grid.GridDataset;
import ucar.nc2.ft.FeatureDatasetFactoryManager;
import ucar.nc2.util.CancelTask;
import ucar.nc2.util.NamedObject;
import ucar.unidata.geoloc.LatLonPoint;
import ucar.unidata.geoloc.LatLonPointImpl;
import ucar.unidata.geoloc.LatLonRect;
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
			THREDDSInfoBean threddsInfoBean = new THREDDSInfoBean();
			String THREDDSUrl = request.getParameter("THREDDSUrl");
			
			List<InvAccess> openDapResources = new LinkedList<InvAccess>();
			if (THREDDSUrl == null || "".equals(THREDDSUrl)) {
				errorBean.getErrors().add("You must select a THREDDS URL to work with..");
				request.setAttribute("errorBean", errorBean);
				RequestDispatcher rd = request.getRequestDispatcher("/jsp/THREDDSSelection.jsp");
        		rd.forward(request, response);
        		return;  
			}
			threddsInfoBean.setTHREDDSServer(THREDDSUrl);
			
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
        	
        	for (InvAccess opendapResource : openDapResources) {
        		threddsInfoBean.getOpenDapStandardURLNameList().add(opendapResource.getStandardUrlName());
        		threddsInfoBean.getOpenDapDataSetNameList().add(opendapResource.getDataset().getName());
            }
        	request.getSession().setAttribute("threddsInfoBean", threddsInfoBean);	
        	request.getSession().setAttribute("shapeFileSetBeanSubsetList", shapeFileSetBeanSubsetList);			
			forwardTo = "/jsp/DataSetSelection.jsp";
		}  else if ("step5".equals(action)) {
			THREDDSInfoBean threddsInfoBean = (THREDDSInfoBean) request.getSession().getAttribute("threddsInfoBean");			
			
			String dataSetSelection = request.getParameter("datasetSelection");
			if (dataSetSelection == null || "".equals(dataSetSelection)) {
				errorBean.getErrors().add("Did not get a DataSet selection. Please try again.");
        		request.setAttribute("errorBean", errorBean);
        		RequestDispatcher rd = request.getRequestDispatcher("/jsp/DataSetSelection.jsp");
        		rd.forward(request, response);
        		return;    
			}
			
			// Throw the settings into the THREDDSInfoBean
			String dataSetUrl = dataSetSelection.substring(0, dataSetSelection.indexOf(":::"));
			String dataSetName = dataSetSelection.substring(dataSetSelection.indexOf(":::") + 3);
			threddsInfoBean.setDataSetUrlSelection(dataSetUrl);
			threddsInfoBean.setDataSetNameSelection(dataSetName);
			
			// Grab the grid dataset
			Formatter errorLog = new Formatter();
			GridDataset gridDataSet = (GridDataset) FeatureDatasetFactoryManager.open(
		                FeatureType.GRID, dataSetUrl, null, errorLog);
			if (gridDataSet == null) {
				errorBean.getErrors().add("Could not open a grid at location: " + dataSetUrl);
				errorBean.getErrors().add("Reason: " + errorLog);
        		request.setAttribute("errorBean", errorBean);
        		RequestDispatcher rd = request.getRequestDispatcher("/jsp/DataSetSelection.jsp");
        		rd.forward(request, response);
        		return;    
			}
			
			// Grab the grid items
			List<String> gridSelectItemList = new ArrayList<String>();
			for (GridDatatype grid : gridDataSet.getGrids()) {
				gridSelectItemList.add(grid.getName());
			}
			gridDataSet.close();
			threddsInfoBean.setOpenDapGridItems(gridSelectItemList);
			
			request.getSession().setAttribute("threddsInfoBean", threddsInfoBean);	
			forwardTo = "/jsp/GridSelection.jsp";
		}   else if ("step6".equals(action)) {
			THREDDSInfoBean threddsInfoBean = (THREDDSInfoBean) request.getSession().getAttribute("threddsInfoBean");		
			String gridSelection = request.getParameter("gridSelection");
			if (gridSelection == null || "".equals(gridSelection)) {
				errorBean.getErrors().add("Did not get a Grid selection. Please try again.");
        		request.setAttribute("errorBean", errorBean);
        		RequestDispatcher rd = request.getRequestDispatcher("/jsp/GridSelection.jsp");
        		rd.forward(request, response);
        		return;    
			}
			
			threddsInfoBean.setGridItemSelection(gridSelection);
			Formatter errorLog = new Formatter();
			GridDataset gridDataSet = (GridDataset) FeatureDatasetFactoryManager.open(
	                FeatureType.GRID, threddsInfoBean.getDataSetUrlSelection(), null, errorLog);
			if (gridDataSet == null) {
				errorBean.getErrors().add("Could not open a grid at location: " + threddsInfoBean.getDataSetUrlSelection());
				errorBean.getErrors().add("Reason: " + errorLog);
        		request.setAttribute("errorBean", errorBean);
        		RequestDispatcher rd = request.getRequestDispatcher("/jsp/DataSetSelection.jsp");
        		rd.forward(request, response);
        		return;    
			}

            List<String> timeSelectItemList = new ArrayList<String>();
            GeoGrid grid = gridDataSet.findGridByName(gridSelection);

            for (NamedObject time : grid.getTimes()) {
                timeSelectItemList.add(time.getName());
            }
            gridDataSet.close();
            threddsInfoBean.setOpenDapGridTimes(timeSelectItemList);
            request.getSession().setAttribute("threddsInfoBean", threddsInfoBean);	
			forwardTo = "/jsp/TimePeriodSelection.jsp";
		} else if ("step7".equals(action)) {
			THREDDSInfoBean threddsInfoBean = (THREDDSInfoBean) request.getSession().getAttribute("threddsInfoBean");	
			List<ShapeFileSetBean> shapeFileSetBeanSubsetList = (List<ShapeFileSetBean>) request.getSession().getAttribute("shapeFileSetBeanSubsetList");
			for (ShapeFileSetBean shapeFileSetBean : shapeFileSetBeanSubsetList) {
				FileDataStore shapeFileDataStore = FileDataStoreFinder.getDataStore(shapeFileSetBean.getShapeFile());
				FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = shapeFileDataStore.getFeatureSource();
				
				String attributeType  = shapeFileSetBean.getChosenAttribute();
		        String attributeValue = shapeFileSetBean.getChosenFeature();
		        Filter filter = null;
		        try {
					filter = CQL.toFilter(attributeType + " = '" + attributeValue + "'");
				} catch (CQLException e) {
					// Do nothing right now -- this will be handled by another class
				}
				FeatureCollection<SimpleFeatureType, SimpleFeature> filteredFeatures = featureSource.getFeatures(filter);
				 SimpleFeature feature;
			        Iterator<SimpleFeature> featureIter = filteredFeatures.iterator();
			        try {
			            feature = featureIter.next();   // Return only the first feature, even if there are multiple matches.
			        } finally {
			            filteredFeatures.close(featureIter);
			        }

		        Geometry geom = (Geometry) feature.getDefaultGeometry();
		        String datasetUrl = threddsInfoBean.getDataSetUrlSelection();
		        Formatter errorLog = new Formatter();
		        GridDataset gridDataset = (GridDataset) FeatureDatasetFactoryManager.open(
		                FeatureType.GRID, datasetUrl, null, errorLog);
		        if (gridDataset == null) {
		            throw new IOException("Cannot open GRID at location= " + datasetUrl + "; error message = " + errorLog);
		        }
		        
		        try {
		            List<GridDatatype> grids = gridDataset.getGrids();
		            GeoGrid grid = (GeoGrid) grids.iterator().next();
		            Range timeRange = null;
		            try {
						timeRange = new Range(Integer.parseInt(threddsInfoBean.getFromTime()),
								Integer.parseInt( threddsInfoBean.getToTime()));
					} catch (NumberFormatException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InvalidRangeException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					Envelope envelope = geom.getEnvelopeInternal();

			        LatLonPoint lowerLeftPoint  = new LatLonPointImpl(envelope.getMinY(), envelope.getMinX());
			        LatLonPoint upperRightPoint = new LatLonPointImpl(envelope.getMaxY(), envelope.getMaxX());
			        LatLonRect boundingBox = new LatLonRect(lowerLeftPoint, upperRightPoint);

			        GeoGrid slicedGrid = null;
					try {
						slicedGrid = grid.subset(timeRange, null, boundingBox, 1, 1, 1);
					} catch (InvalidRangeException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		            VariableDS gridVar = slicedGrid.getVariable();
		            VariableDS proxiedGridVar = new VariableDS(null, gridVar, true);

		            proxiedGridVar.addAttribute(new Attribute("_FillValue", -999f));
		            proxiedGridVar.addAttribute(new Attribute("missing_value", -999f));
		            proxiedGridVar.setProxyReader(new ShapedGridReader(slicedGrid, geom));

		            GeoGrid outputGrid =
		                    new GeoGrid(gridDataset, proxiedGridVar, (GridCoordSys) slicedGrid.getCoordinateSystem());
		            
		            // What is directory name for the files being uploaded
		    		String seperator = FileHelper.getSeparator();
		    	    String userDirectory = (String) request.getSession().getAttribute("userTempDir")+ seperator;
		            File outputFile = new File(userDirectory, attributeValue + ".nc");
		            
		            outputFile.delete();
		            outputGrid.writeFile(outputFile.toString());
		        } finally {
		            gridDataset.close();
		        }
				
			}
			
		}

		request.setAttribute("errorBean", errorBean);
		RequestDispatcher rd = request.getRequestDispatcher(forwardTo);
		rd.forward(request, response);
	}

	private final static class ShapedGridReader implements ProxyReader {
	    private GeoGrid  grid;
	    private Geometry shape;
	    private GeometryFactory geometryFactory;

	    private CoordinateAxis1D xAxis;
	    private CoordinateAxis1D yAxis;

	    private double[][] percentOfShapeOverlappingCells;

	    public ShapedGridReader(GeoGrid grid, Geometry shape) {
	        this.grid = grid;
	        this.shape = shape;
	        this.geometryFactory = JTSFactoryFinder.getGeometryFactory(null);

	        GridCoordSystem gcs = grid.getCoordinateSystem();
	        this.xAxis = (CoordinateAxis1D) gcs.getXHorizAxis();
	        this.yAxis = (CoordinateAxis1D) gcs.getYHorizAxis();

	        this.percentOfShapeOverlappingCells = new double[(int) xAxis.getSize()][(int) yAxis.getSize()];
	        for (int x = 0; x < xAxis.getSize(); ++x) {
	            for (int y = 0; y < yAxis.getSize(); ++y) {
	                percentOfShapeOverlappingCells[x][y] = Double.NaN;
	            }
	        }
	    }

	    @Override public Array read(Variable mainv, CancelTask cancelTask) throws IOException {
	        if (!(mainv instanceof VariableDS) || ((VariableDS) mainv).getOriginalVariable() != grid.getVariable()) {
	            throw new RuntimeException("mainv is not a proxy for the grid's underlying Variable.");
	        }

	        Array data = grid.getVariable().read();

	        Dimension xDim = grid.getXDimension();
	        Dimension yDim = grid.getYDimension();
	        List<Dimension> varDims = grid.getDimensions();

	        int xDimIndex = varDims.indexOf(xDim);  // The index of the X dimension in grid's list of dimensions.
	        int yDimIndex = varDims.indexOf(yDim);  // The index of the Y dimension in grid's list of dimensions.

	        for (IndexIterator indexIter = data.getIndexIterator(); indexIter.hasNext(); ) {
	            indexIter.next();
	            int[] iterPos = indexIter.getCurrentCounter();  // The position of indexIter in data.
	            int xDimPos = iterPos[xDimIndex];               // The X component of iterPos.
	            int yDimPos = iterPos[yDimIndex];               // The Y component of iterPos.

	            double percentOfShapeOverlappingCell = getPercentOfShapeOverlappingCell(xDimPos, yDimPos);
	            if (percentOfShapeOverlappingCell == 0) {
	                indexIter.setDoubleCurrent(-999.0);
	            }
	        }

	        return data;
	    }

	    @Override public Array read(Variable mainv, Section section, CancelTask cancelTask)
	            throws IOException, InvalidRangeException {
	        if (!(mainv instanceof VariableDS) || ((VariableDS) mainv).getOriginalVariable() != grid.getVariable()) {
	            throw new RuntimeException("mainv is not a proxy for the grid's underlying Variable.");
	        }

	        Array data = grid.getVariable().read(section);

	        Dimension xDim = grid.getXDimension();
	        Dimension yDim = grid.getYDimension();
	        List<Dimension> varDims = grid.getDimensions();

	        int xDimIndex = varDims.indexOf(xDim);  // The index of the X dimension in grid's list of dimensions.
	        int yDimIndex = varDims.indexOf(yDim);  // The index of the Y dimension in grid's list of dimensions.

	        int[] origin = section.getOrigin();
	        int xDimOffset = origin[xDimIndex];
	        int yDimOffset = origin[yDimIndex];

	        for (IndexIterator indexIter = data.getIndexIterator(); indexIter.hasNext(); ) {
	            indexIter.next();
	            int[] iterPos = indexIter.getCurrentCounter();  // The position of indexIter in data.
	            int xDimPos = xDimOffset + iterPos[xDimIndex];  // The X component of iterPos.
	            int yDimPos = yDimOffset + iterPos[yDimIndex];  // The Y component of iterPos.

	            double percentOfShapeOverlappingCell = getPercentOfShapeOverlappingCell(xDimPos, yDimPos);
	            if (percentOfShapeOverlappingCell == 0) {
	                indexIter.setFloatCurrent(-999f);
	            }
	        }

	        return data;
	    }

	    private double getPercentOfShapeOverlappingCell(int xIndex, int yIndex) {
	        if (!Double.isNaN(percentOfShapeOverlappingCells[xIndex][yIndex])) {
	            return percentOfShapeOverlappingCells[xIndex][yIndex];
	        }

	        double[] xCellEdges = xAxis.getCoordEdges(xIndex);
	        double[] yCellEdges = yAxis.getCoordEdges(yIndex);

	        Envelope envelope = new Envelope(xCellEdges[0], xCellEdges[1], yCellEdges[0], yCellEdges[1]);
	        Geometry cellRectangle = geometryFactory.toGeometry(envelope);

	        Geometry intersection = cellRectangle.intersection(shape);
	        percentOfShapeOverlappingCells[xIndex][yIndex] = intersection.getArea() / shape.getArea();
	        return percentOfShapeOverlappingCells[xIndex][yIndex];
	    }
	}

	
}

