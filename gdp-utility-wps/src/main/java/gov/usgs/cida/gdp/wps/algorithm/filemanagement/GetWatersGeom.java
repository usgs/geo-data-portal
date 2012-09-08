package gov.usgs.cida.gdp.wps.algorithm.filemanagement;

import com.google.common.base.Preconditions;
import gov.usgs.cida.gdp.constants.AppConstant;
import gov.usgs.cida.gdp.dataaccess.GeoserverManager;
import gov.usgs.cida.gdp.dataaccess.WatersService;
import gov.usgs.cida.n52.wps.algorithm.AbstractAnnotatedAlgorithm;
import gov.usgs.cida.n52.wps.algorithm.annotation.Algorithm;
import gov.usgs.cida.n52.wps.algorithm.annotation.LiteralDataInput;
import gov.usgs.cida.n52.wps.algorithm.annotation.LiteralDataOutput;
import gov.usgs.cida.n52.wps.algorithm.annotation.Process;
import java.io.File;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This algorithm calls the EPA WATERS service which queries the NHD.
 *
 * @author razoerb
 */
@Algorithm(version="1.0.0")
public class GetWatersGeom extends AbstractAnnotatedAlgorithm {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(GetWatersGeom.class);

    private final static String INPUT_LAT = "lat";
    private final static String INPUT_LON = "lon";
    private final static String INPUT_NAME = "name";
    private final static String OUTPUT_LAYER_NAME = "layer-name";
    
    private String lat;
    private String lon;
    private String name;
    private String layerName;
    
    @LiteralDataInput(identifier=INPUT_LAT)
    public void setLat(String lat) {
        this.lat = lat;
    }
    
    @LiteralDataInput(identifier=INPUT_LON)
    public void setLong(String lon) {
        this.lon = lon;
    }
    
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
        Preconditions.checkArgument(StringUtils.isNotBlank(lat), "Invalid " + INPUT_LAT);
        Preconditions.checkArgument(StringUtils.isNotBlank(lon), "Invalid " + INPUT_LON);
        Preconditions.checkArgument(StringUtils.isNotBlank(lat), "Invalid " + INPUT_NAME);

        File shapefile;
        try {
            shapefile = WatersService.getGeometry(lon, lat, name);
        } catch (Exception ex) {
            LOGGER.error("Error getting geometry from WATERS", ex);
            addError(ex.getMessage());
            throw new RuntimeException("Error getting geometry from WATERS", ex);
        }

        String shapefilePath = shapefile.getAbsolutePath();
        LOGGER.debug("WATERS shapefile path: '" + shapefilePath + "'");

        String geoServerURL = AppConstant.WFS_ENDPOINT.getValue();
        String geoServerWorkspace = "waters";
        String geoServerLayer = shapefile.getName().replace(".shp", "");
        try {
            GeoserverManager mws = new GeoserverManager(geoServerURL,
                    AppConstant.WFS_USER.getValue(), AppConstant.WFS_PASS.getValue());
            
            String declaredCRS = "EPSG:4269";
            mws.createDataStore(shapefilePath, geoServerLayer, geoServerWorkspace, declaredCRS, declaredCRS);
        } catch (Exception ex) {
            LOGGER.error("Error creating datastore in GeoServer for WATERS geometry", ex);
            addError(ex.getMessage());
            throw new RuntimeException("Error creating datastore in GeoServer for WATERS geometry", ex);
        }
        
        layerName = geoServerWorkspace + ":" + geoServerLayer;
    }
}
