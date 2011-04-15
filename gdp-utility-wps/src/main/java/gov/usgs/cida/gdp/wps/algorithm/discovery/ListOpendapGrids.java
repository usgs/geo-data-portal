package gov.usgs.cida.gdp.wps.algorithm.discovery;

import gov.usgs.cida.gdp.dataaccess.helper.OpendapServerHelper;
import gov.usgs.cida.gdp.utilities.bean.XmlResponse;
import java.io.IOException;
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
public class ListOpendapGrids extends AbstractSelfDescribingAlgorithm {

    Logger log = LoggerFactory.getLogger(ListOpendapGrids.class);
    private List<String> errors = new ArrayList<String>();

    @Override
    public Map<String, IData> run(Map<String, List<IData>> inputData) {
        Map<String, IData> result = new HashMap<String, IData>();

        String catalogUrl = ((LiteralStringBinding) inputData.get("catalog-url").get(0)).getPayload();

        // Get the optional service type
        /*String serviceType = ((LiteralStringBinding) inputData.get("service-type").get(0)).getPayload();
        if ("cdmremote".equals(serviceType) && !catalogUrl.contains("cdmremote:")) catalogUrl = "cdmremote:" + catalogUrl;*/
        
        StringBuilder response = new StringBuilder();
        List<XmlResponse> xmlResponseList = null;
        try {
            xmlResponseList = OpendapServerHelper.getGridBeanListFromServer(catalogUrl);
        } catch (IllegalArgumentException ex) {
            getErrors().add(ex.getMessage());
            throw new RuntimeException("An error has occured while processing response. Error: " + ex.getMessage());
        } catch (IOException ex) {
            getErrors().add(ex.getMessage());
            throw new RuntimeException("An error has occured while processing response. Error: " + ex.getMessage());
        }

        for (XmlResponse xmlResponse : xmlResponseList) response.append(xmlResponse.toXML()).append("\n");

        result.put("result", new LiteralStringBinding(response.toString()));
        return result;
    }

    @Override
    public List<String> getInputIdentifiers() {
        List<String> result = new ArrayList<String>();
        result.add("service-type");
        result.add("catalog-url");
        return result;
    }

    @Override
    public Class getInputDataType(String id) {
        if (id.equalsIgnoreCase("service-type")) {
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
