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

    private static final Logger log = LoggerFactory.getLogger(ListOpendapGrids.class);
    private static final String PARAM_CATALOG_URL = "catalog-url";
    private static final String PARAM_RESULT = "result";
    private List<String> errors = new ArrayList<String>();

    @Override
    public Map<String, IData> run(Map<String, List<IData>> inputData) {
        String catalogUrl = ((LiteralStringBinding) inputData.get(PARAM_CATALOG_URL).get(0)).getPayload();

        StringBuilder response = new StringBuilder();
        List<XmlResponse> xmlResponseList = null;
        try {
            xmlResponseList = OpendapServerHelper.getGridBeanListFromServer(catalogUrl);
        } catch (IllegalArgumentException ex) {
            getErrors().add(ex.getMessage());
            throw new RuntimeException("An error has occured while processing response. Error: " + ex.getMessage(),ex);
        } catch (IOException ex) {
            getErrors().add(ex.getMessage());
            throw new RuntimeException("An error has occured while processing response. Error: " + ex.getMessage(),ex);
        }

        for (XmlResponse xmlResponse : xmlResponseList) {
            response.append(xmlResponse.toXML()).append("\n");
        }

        Map<String, IData> result = new HashMap<String, IData>(1);
        result.put(PARAM_RESULT, new LiteralStringBinding(response.toString()));
        return result;
    }

    @Override
    public List<String> getInputIdentifiers() {
        List<String> result = new ArrayList<String>(1);
        result.add(PARAM_CATALOG_URL);
        return result;
    }

    @Override
    public Class getInputDataType(String id) {
        if (id.equalsIgnoreCase(PARAM_CATALOG_URL)) {
            return LiteralStringBinding.class;
        }
        return null;
    }


    @Override
    public List<String> getOutputIdentifiers() {
        List<String> result = new ArrayList<String>(1);
        result.add(PARAM_RESULT);
        return result;
    }

    @Override
    public Class getOutputDataType(String id) {
        if (id.equalsIgnoreCase(PARAM_RESULT)) {
            return LiteralStringBinding.class;
        }
        return null;
    }

    @Override
    public BigInteger getMaxOccurs(String identifier) {
        if (PARAM_CATALOG_URL.equals(identifier)) {
            return BigInteger.valueOf(1);
        }
        return super.getMaxOccurs(identifier);
    }

    @Override
    public BigInteger getMinOccurs(String identifier) {
        if (PARAM_CATALOG_URL.equals(identifier)) {
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
