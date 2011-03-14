package gov.usgs.cida.gdp.wps.algorithm.communication;

import gov.usgs.cida.gdp.constants.AppConstant;
import gov.usgs.cida.gdp.dataaccess.ManageGeoserverWorkspace;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.server.AbstractSelfDescribingAlgorithm;

/**
 * Sends commands to our running geoserver instance.
 * User/Pass is mandatory
 *
 * @author isuftin
 */
public class GeoserverManagementAlgorithm extends AbstractSelfDescribingAlgorithm  {
    @Override
    public Map<String, IData> run(Map<String, List<IData>> inputData) {
        if (inputData == null) throw new RuntimeException("Error while allocating input parameters.");
        if (!inputData.containsKey("command")) throw new RuntimeException("Error: Missing input parameter 'command'");
        if (!inputData.containsKey("username")) throw new RuntimeException("Error: Missing input parameter 'username'");
        if (!inputData.containsKey("password")) throw new RuntimeException("Error: Missing input parameter 'password'");

        String command, username, password, workspace = null;
        List<String> datastoreList = new ArrayList<String>();
        
        // Pull in our command
        List<IData> dataList = inputData.get("command");
        command = ((LiteralStringBinding)dataList.get(0)).getPayload();
        if (StringUtils.isBlank(command)) throw new RuntimeException("Error: Missing input parameter 'command'");

        // Pull in our username
        dataList = inputData.get("username");
        username = ((LiteralStringBinding)dataList.get(0)).getPayload();
        if (StringUtils.isBlank(command)) throw new RuntimeException("Error: Missing input parameter 'username'");

        // Pull in our password
        dataList = inputData.get("password");
        password = ((LiteralStringBinding)dataList.get(0)).getPayload();
        if (StringUtils.isBlank(command)) throw new RuntimeException("Error: Missing input parameter 'password'");

        // Pull in our workspace, if any
        dataList = inputData.get("workspace");
        workspace = ((LiteralStringBinding)dataList.get(0)).getPayload();

        // Pull in the datastores, if any
        dataList = inputData.get("datastore");
        for (IData iData : dataList) datastoreList.add(((LiteralStringBinding)iData).getPayload());

        if ("delete".equalsIgnoreCase(command)) {
            if (datastoreList.isEmpty()) throw new RuntimeException("Error: 'delete' called but no datastore was provided");
            if (StringUtils.isBlank(workspace)) throw new RuntimeException("Error: 'delete' called but no workspace was provided");
            ManageGeoserverWorkspace mgw = null;
            
            try { mgw = new ManageGeoserverWorkspace(new URL(AppConstant.WFS_ENDPOINT.getValue())); }
            catch (MalformedURLException ex) { throw new RuntimeException("Error: Unable to delete datastore. Cause is incorrect Geoserver URL. Error follows.\n" + ex.getMessage()); }

            for (String datastore : datastoreList) {
                try { mgw.deleteDatastoreFromGeoserver(workspace, datastore, username, password, true); }
                catch (Exception ex) { getErrors().add("Error: Unable to delete datastore '"+datastore+"'. Error follows.\n" + ex.getMessage()); }
            }
        } else throw new RuntimeException("Error: Unrecognized command: " + command);

        Map<String, IData> result = new HashMap<String, IData>();
        if (getErrors().isEmpty()) result.put("result", new LiteralStringBinding("Your request has completed successfully."));
        else result.put("result", new LiteralStringBinding("Your request has completed with errors."));

        return result;
    }

    @Override
    public List<String> getInputIdentifiers() {
        List<String> result = new ArrayList<String>();
        result.add("command");
        result.add("username");
        result.add("password");
        result.add("workspace");
        result.add("datastore");
        return result;
    }

    @Override
    public BigInteger getMaxOccurs(String identifier) {
        if ("command".equals(identifier)) return BigInteger.valueOf(1);
        if ("username".equals(identifier)) return BigInteger.valueOf(1);
        if ("password".equals(identifier)) return BigInteger.valueOf(1);
        if ("workspace".equals(identifier)) return BigInteger.valueOf(1);
        if ("datastore".equals(identifier)) return BigInteger.valueOf(1);
        return super.getMaxOccurs(identifier);
    }

    @Override
    public BigInteger getMinOccurs(String identifier) {
        if ("command".equals(identifier)) return BigInteger.valueOf(1);
        if ("username".equals(identifier)) return BigInteger.valueOf(1);
        if ("password".equals(identifier)) return BigInteger.valueOf(1);
        if ("workspace".equals(identifier)) return BigInteger.valueOf(0);
        if ("datastore".equals(identifier)) return BigInteger.valueOf(0);
        return super.getMaxOccurs(identifier);
    }

    @Override
    public List<String> getOutputIdentifiers() {
        List<String> result = new ArrayList<String>();
        result.add("result");
        return result;
    }

    @Override
    public Class getInputDataType(String id) {
        if ("command".equals(id)) return LiteralStringBinding.class;
        if ("username".equals(id)) return LiteralStringBinding.class;
        if ("password".equals(id)) return LiteralStringBinding.class;
        if ("workspace".equals(id)) return LiteralStringBinding.class;
        if ("datastore".equals(id)) return LiteralStringBinding.class;
        return null;
    }

    @Override
    public Class getOutputDataType(String id) {
        if ("result".equals(id)) return LiteralStringBinding.class;
        return null;
    }

}
