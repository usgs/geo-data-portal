package gov.usgs.cida.gdp.dataaccess.helper;

import gov.usgs.cida.gdp.dataaccess.bean.DataTypeCollection;
import gov.usgs.cida.gdp.utilities.bean.Time;
import gov.usgs.cida.gdp.utilities.bean.XmlResponse;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.LoggerFactory;

import thredds.catalog.*;
import ucar.nc2.VariableSimpleIF;

public class THREDDSServerHelper {

    static org.slf4j.Logger log = LoggerFactory.getLogger(THREDDSServerHelper.class);

    /**
     * Tests whether or not a THREDDS server is reachable
     */
    public static boolean isServerReachable(String serverURL) {
        
        URL url;
        try {
            url = new URL(serverURL);
        } catch (MalformedURLException ex) {
            return false;
        }

        String host = url.getHost();

        int port = url.getPort();

        // If port isn't specified, use the protocol's default port
        if (port == -1) port = url.getDefaultPort();

        // If there is no default port for the protocol, or the protocol is
        // unknown, give port 80 (default http port) a try because we have
        // nothing else to go on.
        if (port == -1) {
            port = 80;
        }

        Socket testSocket = new Socket();
        InetSocketAddress address = new InetSocketAddress(host, port);
        try {
            // 5 sec timeout
            testSocket.connect(address, 5000);
        } catch (IOException ex) {
            return false;
        } finally {
            try {
                testSocket.close();
            } catch (IOException ex) { }
        }

        return true;
    }

    public static Time getTimeBean(String datasetUrl, String gridSelection) throws IOException, ParseException {

        List<String> dateRange = NetCDFUtility.getDateRange(datasetUrl, gridSelection);
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

    
    public static void getDatasetListFromServer(String catalogURL) throws URISyntaxException {
        InvCatalogFactory factory = new InvCatalogFactory("default", true);
        InvCatalog catalog = factory.readXML(new URI(catalogURL));
        List<InvDataset> dsList = catalog.getDatasets();
        
    }
    
    public static List<XmlResponse> getGridBeanListFromServer(String datasetUrl)
            throws IllegalArgumentException, IOException {

        List<XmlResponse> result = new ArrayList<XmlResponse>();
        List<VariableSimpleIF> variables = NetCDFUtility.getDataVariableNames(datasetUrl);
        String type = NetCDFUtility.getDatasetType(datasetUrl);
        DataTypeCollection dtcb = new DataTypeCollection(type, variables.toArray(new VariableSimpleIF[0]));
        result.add(dtcb);
        return result;
    }
}
