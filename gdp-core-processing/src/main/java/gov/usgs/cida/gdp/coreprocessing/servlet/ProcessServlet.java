package gov.usgs.cida.gdp.coreprocessing.servlet;


import gov.usgs.cida.gdp.coreprocessing.analysis.grid.FeatureCategoricalGridCoverage;
import gov.usgs.cida.gdp.coreprocessing.bean.FileLocationBean;
import gov.usgs.cida.gdp.utilities.FileHelper;
import gov.usgs.cida.gdp.utilities.XmlUtils;
import gov.usgs.cida.gdp.utilities.bean.AckBean;
import gov.usgs.cida.gdp.utilities.bean.AvailableFilesBean;
import gov.usgs.cida.gdp.utilities.bean.ErrorBean;
import gov.usgs.cida.gdp.utilities.bean.ShapeFileSetBean;
import gov.usgs.cida.gdp.utilities.bean.XmlReplyBean;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Formatter;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureSource;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.shapefile.shp.ShapeType;
import org.geotools.data.shapefile.shp.ShapefileWriter;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.CQLException;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.operation.TransformException;
import org.xml.sax.SAXException;

import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import ucar.nc2.VariableSimpleIF;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.dataset.CoordinateAxis1DTime;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.dt.grid.GridDataset;
import ucar.nc2.ft.FeatureDataset;
import ucar.nc2.ft.FeatureDatasetFactoryManager;
import ucar.nc2.ft.FeatureDatasetPoint;
import ucar.nc2.ft.StationTimeSeriesFeatureCollection;
import ucar.nc2.units.DateRange;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import gov.usgs.cida.gdp.communication.EmailHandler;
import gov.usgs.cida.gdp.communication.bean.EmailMessageBean;
import gov.usgs.cida.gdp.coreprocessing.analysis.StationDataCSVWriter;
import gov.usgs.cida.gdp.coreprocessing.analysis.grid.FeatureCoverageWeightedGridStatistics;
import gov.usgs.cida.gdp.coreprocessing.analysis.grid.FeatureCoverageWeightedGridStatisticsWriter.Statistic;
import gov.usgs.cida.gdp.coreprocessing.servlet.ProcessServlet.GroupBy.StationOption;
import ucar.nc2.NetcdfFile;



/**
 * Servlet implementation class ProcessServlet
 */
public class ProcessServlet extends HttpServlet {
	private static org.apache.log4j.Logger log = Logger.getLogger(ProcessServlet.class);

    static {
        try {
            NetcdfFile.registerIOProvider(ucar.nc2.iosp.geotiff.GeoTiffIOServiceProvider.class);
        } catch (Exception e) { }
    }

    /**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Long start = Long.valueOf(new Date().getTime());
		
		String command = request.getParameter("command");

		
		if ("submitforprocessing".equals(command)) {
			
			// Check to see if their user directory is still around.
			if (!FileHelper.doesDirectoryOrFileExist(System.getProperty("applicationUserSpaceDir") + 
					request.getParameter("userdirectory"))) {
			    FileHelper.createDir(System.getProperty("applicationUserSpaceDir") + 
					request.getParameter("userdirectory"));
			}
				
			FileHelper.createUserDirectory(System.getProperty("applicationUserSpaceDir"));
			
			FileLocationBean fileLocations = null;
			try {
				fileLocations = populateFileUpload(request);
			} catch (InvalidRangeException e) {
				XmlReplyBean xmlOutput = new XmlReplyBean(AckBean.ACK_FAIL, new ErrorBean(ErrorBean.ERR_BOX_NO_INTERSECT_GRID));
				XmlUtils.sendXml(xmlOutput, start, response);
				return;
			} catch (AddressException e) {
				XmlReplyBean xmlOutput = new XmlReplyBean(AckBean.ACK_FAIL, new ErrorBean(ErrorBean.ERR_EMAIL_ERROR_INCORRECT_ADDRESS));
				XmlUtils.sendXml(xmlOutput, start, response);
				return;
			} catch (MessagingException e) {
				XmlReplyBean xmlOutput = new XmlReplyBean(AckBean.ACK_FAIL, new ErrorBean(ErrorBean.ERR_EMAIL_ERROR));
				XmlUtils.sendXml(xmlOutput, start, response);
				return;
			} catch (Exception e) {
				XmlReplyBean xmlOutput = new XmlReplyBean(AckBean.ACK_FAIL, new ErrorBean(e.getMessage()));
				XmlUtils.sendXml(xmlOutput, start, response);
                e.printStackTrace();
				return;
            }

			// We are, for the moment, assuming there is a file at this location
			// The link is sent out as just the file name. When the user sends the request
			// back, we go to the directory at String baseFilePath = System.getProperty("applicationTempDir");
			// + the file specified by the user ((fileForUpload.getName()) and we send that
			// back to the user
			XmlReplyBean xmlReply = new XmlReplyBean(AckBean.ACK_OK, fileLocations);
			XmlUtils.sendXml(xmlReply, start, response);
			return;
		}
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	    doGet(request, response);
	}

	private FileLocationBean populateFileUpload(HttpServletRequest request)
            throws IOException, InvalidRangeException, AddressException,
            MessagingException, FactoryException, TransformException,
            org.opengis.coverage.grid.InvalidRangeException, SchemaException {
        String email = request.getParameter("email");
        String finalUrlEmail = request.getParameter("finalurlemail");

        // Create a File Which represents the output we are looking for.
        FileLocationBean uploadFiles = populateSummary(request);

        // If user specified an E-Mail address, send an E-Mail to the user with the provided link
        if (email != null && !"".equals(email)) sendEmail(email, finalUrlEmail);

        // Set that file as the result to be returned to the calling function
        // switch uploadDirectory return statement with the file we are looking for
        return uploadFiles;
    }

	////  START - MOVEME
	// IVAN, move this out where ever you see fit... values in this enum should
	// be reported-to/used-by front end in some manner...  right now, too much loose
	// coupling.
	public enum DelimiterOption {
	    c("[comma]", ","),
        t("[tab]", "\t"),
        s("[space]", " ");
	    public final String description;
	    public final String delimiter;
	    private DelimiterOption(String description, String value) {
	        this.description = description;
	        this.delimiter = value;
	    }
	    public static DelimiterOption getDefault() { return c; }
	    @Override public String toString() { return description; }
	}

	public static class GroupBy {
	    public enum StationOption {
	        station("Station"),
	        variable("Variable");
	        public final String description;
	        private StationOption(String description) {
	            this.description = description;
	        }
	        @Override public String toString() { return description; }
	        public static StationOption getDefault() { return station; }
	    }
	    public enum GridOption {
	        attributes("Attributes"),
	        statistics("Statistics");
	        public final String description;
	        private GridOption(String description) {
                this.description = description;
            }
            public static GridOption getDefault() { return attributes; }
	    }
	}
    ////  END - MOVEME

	private FileLocationBean populateSummary(HttpServletRequest request) throws IOException, InvalidRangeException, FactoryException, TransformException, org.opengis.coverage.grid.InvalidRangeException, SchemaException {

		// Geometry
	    String shapeSet = request.getParameter("shapeset");
	    String attribute = request.getParameter("attribute");
	    String[] features = request.getParameterValues("feature");
	    String[] outputStats = request.getParameterValues("outputstat");
	    
	    String lat = request.getParameter("lat");
	    String lon = request.getParameter("lon");
	    
	    // THREDDS
	    String dataset = request.getParameter("dataset");
	    String[] dataTypes = request.getParameterValues("datatype");
	    String from = request.getParameter("from");
	    String to = request.getParameter("to");
	    
	    // WCS
	    String wcsServer = request.getParameter("wcsserver");
	    String wcsCoverage = request.getParameter("wcscoverage");
	    String wcsGridCRS = request.getParameter("wcsgridcrs");
	    String wcsGridOffsets = request.getParameter("wcsgridoffsets");
	    String wcsBoundingBox = request.getParameter("wcsboundingbox");
	    String wcsResampleFactor = request.getParameter("wcsresamplefactor");
	    String wcsResmapleFilter = request.getParameter("wcsresamplefilter");
	    
	    // Either "thredds" or "wcs"
	    String dataSetInterface = request.getParameter("datasetinterface");
	    
	    // Output
	    String output = request.getParameter("outputtype");
	    String outputFile = request.getParameter("outputfile");
	    String userDirectory = request.getParameter("userdirectory");
	    String groupById	= request.getParameter("groupby");
	    String delimId	= request.getParameter("delim");

        boolean categorical = false;
	    
	    FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection = null;

	    String baseFilePath = System.getProperty("applicationTempDir");
        String userspacePath = System.getProperty("applicationUserSpaceDir");
    	baseFilePath = baseFilePath + FileHelper.getSeparator();
    	File uploadDirectory = FileHelper.createFileRepositoryDirectory(baseFilePath);
    	if (!uploadDirectory.exists()) return null;
    	

    	String attributeName = attribute;
    	
    	String shapefilePath = null;

	    if (lat != null && lon != null) {
	    	
	    	// Get reachcode containing lat/lon point from the EPA WATERS web service
	    	InputStream reachJson =
	    		sendPacket(new URL("http://iaspub.epa.gov/waters10/waters_services.PointIndexingService?" +
	    			"pGeometry=POINT(" + lon + "%20" + lat + ")" + "&pGeometryMod=WKT,SRID=8307" +
	                "&pPointIndexingMethod=RAINDROP" + "&pPointIndexingRaindropDist=25"), "GET");

	    	String reachCode = parseJSON(reachJson, "reachcode");

	    	// Get geometry of reachcode
    		InputStream json = 
    			sendPacket(new URL("http://iaspub.epa.gov/waters10/waters_services.navigationDelineationService?" +
    				"pNavigationType=UT&pStartReachCode=" + reachCode + "&optOutGeomFormat=GEOGML&pFeatureType=CATCHMENT_TOPO&pMaxDistance=999999999"),
    				"GET");

    		String gml = parseJSON(json, "shape");

    		attributeName = "blah";
    		
    		String fullUserDir = System.getProperty("applicationUserSpaceDir") +
    				userDirectory + FileHelper.getSeparator();
    		
    		
    		try {
    			GeometryCollection g = parseGML(gml);
    			
    			// Write to a shapefile so GeoServer can load the geometry
    			shapefilePath = fullUserDir + "latlon.shp";
    			
    			File shpFile = new File(shapefilePath);
    			File shxFile = new File(fullUserDir + "latlon.shx");
    			
    			if (shpFile.exists()) shpFile.delete();
    			if (shxFile.exists()) shxFile.delete();
    			
    			shpFile.createNewFile();
    			shxFile.createNewFile();
    			
    			FileOutputStream shpFileInputStream = new FileOutputStream(shpFile);
    			FileOutputStream shxFileInputStream = new FileOutputStream(shxFile);
    			ShapefileWriter sw = new ShapefileWriter(shpFileInputStream.getChannel(), 
    					shxFileInputStream.getChannel());
    			sw.write(g, ShapeType.POLYGON);
    			
    			featureCollection = createFeatureCollection(g);
    			
			} catch (SAXException e) {
				e.printStackTrace();
				return null;
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
				return null;
			}

    	} else {

    		FileHelper.deleteFile(uploadDirectory.getPath() + outputFile);


    		FileDataStore shapeFileDataStore;
    		FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = null;

    		// Set up the shapefile
    		String appTempDir = System.getProperty("applicationTempDir");
    		String userDir = userDirectory;
    		if (userDir != null && !"".equals(userspacePath + userDir)) {
    			if (FileHelper.doesDirectoryOrFileExist(userspacePath + userDir)) {
    				FileHelper.updateTimestamp(userspacePath + userDir, false); // Update the timestamp
                                userDir = userspacePath + userDir;
    			} else {
    				userDir = "";
    			}
    		}
    		AvailableFilesBean afb = AvailableFilesBean.getAvailableFilesBean(appTempDir, userDir);
    		List<ShapeFileSetBean> shapeBeanList = afb.getShapeSetList();
    		File shapeFile = null;
    		for (ShapeFileSetBean sfsb : shapeBeanList) {
    			if (shapeSet.equals(sfsb.getName())) {
    				shapeFile = sfsb.getShapeFile();
    				shapefilePath = shapeFile.getAbsolutePath();
    			}
    		}

    		shapeFileDataStore = FileDataStoreFinder.getDataStore(shapeFile);
    		featureSource = shapeFileDataStore.getFeatureSource();

    		if (features[0].equals("*")) {
    			featureCollection = featureSource.getFeatures();
    		} else {
    			//Implementing a filter using the CQL language
    			// http://docs.codehaus.org/display/GEOTOOLS/CQL+Parser+Design
    			String cqlQuery = attribute + " == '" + features[0] + "'";
    			Filter attributeFilter = null;
    			for (int index = 1;index < features.length;index++) {
    				cqlQuery = cqlQuery + " OR " + attribute + " == '" + features[index] + "'";
    			}

    			try {
    				attributeFilter = CQL.toFilter(cqlQuery);
    			} catch (CQLException e) {
    				log.debug(e);
    			}
    			featureCollection = featureSource.getFeatures(
    					new DefaultQuery(
    							featureSource.getSchema().getTypeName(),
    							attributeFilter
    					)
    			);
    		}
    	}


	    DelimiterOption delimiterOption = null;
	    if (delimId != null) {
	        try {
	            delimiterOption = DelimiterOption.valueOf(delimId);
	        } catch (IllegalArgumentException e) { /* failure handled below */}
	    }
	    if (delimiterOption == null) {
	        delimiterOption = DelimiterOption.getDefault();
	    }

        if ("WCS".equals(dataSetInterface)) {

            int wcsRequestHashCode = 7;
            wcsRequestHashCode = 31 * wcsRequestHashCode + (wcsServer == null ? 0 : wcsServer.hashCode());
            wcsRequestHashCode = 31 * wcsRequestHashCode + (wcsCoverage == null ? 0 : wcsCoverage.hashCode());
            wcsRequestHashCode = 31 * wcsRequestHashCode + (wcsBoundingBox == null ? 0 : wcsBoundingBox.hashCode());
            wcsRequestHashCode = 31 * wcsRequestHashCode + (wcsGridCRS == null ? 0 : wcsGridCRS.hashCode());
            wcsRequestHashCode = 31 * wcsRequestHashCode + (wcsGridOffsets == null ? 0 : wcsGridOffsets.hashCode());
            wcsRequestHashCode = 31 * wcsRequestHashCode + (wcsResmapleFilter == null ? 0 : wcsResmapleFilter.hashCode());
            wcsRequestHashCode = 31 * wcsRequestHashCode + (wcsResampleFactor == null ? 0 : wcsResampleFactor.hashCode());


            File wcsRequestOutputFile =
                    new File(System.getProperty("applicationWorkDir"),
                            Integer.toHexString(wcsRequestHashCode) + ".tiff");

            FileOutputStream fos = null;
            InputStream coverageIStream = null;

            try {

                // Create WCS request
                String getCoverageRequest = wcsServer +
                    "?service=WCS" +
                    "&version=1.1.1" +
                    "&request=GetCoverage" +
                    "&identifier=" + wcsCoverage +
                    "&boundingBox=" + wcsBoundingBox + "," + wcsGridCRS +
                    "&gridBaseCRS=" + wcsGridCRS +
                    "&gridOffsets=" + wcsGridOffsets +
                    "&format=image/GeoTIFF";

                // Call getCoverage
                HttpURLConnection httpConnection =
                    openHttpConnection(new URL(getCoverageRequest), "GET");

                coverageIStream =
                    getHttpConnectionInputStream(httpConnection);

                Map<String, List<String>> headerFields =
                    getHttpConnectionHeaderFields(httpConnection);


                String boundaryString = null;

                String contentType[] = headerFields.get("Content-Type").get(0).split(" *; *");
                for (int i = 0; i < contentType.length; i++) {
                    String field[] = contentType[i].split("=");
                    if ("boundary".equals(field[0])) {
                        boundaryString = field[1].substring(1, field[1].length() - 1); // remove quotes
                    }
                }

                if (boundaryString == null) throw new IOException(); // TODO: probably change exception thrown

                int part = 0;

                // find second part (coverage) of multi-part response
                while (part < 2) {

                    String s = readLine(coverageIStream);
                    if (s == null) throw new IOException();

                    if (("--" + boundaryString).equals(s)) {
                        part++;
                    }
                }

                // actual coverage starts immediately after blank line
                String line;
                do {
                    line = readLine(coverageIStream);
                } while (!"".equals(line));


                // write coverage bytes to file
                fos = new FileOutputStream(wcsRequestOutputFile);

                String closeTag = "--" + boundaryString + "--\n";

                byte b[] = new byte[1024];
                int bytesRead;
                while ((bytesRead = coverageIStream.read(b, 0, 1024)) > 0) {

                    String endString = new String(
                            Arrays.copyOfRange(b, bytesRead - closeTag.length(),
                                                  bytesRead));

                    if (closeTag.equals(endString)) {
                        // Don't write close tag to file
                        fos.write(b, 0, bytesRead - closeTag.length());
                        break;
                    }

                    fos.write(b, 0, bytesRead);
                }

            } finally {
                if (fos != null) {
                    try { fos.close(); }
                    catch (IOException e) { /* don't care, unrecoverable */ }
                }
                if ( coverageIStream != null) {
                    try { coverageIStream.close(); }
                    catch (IOException e) { /* don't care, unrecoverable */ }
                }
            }

            dataset = wcsRequestOutputFile.toURI().toURL().toString();
            dataTypes = new String[] { "I0B0" };
		}

		if ("THREDDS".equals(dataSetInterface)) {

        } else {

        }

        String fromTime = from;
        String toTime = to;

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date toDate = new Date();
        Date fromDate = new Date();
        boolean parsedDates = false;
        if (toTime == null || fromTime == null) {
            toDate = null;
            fromDate = null;
            parsedDates = true;
        } else {
            try {
                toDate = df.parse(toTime);
                fromDate = df.parse(fromTime);
                parsedDates = true;
            } catch (ParseException e1) {
                parsedDates = false;
                log.debug(e1.getMessage());
            }
        }

        if (!parsedDates) {
            // return some sort of error
        }


        String datasetUrl = dataset;
        Formatter errorLog = new Formatter();
        FeatureDataset featureDataset = FeatureDatasetFactoryManager.open(FeatureType.ANY, datasetUrl, null, errorLog);

        if (featureDataset.getFeatureType() == FeatureType.GRID && featureDataset instanceof GridDataset) {
            GridDataset gridDataset = (GridDataset)featureDataset;

            String gridName = dataTypes[0];

            GridDatatype gdt = gridDataset.findGridByName(gridName);

            categorical = gdt.getDataType().isIntegral();
            
            if (categorical) {

                BufferedWriter writer = null;
                    try {

                    writer = new BufferedWriter(new FileWriter(new File(System.getProperty("applicationWorkDir"), outputFile)));

                    // *** long running task ***
                    FeatureCategoricalGridCoverage.execute(
                        featureCollection,
                        attributeName,
                        gridDataset,
                        gridName,
                        writer,
                        delimiterOption.delimiter);

                } finally {
                    if (writer != null) {
                        try { writer.close(); } catch (IOException e) { /* get bent */ }
                    }
                }
                
            } else {

                try {
                    Range timeRange = null;
                    try {
                        CoordinateAxis1DTime timeAxis = gdt.getCoordinateSystem().getTimeAxis1D();
                        int timeIndexMin = 0;
                        int timeIndexMax = 0;
                        if (fromDate != null && toDate != null) {
                            timeIndexMin = timeAxis.findTimeIndexFromDate(fromDate);
                            timeIndexMax = timeAxis.findTimeIndexFromDate(toDate);
                            timeRange = new Range(timeIndexMin, timeIndexMax);
                        }

                    } catch (NumberFormatException e) {
                        log.error(e.getMessage());
                    } catch (InvalidRangeException e) {
                        log.error(e.getMessage());
                    }

                    GroupBy.GridOption groupBy = null;
                    if (groupById != null) {
                        try {
                            groupBy = GroupBy.GridOption.valueOf(groupById);
                        } catch (IllegalArgumentException e) { /* failure handled below */}
                    }
                    if (groupBy == null) {
                        groupBy = GroupBy.GridOption.getDefault();
                    }

                    List<Statistic> statisticList = new ArrayList<Statistic>();
                    if (outputStats != null && outputStats.length > 0) {
                        for(int i = 0; i < outputStats.length; ++i) {
                            // may throw exception if outputStats value doesn't
                            // map to Statistic enum value, ivan says let percolate up.
                            statisticList.add(Statistic.valueOf(outputStats[i]));
                        }
                    }

                    if (statisticList.isEmpty()) {
                        throw new IllegalArgumentException("no output statistics selected");
                    }

                    BufferedWriter writer = null;
                    try {

                        writer = new BufferedWriter(new FileWriter(new File(System.getProperty("applicationWorkDir"), outputFile)));

                        // *** long running task ***
                        FeatureCoverageWeightedGridStatistics.execute(
                                featureCollection,
                                attributeName,
                                gridDataset,
                                gridName,
                                timeRange,
                                statisticList,
                                writer,
                                groupBy == GroupBy.GridOption.statistics,
                                delimiterOption.delimiter);

                    } finally {
                        if (writer != null) {
                            try { writer.close(); } catch (IOException e) { /* get bent */ }
                        }
                    }
                }

                finally {
                    try {
                        if (gridDataset != null) gridDataset.close();
                    } catch (IOException e) { /* get bent */ }
                }
            }
        } else if (featureDataset.getFeatureType() == FeatureType.STATION && featureDataset instanceof FeatureDatasetPoint) {
            FeatureDatasetPoint fdp = (FeatureDatasetPoint)featureDataset;
            List<ucar.nc2.ft.FeatureCollection> fcl = fdp.getPointFeatureCollectionList();
            if (fcl != null && fcl.size() == 1) {
                ucar.nc2.ft.FeatureCollection fc = fcl.get(0);
                if (fc != null && fc instanceof StationTimeSeriesFeatureCollection) {

                    StationTimeSeriesFeatureCollection stsfc =
                        (StationTimeSeriesFeatureCollection)fc;

                    List<VariableSimpleIF> variableList = new ArrayList<VariableSimpleIF>();
                    for(String variableName : dataTypes) {
                        VariableSimpleIF variable = featureDataset.getDataVariable(variableName);
                        if (variable != null) {
                            variableList.add(variable);
                        } else {
                            // do we care?
                        }
                    }

                    GroupBy.StationOption groupBy = null;
                    if (groupById != null) {
                        try {
                            groupBy = GroupBy.StationOption.valueOf(groupById);
                        } catch (IllegalArgumentException e) { /* failure handled below */}
                    }
                    if (groupBy == null) {
                        groupBy = GroupBy.StationOption.getDefault();
                    }

                    BufferedWriter writer = null;
                    try {
                        writer = new BufferedWriter(new FileWriter(new File(System.getProperty("applicationWorkDir"), outputFile)));
                        StationDataCSVWriter.write(
                                featureCollection,
                                stsfc,
                                variableList,
                                new DateRange(fromDate, toDate),
                                writer,
                                groupBy == StationOption.variable,
                                delimiterOption.delimiter);
                    } finally {
                        if (writer != null) { try { writer.close(); } catch (IOException e) { /* swallow, don't mask exception */ } }
                    }

                } else {
                    // wtf?  I am gonna punch Ivan...
                }
            } else {
                // error, what do we do when more than one FeatureCollection?  does this happen?  If yes, punch Ivan.
            }

        }
		
		FileHelper.copyFileToFile(new File(System.getProperty("applicationWorkDir") + outputFile), uploadDirectory.getPath(), true);
		
    	File outputDataFile = new File(uploadDirectory.getPath(), outputFile);
    	if (!outputDataFile.exists()) return null;
    	
    	FileLocationBean flb = new FileLocationBean(outputDataFile.getName(), shapefilePath);
    	
		return flb;
	}
	
	private String readLine(InputStream is) throws IOException {
		
		StringBuilder sb = new StringBuilder();

		int b;
		while((b = is.read()) != -1) {
			
			char c = (char) b;  // TODO: convert properly
			
			if (c == '\n') {
				return sb.toString();
			} else {
				sb.append(c);
			}
		}
		
		return null;  // new line not found
	}

	private static InputStream sendPacket(URL url, String requestMethod)
			throws IOException {

		HttpURLConnection httpConnection = openHttpConnection(url,
				requestMethod);

		return getHttpConnectionInputStream(httpConnection);
	}
	
	private static HttpURLConnection openHttpConnection(URL url,
			String requestMethod) throws IOException, ProtocolException {
		
		HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
		httpConnection.setRequestMethod(requestMethod);

		return httpConnection;
	}
	
	private static InputStream getHttpConnectionInputStream(HttpURLConnection httpConnection)
			throws IOException {
		
		return httpConnection.getInputStream();
	}
	
	private static Map<String, List<String>> getHttpConnectionHeaderFields(HttpURLConnection httpConnection)
			throws IOException {
	
		return httpConnection.getHeaderFields();
	}

	private static String parseJSON(InputStream json, String element)
	throws JsonParseException, IOException {

		JsonFactory f = new JsonFactory();
		JsonParser jp = f.createJsonParser(new InputStreamReader(json));

		while (true) {
			jp.nextToken();

			if (!jp.hasCurrentToken())
				break;

			if (element.equals(jp.getCurrentName())) {
				jp.nextToken();
				return jp.getText();
			}
		}

		System.out.println("\"" + element + "\" not found.");
		return null;
	}

	private static GeometryCollection parseGML(String gml)
	throws SchemaException, IOException, SAXException, ParserConfigurationException {

		if (gml == null)
			throw new IOException();

		//create the parser with the gml 2.0 configuration
		org.geotools.xml.Configuration configuration = new org.geotools.gml2.GMLConfiguration();
		org.geotools.xml.Parser parser = new org.geotools.xml.Parser( configuration );


		// TODO: parse fails with when gml is only a single polygon with "Authority "SDO" is unknown".
		//parse
		GeometryCollection geom = (GeometryCollection) parser.parse( new StringReader(gml) );

		return geom;
	}
	
	private static FeatureCollection<SimpleFeatureType, SimpleFeature> 
	createFeatureCollection(GeometryCollection geom)
	throws NoSuchAuthorityCodeException, FactoryException {
		
		FeatureCollection<SimpleFeatureType, SimpleFeature> fc = FeatureCollections.newCollection();

		for (int i = 0; i < geom.getNumGeometries(); i++) {
			Geometry g = geom.getGeometryN(i);

			SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
			typeBuilder.setName("testType");
			typeBuilder.setCRS(CRS.decode("EPSG:4326"));
			typeBuilder.add("blah", Integer.class);
			typeBuilder.add("geom", Geometry.class);
			typeBuilder.setDefaultGeometry("geom");

			SimpleFeatureType type = typeBuilder.buildFeatureType();


//			GeometryFactory geomFactory = new GeometryFactory();
			SimpleFeatureBuilder build = new SimpleFeatureBuilder( type );

//			for (Coordinate c : g.getCoordinates()) {
//				System.out.println(c.x + ", " + c.y);
//				build.add( geomFactory.createPoint( c ));
//			}

			build.set("geom", g);
			build.set("blah", i);

			SimpleFeature sf = build.buildFeature(null);
			sf.getBounds();

//			SimpleFeature sf = SimpleFeatureBuilder.build(type, g.getCoordinates(), null);

			fc.add(sf);
		}

		return fc;
	}

	private static final long serialVersionUID = 1L;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public ProcessServlet() {
        super();
    }

	private boolean sendEmail(String email, String finalUrlEmail) throws AddressException, MessagingException {
		String content = "Your file is ready: " + finalUrlEmail;
		String subject = "Your file is ready";
		String from = "gdp_data@usgs.gov";
		EmailMessageBean emBean = new EmailMessageBean(from, email, new ArrayList<String>(), subject, content);
		EmailHandler emh = new EmailHandler();
		return emh.sendMessage(emBean);
	}
}
