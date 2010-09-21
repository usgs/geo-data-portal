package gov.usgs.cida.gdp.dataaccess.helper;

import gov.usgs.cida.gdp.dataaccess.bean.DataTypeCollection;
import gov.usgs.cida.gdp.utilities.bean.Time;
import gov.usgs.cida.gdp.utilities.bean.XmlResponse;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Formatter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.slf4j.LoggerFactory;

import thredds.catalog.*;
import ucar.nc2.VariableSimpleIF;
import ucar.nc2.ft.FeatureDataset;
import ucar.nc2.ft.FeatureDatasetFactoryManager;

public class THREDDSServerHelper {

    static org.slf4j.Logger log = LoggerFactory.getLogger(THREDDSServerHelper.class);

    /**
     * Sets the default timeout for this function to 5 seconds
     *
     * @see THREDDSServerHelper#isServerReachable(java.lang.String, int, int)
     * @param host
     * @param port
     * @return
     */
    public static boolean isServerReachable(final String host, final int port) {
        return THREDDSServerHelper.isServerReachable(host, port, 5000);
    }

    /**
     * Tests whether or not a THREDDS server is reachable
     *
     * @param host
     * @param port
     * @param timeout - milliseconds
     * @return
     * @throws IOException
     */
    public static boolean isServerReachable(final String host, final int port, final int timeout) {
        boolean result = false;

        Socket testSocket = new Socket();
        InetSocketAddress address = new InetSocketAddress(host, port);
        try {
            testSocket.connect(address, timeout);
        } catch (IOException ex) {
            Logger.getLogger(THREDDSServerHelper.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        result = testSocket.isConnected();
        if (result) {
            try {
                testSocket.close();
            } catch (IOException ex) {
                Logger.getLogger(THREDDSServerHelper.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return result;
    }

    public static Time getTimeBean(String datasetUrl, String gridSelection) throws IOException, ParseException {
        Formatter errorLog = new Formatter();
        FeatureDataset featureDataset = null;
        featureDataset =
                FeatureDatasetFactoryManager.open(null, datasetUrl, null, errorLog);

        if (featureDataset != null) {
            try {
                List<String> dateRange = NetCDFUtility.getDateRange(datasetUrl, gridSelection);
                if (dateRange.isEmpty()) {
                    boolean hasTimeCoord = NetCDFUtility.hasTimeCoordinate(datasetUrl);
                    if (hasTimeCoord) { // This occurs when there is no date range in the file but has time coords
                        // We want the user to pick dates but don't have a range to give
                        dateRange.add("1800-01-01 00:00:00Z");
                        dateRange.add("2100-12-31 00:00:00Z");
                    }
                }

                Time timeBean = new Time(dateRange);

                if (timeBean != null /*&& !timeBean.getTime().isEmpty()*/) {
                    return timeBean;
                }

            } finally {
                featureDataset.close();
            }
        }
        return null;
    }

    /**
     * Returns a list of dataset handles from the specified server.
     * 
     * @param hostname
     * @param port
     * @param uri
     * @param serviceType
     * @return
     */
    public static List<InvAccess> getDatasetHandlesFromServer(
            String hostname, int port, String uri, ServiceType serviceType) {
        try {
            InvCatalog catalog = getCatalogFromServer(hostname, port, uri);
            return NetCDFUtility.getDatasetHandles(catalog, serviceType);
        } catch (IOException e) {
            return Collections.emptyList();
        }
    }

    public static InvCatalog getCatalogFromServer(String hostname, int port, String uri) throws IOException {
        String ThreddsURL = "http://" + hostname + ":" + port + uri;
        URI catalogURI = URI.create(ThreddsURL);
        InvCatalogFactory factory = new InvCatalogFactory("default", true);
        InvCatalog catalog = factory.readXML(catalogURI);

        StringBuilder buff = new StringBuilder();
        if (!catalog.check(buff)) {
            throw new IOException(buff.toString());
        }

        return catalog;
    }

    
    public static void getDatasetListFromServer(URL catalogURL) throws URISyntaxException {
        InvCatalogFactory factory = new InvCatalogFactory("default", true);
        InvCatalog catalog = factory.readXML(catalogURL.toURI());
        List<InvDataset> dsList = catalog.getDatasets();
        
    }
    
    public static List<XmlResponse> getGridBeanListFromServer(String datasetUrl) throws IllegalArgumentException, IOException {

        if (datasetUrl == null || "".equals(datasetUrl)) {
            throw new IllegalArgumentException("DataSet URL invalid or null");
        }

        List<XmlResponse> result = new ArrayList<XmlResponse>();
        List<VariableSimpleIF> variables = NetCDFUtility.getDataVariableNames(datasetUrl);
        String type = NetCDFUtility.getDatasetType(datasetUrl);
        DataTypeCollection dtcb = new DataTypeCollection(type, variables.toArray(new VariableSimpleIF[0]));
        result.add(dtcb);
        return result;

    }
}
