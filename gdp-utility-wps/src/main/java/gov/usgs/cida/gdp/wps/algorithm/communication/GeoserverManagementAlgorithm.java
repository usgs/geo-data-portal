package gov.usgs.cida.gdp.wps.algorithm.communication;

import gov.usgs.cida.gdp.dataaccess.GeoserverManager;
import java.math.BigInteger;
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
    private final static String PARAM_COMMAND = "command";
    private final static String PARAM_USERNAME = "username";
    private final static String PARAM_PASSWORD = "password";
    private final static String PARAM_WFS_HOST = "wfsHost";
    private final static String PARAM_WFS_PORT = "wfsPort";
    private final static String PARAM_WORKSPACE = "workspace";
    private final static String PARAM_DATASTORE = "datastore";
    private final static String PARAM_RESULT = "result";
    private final static String PARAM_DELETE = "delete";
    
    @Override
    public Map<String, IData> run(Map<String, List<IData>> inputData) {
        if (inputData == null) {
            throw new RuntimeException("Error while allocating input parameters.");
        }
        if (!inputData.containsKey(PARAM_COMMAND)) {
            throw new RuntimeException("Error: Missing input parameter '"+PARAM_COMMAND+"'");
        }
        if (!inputData.containsKey(PARAM_USERNAME)) {
            throw new RuntimeException("Error: Missing input parameter '"+PARAM_USERNAME+"'");
        }
        if (!inputData.containsKey(PARAM_PASSWORD)) {
            throw new RuntimeException("Error: Missing input parameter '"+PARAM_PASSWORD+"'");
        }
        if (!inputData.containsKey(PARAM_WFS_HOST)) {
            throw new RuntimeException("Error: Missing input parameter '"+PARAM_WFS_HOST+"'");
        }
        if (!inputData.containsKey(PARAM_WFS_PORT)) {
            throw new RuntimeException("Error: Missing input parameter '"+PARAM_WFS_PORT+"'");
        }

        String command, username, password, workspace, wfsHost, wfsPort = null;
        
        // Pull in our command
        List<IData> dataList = inputData.get(PARAM_COMMAND);
        command = ((LiteralStringBinding)dataList.get(0)).getPayload();
        if (StringUtils.isBlank(command)) {
            throw new RuntimeException("Error: Missing input parameter '"+PARAM_COMMAND+"'");
        }

        // Pull in our host
        dataList = inputData.get(PARAM_WFS_HOST);
        wfsHost = ((LiteralStringBinding)dataList.get(0)).getPayload();
        if (StringUtils.isBlank(wfsHost)) {
            throw new RuntimeException("Error: Missing input parameter '"+PARAM_WFS_HOST+"'");
        }

        // Pull in our host
        dataList = inputData.get(PARAM_WFS_PORT);
        wfsPort = ((LiteralStringBinding)dataList.get(0)).getPayload();
        if (StringUtils.isBlank(wfsPort)) {
            throw new RuntimeException("Error: Missing input parameter '"+PARAM_WFS_PORT+"'");
        }

        // Pull in our username
        dataList = inputData.get(PARAM_USERNAME);
        username = ((LiteralStringBinding)dataList.get(0)).getPayload();
        if (StringUtils.isBlank(username)) {
            throw new RuntimeException("Error: Missing input parameter '"+PARAM_USERNAME+"'");
        }

        // Pull in our password
        dataList = inputData.get(PARAM_PASSWORD);
        password = ((LiteralStringBinding)dataList.get(0)).getPayload();
        if (StringUtils.isBlank(password)) {
            throw new RuntimeException("Error: Missing input parameter '"+PARAM_PASSWORD+"'");
        }

        // Pull in our workspace, if any
        dataList = inputData.get(PARAM_WORKSPACE);
        workspace = ((LiteralStringBinding)dataList.get(0)).getPayload();

        // Pull in the datastores, if any
        dataList = inputData.get(PARAM_DATASTORE);
        List<String> datastoreList = new ArrayList<String>(dataList.size());
        for (IData iData : dataList) {
            datastoreList.add(((LiteralStringBinding)iData).getPayload());
        }

        if (PARAM_DELETE.equalsIgnoreCase(command)) {
            if (datastoreList.isEmpty()) {
                throw new RuntimeException("Error: '"+PARAM_DELETE+"' called but no datastore was provided");
            }
            if (StringUtils.isBlank(workspace)) {
                throw new RuntimeException("Error: '"+PARAM_DELETE+"' called but no workspace was provided");
            }
            
            GeoserverManager gm = new GeoserverManager(
                    "http://" + wfsHost + ":" + wfsPort + "/geoserver", username, password);
            
            for (String datastore : datastoreList) {
                try { gm.deleteAndWipeDataStore(workspace, datastore); }
                catch (Exception ex) { getErrors().add("Error: Unable to delete datastore '"+datastore+"'. Error follows.\n" + ex.getMessage()); }
            }
        } else {
            throw new RuntimeException("Error: Unrecognized command: " + command);
        }

        Map<String, IData> result = new HashMap<String, IData>(1);
        if (getErrors().isEmpty()) {
            result.put(PARAM_RESULT, new LiteralStringBinding("Your request has completed successfully."));
        }
        else {
            result.put(PARAM_RESULT, new LiteralStringBinding("Your request has completed with errors."));
        }

        return result;
    }

    @Override
    public List<String> getInputIdentifiers() {
        List<String> result = new ArrayList<String>(7);
        result.add(PARAM_COMMAND);
        result.add(PARAM_WFS_HOST);
        result.add(PARAM_WFS_PORT);
        result.add(PARAM_USERNAME);
        result.add(PARAM_PASSWORD);
        result.add(PARAM_WORKSPACE);
        result.add(PARAM_DATASTORE);
        return result;
    }

    @Override
    public BigInteger getMaxOccurs(String identifier) {
        if (PARAM_COMMAND.equals(identifier)) { return BigInteger.valueOf(1); }
        if (PARAM_WFS_HOST.equals(identifier)) { return BigInteger.valueOf(1); }
        if (PARAM_WFS_PORT.equals(identifier)) { return BigInteger.valueOf(1); }
        if (PARAM_USERNAME.equals(identifier)) { return BigInteger.valueOf(1); }
        if (PARAM_PASSWORD.equals(identifier)) { return BigInteger.valueOf(1); }
        if (PARAM_WORKSPACE.equals(identifier)) { return BigInteger.valueOf(1); }
        if (PARAM_DATASTORE.equals(identifier)) { return BigInteger.valueOf(1); }
        return super.getMaxOccurs(identifier);
    }

    @Override
    public BigInteger getMinOccurs(String identifier) {
        if (PARAM_COMMAND.equals(identifier)) {
            return BigInteger.valueOf(1);
        }
        if (PARAM_WFS_HOST.equals(identifier)) {
            return BigInteger.valueOf(1);
        }
        if (PARAM_WFS_PORT.equals(identifier)) {
            return BigInteger.valueOf(1);
        }
        if (PARAM_USERNAME.equals(identifier)) {
            return BigInteger.valueOf(1);
        }
        if (PARAM_PASSWORD.equals(identifier)) {
            return BigInteger.valueOf(1);
        }
        if (PARAM_WORKSPACE.equals(identifier)) {
            return BigInteger.valueOf(0);
        }
        if (PARAM_DATASTORE.equals(identifier)) {
            return BigInteger.valueOf(0);
        }
        return super.getMaxOccurs(identifier);
    }

    @Override
    public List<String> getOutputIdentifiers() {
        List<String> result = new ArrayList<String>(1);
        result.add(PARAM_RESULT);
        return result;
    }

    /**
     * 
     * @param id
     * @return
     */
    @Override
    public Class getInputDataType(String id) {
        if (PARAM_COMMAND.equals(id)) {
            return LiteralStringBinding.class;
        }
        if (PARAM_WFS_HOST.equals(id)) {
            return LiteralStringBinding.class;
        }
        if (PARAM_WFS_PORT.equals(id)) {
            return LiteralStringBinding.class;
        }
        if (PARAM_USERNAME.equals(id)) {
            return LiteralStringBinding.class;
        }
        if (PARAM_PASSWORD.equals(id)) {
            return LiteralStringBinding.class;
        }
        if (PARAM_WORKSPACE.equals(id)) {
            return LiteralStringBinding.class;
        }
        if (PARAM_DATASTORE.equals(id)) {
            return LiteralStringBinding.class;
        }
        return null;
    }

    @Override
    public Class getOutputDataType(String id) {
        if (PARAM_RESULT.equals(id)) {
            return LiteralStringBinding.class;
        }
        return null;
    }

}

