package gov.usgs.cida.gdp.wps.algorithm.discovery;

import com.google.common.collect.Lists;
import gov.usgs.cida.gdp.dataaccess.helper.OpendapServerHelper;
import gov.usgs.cida.gdp.utilities.bean.XmlResponse;
import gov.usgs.cida.gdp.wps.cache.ResponseCache;
import gov.usgs.cida.gdp.wps.cache.ResponseCache.CacheIdentifier;
import static gov.usgs.cida.gdp.wps.cache.ResponseCache.CacheIdentifier.CacheType.*;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.literal.LiteralBooleanBinding;
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
    private static final String PARAM_USE_CACHE = "allow-cached-response";
    private static final String PARAM_RESULT = "result";
    private List<String> errors = new ArrayList<String>();

    @Override
    public Map<String, IData> run(Map<String, List<IData>> inputData) {
        if (inputData == null)  {
            throw new RuntimeException("Error while allocating input parameters: Unable to find input parameters");
        }
        if (!inputData.containsKey(PARAM_CATALOG_URL))  {
            throw new RuntimeException("Error while allocating input parameters: missing required parameter: '"+PARAM_CATALOG_URL+"'");
        }
        String catalogUrl = ((LiteralStringBinding) inputData.get(PARAM_CATALOG_URL).get(0)).getPayload();
        
        boolean useCache = false; // default to false
        if (inputData.containsKey(PARAM_USE_CACHE)) {
            useCache = ((LiteralBooleanBinding)inputData.get(PARAM_USE_CACHE).get(0)).getPayload().booleanValue();
        }
        CacheIdentifier ci = new ResponseCache.CacheIdentifier(catalogUrl, DATA_TYPE, null);

        StringBuilder response = new StringBuilder();
        List<XmlResponse> xmlResponseList = null;
        try {
            if (useCache && ResponseCache.hasCachedResponse(ci)) {
                xmlResponseList = Lists.newLinkedList();
                XmlResponse readXmlFromCache = ResponseCache.readXmlFromCache(ci);
                xmlResponseList.add(readXmlFromCache);
            }
            else {
                xmlResponseList = OpendapServerHelper.getGridBeanListFromServer(catalogUrl);
                if (useCache) {
                    ResponseCache.writeXmlToCache(ci, xmlResponseList.get(0));
                }
            }
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
        List<String> result = new ArrayList<String>(2);
        result.add(PARAM_CATALOG_URL);
        result.add(PARAM_USE_CACHE);
        return result;
    }

    @Override
    public Class getInputDataType(String id) {
        if (id.equalsIgnoreCase(PARAM_CATALOG_URL)) {
            return LiteralStringBinding.class;
        }
        if (id.equalsIgnoreCase(PARAM_USE_CACHE)) {
            return LiteralBooleanBinding.class;
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
        if (PARAM_USE_CACHE.equals(identifier)) {
            return BigInteger.valueOf(1);
        }
        return super.getMaxOccurs(identifier);
    }

    @Override
    public BigInteger getMinOccurs(String identifier) {
        if (PARAM_CATALOG_URL.equals(identifier)) {
            return BigInteger.valueOf(1);
        }
        if (PARAM_USE_CACHE.equals(identifier)) {
            return BigInteger.valueOf(0);
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
