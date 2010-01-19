package gov.usgs.gdp.servlet;

import gov.usgs.gdp.analysis.NetCDFUtility;
import gov.usgs.gdp.bean.MessageBean;
import gov.usgs.gdp.bean.ShapeFileSetBean;
import gov.usgs.gdp.bean.THREDDSInfoBean;
import gov.usgs.gdp.helper.FileHelper;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
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
import gov.usgs.gdp.analysis.SimpleStatistics;
import java.io.BufferedWriter;
import java.io.FileWriter;

import thredds.catalog.InvAccess;
import thredds.catalog.InvCatalog;
import thredds.catalog.InvCatalogFactory;
import ucar.ma2.Array;
import ucar.ma2.IndexIterator;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import ucar.ma2.Section;
import ucar.ma2.StructureData;
import ucar.ma2.StructureMembers;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.ProxyReader;
import ucar.nc2.Variable;
import ucar.nc2.VariableSimpleIF;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.dataset.CoordinateAxis1D;
import ucar.nc2.dataset.CoordinateAxis1DTime;
import ucar.nc2.dataset.VariableDS;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.dt.grid.GeoGrid;
import ucar.nc2.dt.grid.GridCoordSys;
import ucar.nc2.dt.grid.GridDataset;
import ucar.nc2.ft.FeatureDataset;
import ucar.nc2.ft.FeatureDatasetFactoryManager;
import ucar.nc2.ft.PointFeature;
import ucar.nc2.ft.PointFeatureCollection;
import ucar.nc2.ft.PointFeatureIterator;
import ucar.nc2.util.CancelTask;
import ucar.nc2.util.NamedObject;
import ucar.unidata.geoloc.LatLonPoint;
import ucar.unidata.geoloc.LatLonPointImpl;
import ucar.unidata.geoloc.LatLonRect;

/**
 * Servlet implementation class FileProcessServlet
 */
public class FileProcessServlet extends HttpServlet {
	private static org.apache.log4j.Logger log = Logger.getLogger(FileProcessServlet.class);

    private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public FileProcessServlet() {
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
    @SuppressWarnings("unchecked")
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = (request.getParameter("action") == null) ? "" : request.getParameter("action").toLowerCase();
        List<ShapeFileSetBean> shapeFileSetBeanList = (List<ShapeFileSetBean>) request.getSession().getAttribute("shapeFileSetBeanList");
        List<ShapeFileSetBean> shapeFileSetBeanSubsetList = (List<ShapeFileSetBean>) request.getSession().getAttribute("shapeFileSetBeanSubsetList");


        MessageBean errorBean = new MessageBean();
        MessageBean messageBean = new MessageBean();
        String forwardTo = "";

        if (action == null || "".equals(action)) {
            errorBean.addMessage("Your action was not read in properly. Please try again");
            request.setAttribute("messageBean", messageBean);
            request.setAttribute("errorBean", errorBean);
            RequestDispatcher rd = request.getRequestDispatcher("/jsp/fileSelection.jsp");
            rd.forward(request, response);
            return;
        }

        if ("step1".equals(action)) {
            String[] checkboxItems = request.getParameterValues("fileName");

            if (shapeFileSetBeanList == null) {
                errorBean.addMessage("Unable to retrieve shape file set lists. Please choose new shape file(s).");
                request.setAttribute("messageBean", messageBean);
                request.setAttribute("errorBean", errorBean);
                RequestDispatcher rd = request.getRequestDispatcher("/jsp/fileSelection.jsp");
                rd.forward(request, response);
                return;
            }

            if (checkboxItems == null || checkboxItems.length == 0) {
                errorBean.addMessage("You must select at least one file to process.");
                request.setAttribute("messageBean", messageBean);
                request.setAttribute("errorBean", errorBean);
                RequestDispatcher rd = request.getRequestDispatcher("/jsp/fileSelection.jsp");
                rd.forward(request, response);
                return;
            }

            // Get the subset of ShapeFile sets the user wants to work on
            List<ShapeFileSetBean> shpFilesSetSubList = getShapeFilesSetSubList(checkboxItems, shapeFileSetBeanList);

            // Populate the attribute values of each ShapeFileSet
            for (ShapeFileSetBean shapeFileSetBean : shpFilesSetSubList) {
                shapeFileSetBean.setAttributeList(ShapeFileSetBean.getAttributeListFromBean(shapeFileSetBean));
            }

            request.getSession().setAttribute("shapeFileSetBeanSubsetList", shpFilesSetSubList);
            forwardTo = "/jsp/attributeSelection.jsp";

        } else if ("step2".equals(action)) { // Attributes chosen, set up feature list
            String[] attributeSelections = request.getParameterValues("attributeSelection");

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
            
            // Pull the THREDDS urls from the properties files
            Map<String, String> threddsMap = THREDDSInfoBean.getTHREDDSUrlMap();
            request.setAttribute("threddsMap", threddsMap);
            
            request.getSession().setAttribute("shapeFileSetBeanSubsetList", shapeFileSetBeanSubsetList);
            forwardTo = "/jsp/THREDDSSelection.jsp";
        } else if ("step4".equals(action)) {
            THREDDSInfoBean threddsInfoBean = new THREDDSInfoBean();
            String THREDDSUrl = request.getParameter("THREDDSUrl");

            List<InvAccess> openDapResources = new LinkedList<InvAccess>();
            if (THREDDSUrl == null || "".equals(THREDDSUrl)) {
                errorBean.getMessages().add("You must select a THREDDS URL to work with..");
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
                errorBean.getMessages().add(buff.toString());
                request.setAttribute("errorBean", errorBean);
                RequestDispatcher rd = request.getRequestDispatcher("/jsp/THREDDSSelection.jsp");
                rd.forward(request, response);
                return;
            }

            // Grab resources from the THREDDS catalog
            openDapResources = NetCDFUtility.getOpenDapResources(catalog);
            if (openDapResources == null) {
                errorBean.getMessages().add("Could not pull information from THREDDS Server");
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
        } else if ("step5".equals(action)) {
            THREDDSInfoBean threddsInfoBean = (THREDDSInfoBean) request.getSession().getAttribute("threddsInfoBean");

            String dataSetSelection = request.getParameter("datasetSelection");
            if (dataSetSelection == null || "".equals(dataSetSelection)) {
                errorBean.getMessages().add("Did not get a DataSet selection. Please try again.");
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


            FeatureDataset featureDataset = FeatureDatasetFactoryManager.open(
                    null, dataSetUrl, null, errorLog);

            if (featureDataset != null) {

                List<String> dataSelectItemList = new ArrayList<String>();
//                if(featureDataset instanceof GridDataset) {
//                    // Grab the grid items
//                    for (GridDatatype grid : ((GridDataset)featureDataset).getGrids()) {
//                        dataSelectItemList.add(grid.getName());
//                    }
//                    ((GridDataset)featureDataset).close();
//                } else
//                if(featureDataset instanceof FeatureDatasetPoint) {
//                    for (ucar.nc2.ft.FeatureCollection fc : ((FeatureDatasetPoint)featureDataset).getPointFeatureCollectionList()) {
//                        System.out.println(fc.getName());
//                    }
//                }
                for (VariableSimpleIF vs : featureDataset.getDataVariables()) {
                    dataSelectItemList.add(vs.getName());
                }
                threddsInfoBean.setOpenDapGridItems(dataSelectItemList);

            } else {

                errorBean.getMessages().add("Could not open a grid at location: " + dataSetUrl);
                errorBean.getMessages().add("Reason: " + errorLog);
                request.setAttribute("errorBean", errorBean);
                RequestDispatcher rd = request.getRequestDispatcher("/jsp/DataSetSelection.jsp");
                rd.forward(request, response);
                return;
            }

            request.getSession().setAttribute("threddsInfoBean", threddsInfoBean);
            forwardTo = "/jsp/GridSelection.jsp";
        } else if ("step6".equals(action)) { // Set up time selection
            THREDDSInfoBean threddsInfoBean = (THREDDSInfoBean) request.getSession().getAttribute("threddsInfoBean");
            String gridSelection = request.getParameter("gridSelection");
            if (gridSelection == null || "".equals(gridSelection)) {
                errorBean.getMessages().add("Did not get a Grid selection. Please try again.");
                request.setAttribute("errorBean", errorBean);
                RequestDispatcher rd = request.getRequestDispatcher("/jsp/GridSelection.jsp");
                rd.forward(request, response);
                return;
            }

            threddsInfoBean.setGridItemSelection(gridSelection);
            Formatter errorLog = new Formatter();
            FeatureDataset featureDataset = FeatureDatasetFactoryManager.open(
                    null, threddsInfoBean.getDataSetUrlSelection(), null, errorLog);

            if (featureDataset != null) {

                List<String> timeSelectItemList = new ArrayList<String>();
                if (featureDataset instanceof GridDataset) {
                    GeoGrid grid = ((GridDataset)featureDataset).findGridByName(gridSelection);

                    for (NamedObject time : grid.getTimes()) {
                        timeSelectItemList.add(time.getName());
                    }
                } else {
                    // TODO:
                }

                featureDataset.close();
                threddsInfoBean.setOpenDapGridTimes(timeSelectItemList);
                              
                request.getSession().setAttribute("threddsInfoBean", threddsInfoBean);
                forwardTo = "/jsp/TimePeriodSelection.jsp";

            } else {
                errorBean.getMessages().add("Could not open a grid at location: " + threddsInfoBean.getDataSetUrlSelection());
                errorBean.getMessages().add("Reason: " + errorLog);
                request.setAttribute("errorBean", errorBean);
                RequestDispatcher rd = request.getRequestDispatcher("/jsp/DataSetSelection.jsp");
                rd.forward(request, response);
                return;
            }
        } else if ("step7".equals(action)) {
            THREDDSInfoBean threddsInfoBean = (THREDDSInfoBean) request.getSession().getAttribute("threddsInfoBean");
            String fromTime = request.getParameter("timeFromSelection");
            String toTime = request.getParameter("timeToSelection");
            
            DateFormat df = new SimpleDateFormat("MM-dd-yyyy");
			Date toDate = new Date();
			Date fromDate = new Date();
            boolean parsedDates = false;
            try {
				toDate = df.parse(toTime);
				fromDate = df.parse(fromTime);
				parsedDates = true;
			} catch (ParseException e1) {
				parsedDates = false;
				log.debug(e1.getMessage());
			}
			
			if (!parsedDates) {
				errorBean.getMessages().add("Could not parse dates.");
                request.setAttribute("errorBean", errorBean);
                RequestDispatcher rd = request.getRequestDispatcher("/jsp/TimePeriodSelection.jsp");
                rd.forward(request, response);
                return;
			}
			
            threddsInfoBean.setFromTime(fromTime);
            threddsInfoBean.setToTime(toTime);
            for (ShapeFileSetBean shapeFileSetBean : shapeFileSetBeanSubsetList) {
                FileDataStore shapeFileDataStore = FileDataStoreFinder.getDataStore(shapeFileSetBean.getShapeFile());
                FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = shapeFileDataStore.getFeatureSource();

                String attributeType = shapeFileSetBean.getChosenAttribute();
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
                        CoordinateAxis1DTime timeAxis = grid.getCoordinateSystem().getTimeAxis1D();
                        int timeIndexMin = timeAxis.findTimeIndexFromDate(fromDate);
                        int timeIndexMax = timeAxis.findTimeIndexFromDate(toDate);
                        timeRange = new Range(timeIndexMin, timeIndexMax);
                    } catch (NumberFormatException e) {
                       log.debug(e.getMessage());
                    } catch (InvalidRangeException e) {
                    	log.debug(e.getMessage());
                    }

                    Envelope envelope = geom.getEnvelopeInternal();

                    LatLonPoint lowerLeftPoint = new LatLonPointImpl(envelope.getMinY(), envelope.getMinX());
                    LatLonPoint upperRightPoint = new LatLonPointImpl(envelope.getMaxY(), envelope.getMaxX());
                    LatLonRect boundingBox = new LatLonRect(lowerLeftPoint, upperRightPoint);

                    GeoGrid slicedGrid = null;
                    try {
                        slicedGrid = grid.subset(timeRange, null, boundingBox, 1, 1, 1);
                    } catch (InvalidRangeException e) {
                    	errorBean.getMessages().add("Unable to slice grid");
                    	errorBean.getMessages().add("Error output:\n" + e.getMessage());
                        RequestDispatcher rd = request.getRequestDispatcher("/jsp/DataSetSelection.jsp");
                        rd.forward(request, response);
                        return;
                    }

                    List<String> simpleStats = SimpleStatistics.getStatisticsList(feature, gridDataset, slicedGrid.getVariable().getName(), timeRange);
                    threddsInfoBean.setStatsSummary(simpleStats);
                    
                    // Create a null check here
                    VariableDS gridVar = slicedGrid.getVariable();
                    VariableDS proxiedGridVar = new VariableDS(null, gridVar, true);

                    proxiedGridVar.addAttribute(new Attribute("_FillValue", Float.valueOf(-999f)));
                    proxiedGridVar.addAttribute(new Attribute("missing_value", Float.valueOf(-999f)));
                    proxiedGridVar.setProxyReader(new ShapedGridReader(slicedGrid, geom));

                    GeoGrid outputGrid = new GeoGrid(gridDataset, proxiedGridVar, (GridCoordSys) slicedGrid.getCoordinateSystem());
                    threddsInfoBean.setGeoGrid(outputGrid);
                    request.getSession().setAttribute("threddsInfoBean", threddsInfoBean);
                    forwardTo = "/jsp/showSummary.jsp";
                    
                } finally {
                    gridDataset.close();
                }
                forwardTo = "/jsp/showSummary.jsp";

            }

        } else if ("step8".equals(action))  {
        	
        	THREDDSInfoBean threddsInfoBean = (THREDDSInfoBean) request.getSession().getAttribute("threddsInfoBean");
            // What is directory name for the files being uploaded
            String seperator = FileHelper.getSeparator();
            String userDirectory = (String) request.getSession().getAttribute("userTempDir") + seperator;
            GeoGrid outputGrid = threddsInfoBean.getGeoGrid();
            ShapeFileSetBean shapeFileSetBean = shapeFileSetBeanSubsetList.get(0);
            String attributeValue = shapeFileSetBean.getChosenFeature();
            File outputFile = new File(userDirectory, attributeValue + ".nc");

            outputFile.delete();
            outputGrid.writeFile(outputFile.toString());
            request.setAttribute("fileLink", outputFile.getPath());
        }
        
        request.setAttribute("messageBean", messageBean);
        request.setAttribute("errorBean", errorBean);
        RequestDispatcher rd = request.getRequestDispatcher(forwardTo);
        rd.forward(request, response);
    }

    /**
     * Writes a collection of {@link PointFeature}s to a file in CSV format. PointFeatures are written one per line
     * in the order that they appear in {@code points}'s iterator.
     *
     * @param points    a collection of point features. Before being passed to this method, a PointFeatureCollection
     *                  can be subset in space and/or time with {@link PointFeatureCollection#subset}.
     * @param outFile   the file to write output to.
     * @throws IOException  if an I/O error occurs.
     */
    public static void writePointsToFile(PointFeatureCollection points, File outFile)
            throws IOException {
        boolean columnNamesWritten = false;
        BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));
        String fieldSep = ", ";
        String eol = "\n";

        try {
            for (PointFeatureIterator iter = points.getPointFeatureIterator(-1); iter.hasNext();) {
                StringBuilder strBuilder = new StringBuilder();

                PointFeature pointFeature = iter.next();
                StructureData data = pointFeature.getData();

                if (!columnNamesWritten) {
                    for (StructureMembers.Member member : data.getMembers()) {
                        String memberName = member.getName().trim();
                        strBuilder.append(memberName).append(fieldSep);
                    }

                    // Replace trailing fieldSep with eol.
                    strBuilder.replace(strBuilder.length() - fieldSep.length(), strBuilder.length(), eol);
                    columnNamesWritten = true;
                }

                for (StructureMembers.Member member : data.getMembers()) {
                    String memberValue = data.getArray(member).toString().trim();
                    strBuilder.append(memberValue).append(fieldSep);
                }

                // Replace trailing fieldSep with eol.
                strBuilder.replace(strBuilder.length() - fieldSep.length(), strBuilder.length(), eol);
                writer.write(strBuilder.toString());
            }
        } finally {
            writer.close();
        }
    }

    private List<ShapeFileSetBean> getShapeFilesSetSubList(String[] checkboxItems, List<ShapeFileSetBean> shapeFileSetBeanList) {
        List<ShapeFileSetBean> result = new ArrayList<ShapeFileSetBean>();

        for (String item : checkboxItems) {
            for (ShapeFileSetBean shapeFileSetBean : shapeFileSetBeanList) {
                if (shapeFileSetBean.getName().equals(item)) {
                    result.add(shapeFileSetBean);
                }
            }
        }

        return result;
    }

    private final static class ShapedGridReader implements ProxyReader {

        private GeoGrid grid;
        private Geometry shape;
        private GeometryFactory geometryFactory;
        private CoordinateAxis1D xAxis;
        private CoordinateAxis1D yAxis;
        private double[][] percentOfShapeOverlappingCells;

        public ShapedGridReader(GeoGrid gridParam, Geometry shapeParam) {
            this.grid = gridParam;
            this.shape = shapeParam;
            this.geometryFactory = JTSFactoryFinder.getGeometryFactory(null);

            GridCoordSystem gcs = this.grid.getCoordinateSystem();
            this.xAxis = (CoordinateAxis1D) gcs.getXHorizAxis();
            this.yAxis = (CoordinateAxis1D) gcs.getYHorizAxis();

            this.percentOfShapeOverlappingCells = new double[(int) this.xAxis.getSize()][(int) this.yAxis.getSize()];
            for (int x = 0; x < this.xAxis.getSize(); ++x) {
                for (int y = 0; y < this.yAxis.getSize(); ++y) {
                    this.percentOfShapeOverlappingCells[x][y] = Double.NaN;
                }
            }
        }

        @Override
        public Array read(Variable mainv, CancelTask cancelTask) throws IOException {
            if (!(mainv instanceof VariableDS) || ((VariableDS) mainv).getOriginalVariable() != this.grid.getVariable()) {
                throw new RuntimeException("mainv is not a proxy for the grid's underlying Variable.");
            }

            Array data = this.grid.getVariable().read();

            Dimension xDim = this.grid.getXDimension();
            Dimension yDim = this.grid.getYDimension();
            List<Dimension> varDims = this.grid.getDimensions();

            int xDimIndex = varDims.indexOf(xDim);  // The index of the X dimension in grid's list of dimensions.
            int yDimIndex = varDims.indexOf(yDim);  // The index of the Y dimension in grid's list of dimensions.

            for (IndexIterator indexIter = data.getIndexIterator(); indexIter.hasNext();) {
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

        @Override
        public Array read(Variable mainv, Section section, CancelTask cancelTask)
                throws IOException, InvalidRangeException {
            if (!(mainv instanceof VariableDS) || ((VariableDS) mainv).getOriginalVariable() != this.grid.getVariable()) {
                throw new RuntimeException("mainv is not a proxy for the grid's underlying Variable.");
            }

            Array data = this.grid.getVariable().read(section);

            Dimension xDim =this.grid.getXDimension();
            Dimension yDim = this.grid.getYDimension();
            List<Dimension> varDims = this.grid.getDimensions();

            int xDimIndex = varDims.indexOf(xDim);  // The index of the X dimension in grid's list of dimensions.
            int yDimIndex = varDims.indexOf(yDim);  // The index of the Y dimension in grid's list of dimensions.

            int[] origin = section.getOrigin();
            int xDimOffset = origin[xDimIndex];
            int yDimOffset = origin[yDimIndex];

            for (IndexIterator indexIter = data.getIndexIterator(); indexIter.hasNext();) {
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
            if (!Double.isNaN(this.percentOfShapeOverlappingCells[xIndex][yIndex])) {
                return this.percentOfShapeOverlappingCells[xIndex][yIndex];
            }

            double[] xCellEdges = this.xAxis.getCoordEdges(xIndex);
            double[] yCellEdges = this.yAxis.getCoordEdges(yIndex);

            Envelope envelope = new Envelope(xCellEdges[0], xCellEdges[1], yCellEdges[0], yCellEdges[1]);
            Geometry cellRectangle = this.geometryFactory.toGeometry(envelope);

            Geometry intersection = cellRectangle.intersection(this.shape);
            this.percentOfShapeOverlappingCells[xIndex][yIndex] = intersection.getArea() / this.shape.getArea();
            return this.percentOfShapeOverlappingCells[xIndex][yIndex];
        }
    }
}

