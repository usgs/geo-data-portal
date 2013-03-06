package gov.usgs.cida.gdp.wps.algorithm.communication;

import com.google.common.base.Preconditions;
import gov.usgs.cida.gdp.dataaccess.GeoserverManager;
import org.apache.commons.lang.StringUtils;
import org.n52.wps.algorithm.annotation.Algorithm;
import org.n52.wps.algorithm.annotation.Execute;
import org.n52.wps.algorithm.annotation.LiteralDataInput;
import org.n52.wps.algorithm.annotation.LiteralDataOutput;
import org.n52.wps.server.AbstractAnnotatedAlgorithm;

/**
 * Sends commands to our running geoserver instance.
 * User/Pass is mandatory
 *
 * @author isuftin
 */
@Algorithm(version="1.0.0")
public class GeoserverManagementAlgorithm extends AbstractAnnotatedAlgorithm  {
    
    public enum COMMAND {
        delete;
    }
    
    private final static String PARAM_COMMAND = "command";
    private final static String PARAM_USERNAME = "username";
    private final static String PARAM_PASSWORD = "password";
    private final static String PARAM_WFS_HOST = "wfsHost";
    private final static String PARAM_WFS_PORT = "wfsPort";
    private final static String PARAM_WORKSPACE = "workspace";
    private final static String PARAM_DATASTORE = "datastore";
    private final static String PARAM_RESULT = "result";
    
    private COMMAND command;
    private String username;
    private String password;
    private String wfsHost;
    private String wfsPort;
    private String workspace;
    private String datastore;
    private String result;
    
    @LiteralDataInput(identifier=PARAM_COMMAND)
    public void setCommand(COMMAND command) {
        this.command = command;
    }
    
    @LiteralDataInput(identifier=PARAM_USERNAME)
    public void setUsername(String username) {
        this.username = username;
    }
    
    @LiteralDataInput(identifier=PARAM_WFS_HOST)
    public void setWfsHost(String wfsHost) {
        this.wfsHost = wfsHost;
    }
    
    @LiteralDataInput(identifier=PARAM_WFS_PORT)
    public void setWfsPort(String wfsPort) {
        this.wfsPort = wfsPort;
    }
    
    @LiteralDataInput(identifier=PARAM_WORKSPACE, minOccurs=0)
    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }
    
    @LiteralDataInput(identifier=PARAM_DATASTORE, minOccurs=0)
    public void setDatastore(String datastore) {
        this.datastore = datastore;
    }
    
    @LiteralDataOutput(identifier=PARAM_RESULT)
    public String getResult() {
        return result;
    }
    
    @Execute
    public void process() {
        // required arguments, must not be empty (null, "", or all whitespace);
        Preconditions.checkArgument(command != null, "Invalid " + PARAM_COMMAND);
        Preconditions.checkArgument(!StringUtils.isBlank(username), "Invalid " + PARAM_USERNAME);
        Preconditions.checkArgument(!StringUtils.isBlank(password), "Invalid " + PARAM_PASSWORD);
        Preconditions.checkArgument(!StringUtils.isBlank(wfsHost), "Invalid " + PARAM_WFS_HOST);
        Preconditions.checkArgument(!StringUtils.isBlank(wfsPort), "Invalid " + PARAM_WFS_PORT);
        // optional arguments, may be null OR not empty ("", or all whitespace)
        Preconditions.checkArgument(workspace == null || !StringUtils.isBlank(workspace), "Invalid " + PARAM_WORKSPACE);
        Preconditions.checkArgument(datastore == null || !StringUtils.isBlank(datastore), "Invalid " + PARAM_DATASTORE);
        // argument interdependencies
        Preconditions.checkArgument(command == COMMAND.delete && !StringUtils.isBlank(workspace), "\"" + command + "\" requires " + PARAM_WORKSPACE + " to be set");
        Preconditions.checkArgument(command == COMMAND.delete && !StringUtils.isBlank(datastore), "\"" + command + "\" requires " + PARAM_DATASTORE + " to be set");
    
        GeoserverManager gm = new GeoserverManager("http://" + wfsHost + ":" + wfsPort + "/geoserver", username, password);

        try {
             gm.deleteAndWipeDataStore(workspace, datastore);
             result = "Your request has completed successfully.";
         }catch (Exception ex) {
             result = "Your request has completed with errors.";
             getErrors().add("Error: Unable to delete datastore '" + datastore + "'. Error follows.\n" + ex.getMessage());
         }
    }
}