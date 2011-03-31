package gov.usgs.cida.gdp.wps.algorithm.filemanagement;

import gov.usgs.cida.gdp.constants.AppConstant;
import gov.usgs.cida.gdp.dataaccess.ManageGeoserverWorkspace;
import gov.usgs.cida.gdp.utilities.GeoToolsUtils;
import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.server.AbstractSelfDescribingAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This algorithm calls the EPA WATERS service which queries the NHD.
 *
 * @author razoerb
 */
public class CreateNewShapefileDataStore extends AbstractSelfDescribingAlgorithm {
    Logger log = LoggerFactory.getLogger(CreateNewShapefileDataStore.class);

    @Override
    public Map<String, IData> run(Map<String, List<IData>> inputData) {

        if (inputData == null) throw new RuntimeException("Error while allocating input parameters.");

        if (!inputData.containsKey("name")) throw new RuntimeException("Error: Missing input parameter 'name'");

        String name = ((LiteralStringBinding) inputData.get("name").get(0)).getPayload();

        Map<String, IData> result = new HashMap<String, IData>();

        String outputDir = AppConstant.SHAPEFILE_LOCATION.getValue() + File.separator + UUID.randomUUID();

        File shapefile;
        try {
            shapefile = GeoToolsUtils.createEmptyShapefile(outputDir, name);
        } catch (Exception ex) {
            log.error("Error creating shapefile", ex);
            throw new RuntimeException("Error creating shapefile");
        }

        String shapefilePath = shapefile.getAbsolutePath();

        String geoServerURL = AppConstant.WFS_ENDPOINT.getValue();
        String geoServerWorkspace = "draw";
        try {
            ManageGeoserverWorkspace mws = new ManageGeoserverWorkspace(geoServerURL);
            String declaredCRS = "EPSG:4326";
            mws.createDataStore(shapefilePath, name, geoServerWorkspace, declaredCRS, declaredCRS);
        } catch (Exception ex) {
            log.error("Error creating datastore in GeoServer for draw geometry", ex);
            throw new RuntimeException("Error creating datastore in GeoServer for draw geometry");
        }
        
        result.put("layer-name", new LiteralStringBinding(geoServerWorkspace + ":" + name));
        return result;
    }

    @Override
    public BigInteger getMaxOccurs(String identifier) {
        if ("name".equals(identifier)) {
            return BigInteger.valueOf(1);
        }
        return super.getMaxOccurs(identifier);
    }

    @Override
    public BigInteger getMinOccurs(String identifier) {
        if ("name".equals(identifier)) {
            return BigInteger.valueOf(1);
        }
        return super.getMinOccurs(identifier);
    }

    @Override
    public List<String> getInputIdentifiers() {
        List<String> result = new ArrayList<String>();
        result.add("name");
        return result;
    }

    @Override
    public List<String> getOutputIdentifiers() {
        List<String> result = new ArrayList<String>();
        result.add("layer-name");
        return result;
    }

    @Override
    public Class getInputDataType(String id) {
        if ("name".equals(id)) {
            return LiteralStringBinding.class;
        }
        return null;
    }

    @Override
    public Class getOutputDataType(String id) {
        if (id.equals("layer-name")) {
            return LiteralStringBinding.class;
        }
        return null;
    }
}
