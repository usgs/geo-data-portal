package gov.usgs.cida.gdp.wps.algorithm.discovery;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import gov.usgs.cida.gdp.dataaccess.helper.OpendapServerHelper;
import gov.usgs.cida.gdp.utilities.bean.XmlResponse;
import gov.usgs.cida.gdp.wps.cache.ResponseCache;
import gov.usgs.cida.gdp.wps.cache.ResponseCache.CacheIdentifier;
import static gov.usgs.cida.gdp.wps.cache.ResponseCache.CacheIdentifier.CacheType.*;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.n52.wps.algorithm.annotation.Algorithm;
import org.n52.wps.algorithm.annotation.Execute;
import org.n52.wps.algorithm.annotation.LiteralDataInput;
import org.n52.wps.algorithm.annotation.LiteralDataOutput;
import org.n52.wps.server.AbstractAnnotatedAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author isuftin
 */
@Algorithm(version="1.0.0")
public class ListOpendapGrids extends AbstractAnnotatedAlgorithm {

    private static final Logger LOGGER = LoggerFactory.getLogger(ListOpendapGrids.class);
    
    private static final String PARAM_CATALOG_URL = "catalog-url";
    private static final String PARAM_USE_CACHE = "allow-cached-response";
    private static final String PARAM_RESULT = "result";
    
    private String catalogURL;
    private boolean useCache = false; // optional parameter, set default...
    private String result;
    
    @LiteralDataInput(identifier=PARAM_CATALOG_URL)
    public void setCatalogURL(String catalogURL) {
        this.catalogURL = catalogURL;
    }
    
    @LiteralDataInput(identifier=PARAM_USE_CACHE, minOccurs=0, defaultValue="false")
    public void setUseCache(boolean useCache) {
        this.useCache = useCache;
    }
    
    @LiteralDataOutput(identifier=PARAM_RESULT)
    public String getResult() {
        return result;
    }

    @Execute
    public void process() {
        Preconditions.checkArgument(StringUtils.isNotBlank(catalogURL), "Invalid " + PARAM_CATALOG_URL);
        
        CacheIdentifier cacheIdentifier = new ResponseCache.CacheIdentifier(
                catalogURL, DATA_TYPE, null);

        StringBuilder response = new StringBuilder();
        List<XmlResponse> xmlResponseList;
        try {
            if (useCache && ResponseCache.hasCachedResponse(cacheIdentifier)) {
                xmlResponseList = Lists.newLinkedList();
                XmlResponse readXmlFromCache = ResponseCache.readXmlFromCache(cacheIdentifier);
                xmlResponseList.add(readXmlFromCache);
            }
            else {
                xmlResponseList = OpendapServerHelper.getGridBeanListFromServer(catalogURL);
                if (useCache) {
                    ResponseCache.writeXmlToCache(cacheIdentifier, xmlResponseList.get(0));
                }
            }
            for (XmlResponse xmlResponse : xmlResponseList) {
                response.append(xmlResponse.toXML()).append("\n");
            }
            result = response.toString();
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
            addError(ex.getMessage());
            throw new RuntimeException("An error has occured while processing response. Error: " + ex.getMessage(),ex);
        }
    }
}
