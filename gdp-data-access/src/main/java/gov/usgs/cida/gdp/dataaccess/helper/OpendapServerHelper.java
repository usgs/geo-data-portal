package gov.usgs.cida.gdp.dataaccess.helper;

import gov.usgs.cida.gdp.dataaccess.bean.DataTypeCollection;
import gov.usgs.cida.gdp.dataaccess.bean.DataTypeCollection.DataTypeBean;
import gov.usgs.cida.gdp.utilities.bean.Time;
import gov.usgs.cida.gdp.utilities.bean.XmlResponse;
import java.io.FileNotFoundException;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import opendap.dap.Attribute;
import opendap.dap.AttributeTable;
import opendap.dap.BaseType;
import opendap.dap.BaseTypePrimitiveVector;
import opendap.dap.DAP2Exception;
import opendap.dap.DAS;
import opendap.dap.DArray;
import opendap.dap.DArrayDimension;
import opendap.dap.DConnect2;
import opendap.dap.DDS;
import opendap.dap.DDSException;
import opendap.dap.DGrid;
import opendap.dap.DInt32;
import opendap.dap.DataDDS;
import opendap.dap.Int32PrimitiveVector;
import opendap.dap.PrimitiveVector;
import opendap.dap.StatusUI;
import org.slf4j.LoggerFactory;
import ucar.nc2.units.DateUnit;

public class OpendapServerHelper {

	static org.slf4j.Logger log = LoggerFactory.getLogger(OpendapServerHelper.class);

//    /**
//     * Tests whether or not a THREDDS server is reachable
//     */
//    public static boolean isServerReachable(String serverURL) {
//
//        URL url;
//        try {
//            url = new URL(serverURL);
//        } catch (MalformedURLException ex) {
//            return false;
//        }
//
//        String host = url.getHost();
//
//        int port = url.getPort();
//
//        // If port isn't specified, use the protocol's default port
//        if (port == -1) port = url.getDefaultPort();
//
//        // If there is no default port for the protocol, or the protocol is
//        // unknown, give port 80 (default http port) a try because we have
//        // nothing else to go on.
//        if (port == -1) {
//            port = 80;
//        }
//
//        Socket testSocket = new Socket();
//        InetSocketAddress address = new InetSocketAddress(host, port);
//        try {
//            // 5 sec timeout
//            testSocket.connect(address, 5000);
//        } catch (IOException ex) {
//            return false;
//        } finally {
//            try {
//                testSocket.close();
//            } catch (IOException ex) { }
//        }
//
//        return true;
//    }
	public static Time getTimeBean(String datasetUrl, String gridSelection) throws IOException, ParseException {

		//List<String> dateRangeOld = NetCDFUtility.getDateRange(datasetUrl, gridSelection);
		List<String> dateRange = getOPeNDAPTimeRange(datasetUrl, gridSelection);
		if (dateRange.isEmpty()) {
			boolean hasTimeCoord = NetCDFUtility.hasTimeCoordinate(datasetUrl);
			if (hasTimeCoord) { // This occurs when there is no date range
				// in the file but dataset has time coords. We want the user
				// to pick dates but don't have a range to give.
				dateRange.add("1800-01-01 00:00:00Z");
				dateRange.add("2100-12-31 00:00:00Z");
			}
		}

		Time timeBean = new Time(dateRange);

		return timeBean;
	}

	public static List<String> getOPeNDAPTimeRange(String datasetUrl, String gridSelection) throws IOException {
		//Date minDate = new Date(Long.MAX_VALUE);
		//Date maxDate = new Date(Long.MIN_VALUE);
		List<String> returnList = new LinkedList<String>();
		try {
			// call das, dds
			String finalUrl = "";
			if (datasetUrl.startsWith("dods:")) {
				finalUrl = "http:" + datasetUrl.substring(5);
			}
			else {
				if (datasetUrl.startsWith("http:")) {
					//this.location = "dods:" + datasetURL.substring(5);
				}
				else {
					throw new java.net.MalformedURLException(datasetUrl + " must start with dods: or http:");
				}
			}
			DConnect2 dodsConnection = new DConnect2(finalUrl, false);
			//DataDDS dds = dodsConnection.getData("?" + gridSelection, null);
			DDS dds = dodsConnection.getDDS(gridSelection);
			
			//dodsConnection.getData(gridSelection, null);
//			DArray var = (DArray)dds.getVar(2);
//			Int32PrimitiveVector primitiveVector = (Int32PrimitiveVector)var.getPrimitiveVector();
//			primitiveVector.getValue(3);
			DAS das = dodsConnection.getDAS();
			DGrid grid = (DGrid)dds.getVariable(gridSelection);
			DArray array = (DArray)grid.getVar(grid.ARRAY);
			Enumeration<DArrayDimension> dimensions = array.getDimensions();

			while (dimensions.hasMoreElements()) {
				DArrayDimension nextDim = dimensions.nextElement();
				String name = nextDim.getName();
				AttributeTable attributeTable = das.getAttributeTable(name);
				BaseType dimVar = dds.getVariable(name);
				Attribute units = attributeTable.getAttribute("units");
				if (units != null) {
					try {
						DateUnit dateUnit = new DateUnit(units.getValueAt(0));
//						BaseTypePrimitiveVector times = (BaseTypePrimitiveVector)dimVar.newPrimitiveVector();
//						DArray outer = (DArray)times.getTemplate();
//						PrimitiveVector inheritVector = outer.getPrimitiveVector();
//						inheritVector.getLength();
						//DInt32 inner = (DInt32)inheritVector.getTemplate();
						//PrimitiveVector primitiveVector = inner.getPrimitiveVector();
						//PrimitiveVector arrayVector = arrayTemplate.newPrimitiveVector();
						
						//template.newPrimitiveVector()
						//for (int i=0; i<inheritVector.getLength(); i++) {
							//BaseType valTemp = times.getTemplate();
							//PrimitiveVector newPrimitiveVector = template.newPrimitiveVector();
							//DInt32 val = (DInt32)times.getValue(i);
						// HACK, will only work for strides
							int first = nextDim.getStart()+1; // TODO indexing issue?
							int last = nextDim.getStop()+1 * nextDim.getStride(); // TODO same as above
//				            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//							Date d1 = dateUnit.makeDate(first);
//							Date d2 = dateUnit.makeDate(last);
//							returnList.add(sdf.format(d1));
//							returnList.add(sdf.format(d2));
							returnList.add(dateUnit.makeStandardDateString(first));
							returnList.add(dateUnit.makeStandardDateString(last));
							return returnList;
							//if (d.before(minDate)) minDate = d;
							//if (d.after(maxDate)) maxDate = d;
						//}
					}
					catch (Exception e) {
						e.getMessage();
						// not time unit
					}
				}
			}
			return returnList;
		}
		catch (opendap.dap.parser.ParseException ex) {
			Logger.getLogger(OpendapServerHelper.class.getName()).log(Level.SEVERE, null, ex);
		}
		catch (DAP2Exception ex) {
			Logger.getLogger(OpendapServerHelper.class.getName()).log(Level.SEVERE, null, ex);
		}
		catch (Exception ex) {
			Logger.getLogger(OpendapServerHelper.class.getName()).log(Level.SEVERE, null, ex);
		}
		finally {
			return returnList;
		}
	}

//    /**
//     * Returns a list of dataset handles from the specified server.
//     *
//     * @param hostname
//     * @param port
//     * @param uri
//     * @param serviceType
//     * @return
//     */
//    public static List<InvAccess> getDatasetHandlesFromServer(
//            String hostname, int port, String uri, ServiceType serviceType) {
//        try {
//            InvCatalog catalog = getCatalogFromServer(hostname, port, uri);
//            return NetCDFUtility.getDatasetHandles(catalog, serviceType);
//        } catch (IOException e) {
//            return Collections.emptyList();
//        }
//    }
//
//    public static InvCatalog getCatalogFromServer(String hostname, int port, String uri) throws IOException {
//        String ThreddsURL = "http://" + hostname + ":" + port + uri;
//        URI catalogURI = URI.create(ThreddsURL);
//        InvCatalogFactory factory = new InvCatalogFactory("default", true);
//        InvCatalog catalog = factory.readXML(catalogURI);
//
//        StringBuilder buff = new StringBuilder();
//        if (!catalog.check(buff)) {
//            throw new IOException(buff.toString());
//        }
//
//        return catalog;
//    }
//
//
//    public static void getDatasetListFromServer(String catalogURL) throws URISyntaxException {
//        InvCatalogFactory factory = new InvCatalogFactory("default", true);
//        InvCatalog catalog = factory.readXML(new URI(catalogURL));
//        List<InvDataset> dsList = catalog.getDatasets();
//
//    }
	public static List<XmlResponse> getGridBeanListFromServer(String datasetUrl)
			throws IllegalArgumentException, IOException {

		List<XmlResponse> result = new ArrayList<XmlResponse>();

		//DataTypeCollection dtcb = NetCDFUtility.getDataTypeCollection(datasetUrl);
		DataTypeCollection dtcb = callDDSandDAS(datasetUrl);
		result.add(dtcb);
		return result;
	}

	public static DataTypeCollection callDDSandDAS(String datasetUrl) throws IOException {
		// call das, dds
		String finalUrl = "";
		if (datasetUrl.startsWith("dods:")) {
			finalUrl = "http:" + datasetUrl.substring(5);
		}
		else {
			if (datasetUrl.startsWith("http:")) {
				//this.location = "dods:" + datasetURL.substring(5);
			}
			else {
				throw new java.net.MalformedURLException(datasetUrl + " must start with dods: or http:");
			}
		}
		DConnect2 dodsConnection = new DConnect2(finalUrl, false);
		List<DataTypeBean> dtbList = new LinkedList<DataTypeBean>();
		try {
			DDS dds = dodsConnection.getDDS();
			DAS das = dodsConnection.getDAS();

			Enumeration<BaseType> variables = dds.getVariables();
			while (variables.hasMoreElements()) {
				BaseType nextElement = variables.nextElement();
				if ("Grid".equals(nextElement.getTypeName())) {
					DGrid grid = (DGrid) nextElement;
					DataTypeBean dtb = new DataTypeBean();
					//Enumeration<BaseType> variables1 = grid.getVariables();
					DArray array = (DArray) grid.getVar(grid.ARRAY); // Array, not map
					Enumeration<DArrayDimension> dimensions = array.getDimensions();
					int[] dims = new int[array.numDimensions()];
					int i = 0;
					while (dimensions.hasMoreElements()) {
						DArrayDimension dim = dimensions.nextElement();
						dims[i] = dim.getSize();
						i++;
					}
					String name = grid.getLongName();
					AttributeTable dasAttrs = das.getAttributeTable(name);
					Attribute long_name = dasAttrs.getAttribute("long_name");
					String long_val = (long_name == null) ? name : long_name.getValueAt(0);
					String units = dasAttrs.getAttribute("units").getValueAt(0);
					dtb.setDescription(long_val);
					dtb.setName(name);
					dtb.setRank(dims.length);
					dtb.setShape(dims);
					dtb.setShortname(name);
					dtb.setUnitsstring(units);
					dtbList.add(dtb);
				}
			}
		}
		catch (opendap.dap.parser.ParseException ex) {
			// do something with exceptions
		}
		catch (DAP2Exception ex) {
			// do something with exceptions
		}
		// build datatypebeans
		// put them in new DataTypeCollection
		DataTypeBean[] dtbArr = new DataTypeBean[dtbList.size()];
		dtbList.toArray(dtbArr);
		DataTypeCollection dtc = new DataTypeCollection("GRID", dtbArr); // TODO shouldn't be explicit GRID, didn't know where to get it
		// return
		return dtc;
	}
}
