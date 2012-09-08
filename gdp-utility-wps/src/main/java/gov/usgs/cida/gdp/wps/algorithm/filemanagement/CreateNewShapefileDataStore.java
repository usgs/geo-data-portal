package gov.usgs.cida.gdp.wps.algorithm.filemanagement;

import com.google.common.base.Preconditions;
import gov.usgs.cida.gdp.constants.AppConstant;
import gov.usgs.cida.gdp.dataaccess.GeoserverManager;
import gov.usgs.cida.gdp.utilities.GeoToolsUtils;
import gov.usgs.cida.n52.wps.algorithm.AbstractAnnotatedAlgorithm;
import gov.usgs.cida.n52.wps.algorithm.annotation.Algorithm;
import gov.usgs.cida.n52.wps.algorithm.annotation.LiteralDataInput;
import gov.usgs.cida.n52.wps.algorithm.annotation.LiteralDataOutput;
import gov.usgs.cida.n52.wps.algorithm.annotation.Process;
import java.io.File;
import java.util.UUID;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This algorithm calls the EPA WATERS service which queries the NHD.
 *
 * @author razoerb
 */
@Algorithm(version="1.0.0")
public class CreateNewShapefileDataStore extends AbstractAnnotatedAlgorithm {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(CreateNewShapefileDataStore.class);

    private final static String INPUT_NAME = "name";
    private final static String OUTPUT_LAYER_NAME = "layer-name";
    
    private String name;
    private String layerName;
    
    @LiteralDataInput(identifier=INPUT_NAME)
    public void setName(String name) {
        this.name = name;
    }
    
    @LiteralDataOutput(identifier=OUTPUT_LAYER_NAME)
    public String getLayerName() {
        return layerName;
    }
    
    @Process
    public void process() {
        Preconditions.checkArgument(StringUtils.isNotBlank(name), "Invalid " + INPUT_NAME);

        String outputDir = AppConstant.SHAPEFILE_LOCATION.getValue() + File.separator + UUID.randomUUID();

        File shapefile;
        try {
            shapefile = GeoToolsUtils.createEmptyShapefile(outputDir, name);
        } catch (Exception ex) {
            LOGGER.error("Error creating shapefile", ex);
            addError(ex.getMessage());
            throw new RuntimeException("Error creating shapefile",ex);
        }

        String shapefilePath = shapefile.getAbsolutePath();

        String geoServerURL = AppConstant.WFS_ENDPOINT.getValue();
        String geoServerUser = AppConstant.WFS_USER.getValue();
        String geoServerPass = AppConstant.WFS_PASS.getValue();
        String geoServerWorkspace = "draw";
        try {
            GeoserverManager gm = new GeoserverManager(geoServerURL, geoServerUser, geoServerPass);
            
            String declaredCRS = "EPSG:4326";
            gm.createDataStore(shapefilePath, name, geoServerWorkspace, declaredCRS, declaredCRS);
        } catch (Exception ex) {
            LOGGER.error("Error creating datastore in GeoServer for draw geometry", ex);
            addError(ex.getMessage());
            throw new RuntimeException("Error creating datastore in GeoServer for draw geometry",ex);
        }
        
        layerName = geoServerWorkspace + ":" + name;
    }
}
