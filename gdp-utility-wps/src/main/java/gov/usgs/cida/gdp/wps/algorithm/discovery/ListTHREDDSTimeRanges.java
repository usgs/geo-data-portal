package gov.usgs.cida.gdp.wps.algorithm.discovery;

import gov.usgs.cida.gdp.dataaccess.helper.THREDDSServerHelper;
import gov.usgs.cida.gdp.utilities.bean.Time;
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
 *
 * @author isuftin
 */
public class ListTHREDDSTimeRanges extends AbstractSelfDescribingAlgorithm {

    Logger log = LoggerFactory.getLogger(ListTHREDDSTimeRanges.class);
    private List<String> errors = new ArrayList<String>();

    @Override
    public Map<String, IData> run(Map<String, List<IData>> inputData) {
        Map<String, IData> result = new HashMap<String, IData>();
        String response = "";
        
        if (!inputData.containsKey("catalog-url")) {
            throw new RuntimeException("Error: missing input parameter 'catalog_url'");
        }

        if (!inputData.containsKey("grid-selection")) {
            throw new RuntimeException("Error: missing input parameter 'grid-selection'");
        }

        String catalogUrl = ((LiteralStringBinding) inputData.get("catalog-url").get(0)).getPayload();
        String gridSelection = ((LiteralStringBinding) inputData.get("grid-selection").get(0)).getPayload();

        // Get the optional service type
        String serviceType = ((LiteralStringBinding) inputData.get("service-type").get(0)).getPayload();
        if ("cdmremote".equals(serviceType) && !catalogUrl.contains("cdmremote:")) catalogUrl = "cdmremote:" + catalogUrl;
        
        Time timeRange = null;
        try {
            timeRange = THREDDSServerHelper.getTimeBean(catalogUrl, gridSelection);
        } catch (Exception ex) {
            throw new RuntimeException("An error has occured while processing response. Error: " + ex.getMessage());
        }

        response = timeRange.toXML();
        
        result.put("result", new LiteralStringBinding(response));
        return result;
    }

    @Override
    public List<String> getInputIdentifiers() {
        List<String> result = new ArrayList<String>();
        result.add("service-type");
        result.add("grid-selection");
        result.add("catalog-url");
        return result;
    }

    @Override
    public Class getInputDataType(String id) {
        if (id.equalsIgnoreCase("service-type")) {
            return LiteralStringBinding.class;
        }
        if (id.equalsIgnoreCase("grid-selection")) {
            return LiteralStringBinding.class;
        }

        if (id.equalsIgnoreCase("catalog-url")) {
            return LiteralStringBinding.class;
        }
        return null;
    }


    @Override
    public List<String> getOutputIdentifiers() {
        List<String> result = new ArrayList<String>();
        result.add("result");
        return result;
    }

    @Override
    public Class getOutputDataType(String id) {
        if (id.equalsIgnoreCase("result")) {
            return LiteralStringBinding.class;
        }
        return null;
    }

    @Override
    public BigInteger getMaxOccurs(String identifier) {
        if ("service-type".equals(identifier)) {
            return BigInteger.valueOf(1);
        }
        if ("grid-selection".equals(identifier)) {
            return BigInteger.valueOf(1);
        }
        if ("catalog-url".equals(identifier)) {
            return BigInteger.valueOf(1);
        }
        return super.getMaxOccurs(identifier);
    }

    @Override
    public BigInteger getMinOccurs(String identifier) {
        if ("service-type".equals(identifier)) {
            return BigInteger.valueOf(0);
        }
        if ("grid-selection".equals(identifier)) {
            return BigInteger.valueOf(1);
        }
        if ("catalog-url".equals(identifier)) {
            return BigInteger.valueOf(1);
        }
        return super.getMaxOccurs(identifier);
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    @Override
    public List<String> getErrors() {
        return errors;
    }
}
