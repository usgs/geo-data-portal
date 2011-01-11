package gov.usgs.cida.gdp.wps.algorithm.filemanagement;

import gov.usgs.cida.gdp.constants.AppConstant;
import gov.usgs.cida.gdp.dataaccess.ManageGeoserverWorkspace;
import gov.usgs.cida.gdp.dataaccess.WatersService;
import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public class GetWatersGeom extends AbstractSelfDescribingAlgorithm {
    Logger log = LoggerFactory.getLogger(GetWatersGeom.class);

    @Override
    public Map<String, IData> run(Map<String, List<IData>> inputData) {

        if (inputData == null) throw new RuntimeException("Error while allocating input parameters.");

        if (!inputData.containsKey("lat")) throw new RuntimeException("Error: Missing input parameter 'lat'");
        if (!inputData.containsKey("lon")) throw new RuntimeException("Error: Missing input parameter 'lon'");
        if (!inputData.containsKey("name")) throw new RuntimeException("Error: Missing input parameter 'name'");

        String lat = ((LiteralStringBinding) inputData.get("lat").get(0)).getPayload();
        String lon = ((LiteralStringBinding) inputData.get("lon").get(0)).getPayload();
        String name = ((LiteralStringBinding) inputData.get("name").get(0)).getPayload();

        Map<String, IData> result = new HashMap<String, IData>();

        File shapefile;
        try {
            shapefile = WatersService.getGeometry(lon, lat, name);
        } catch (Exception ex) {
            log.error("Error getting geometry from WATERS", ex);
            throw new RuntimeException("Error getting geometry from WATERS");
        }

        String geoServerURL = AppConstant.WFS_ENDPOINT.toString();
        String geoServerWorkspace = "waters";
        String geoServerLayer = shapefile.getName().replace(".shp", "");
        try {
            ManageGeoserverWorkspace mws = new ManageGeoserverWorkspace(geoServerURL);
            String declaredCRS = "EPSG:4269";
            mws.createDataStore(shapefile.getAbsolutePath(), geoServerLayer, geoServerWorkspace, declaredCRS, declaredCRS);
        } catch (Exception ex) {
            log.error("Error creating datastore in GeoServer for WATERS geometry", ex);
            throw new RuntimeException("Error creating datastore in GeoServer for WATERS geometry");
        }
        
        result.put("layer-name", new LiteralStringBinding(geoServerWorkspace + ":" + geoServerLayer));
        return result;
    }

    @Override
    public BigInteger getMaxOccurs(String identifier) {
        if ("lat".equals(identifier)) {
            return BigInteger.valueOf(1);
        }
        if ("lon".equals(identifier)) {
            return BigInteger.valueOf(1);
        }
        if ("name".equals(identifier)) {
            return BigInteger.valueOf(1);
        }
        return super.getMaxOccurs(identifier);
    }

    @Override
    public BigInteger getMinOccurs(String identifier) {
        if ("lat".equals(identifier)) {
            return BigInteger.valueOf(1);
        }
        if ("lon".equals(identifier)) {
            return BigInteger.valueOf(1);
        }
        if ("name".equals(identifier)) {
            return BigInteger.valueOf(1);
        }
        return super.getMinOccurs(identifier);
    }

    @Override
    public List<String> getInputIdentifiers() {
        List<String> result = new ArrayList<String>();
        result.add("lat");
        result.add("lon");
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
        if ("lat".equals(id)) {
            return LiteralStringBinding.class;
        }
        if ("lon".equals(id)) {
            return LiteralStringBinding.class;
        }
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
