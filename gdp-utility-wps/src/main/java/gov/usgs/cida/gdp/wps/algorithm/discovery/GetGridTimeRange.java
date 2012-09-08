package gov.usgs.cida.gdp.wps.algorithm.discovery;

import com.google.common.base.Preconditions;
import gov.usgs.cida.gdp.dataaccess.helper.OpendapServerHelper;
import gov.usgs.cida.gdp.utilities.bean.XmlResponse;
import gov.usgs.cida.gdp.wps.cache.ResponseCache;
import gov.usgs.cida.gdp.wps.cache.ResponseCache.CacheIdentifier;
import gov.usgs.cida.n52.wps.algorithm.AbstractAnnotatedAlgorithm;
import gov.usgs.cida.n52.wps.algorithm.annotation.Algorithm;
import gov.usgs.cida.n52.wps.algorithm.annotation.LiteralDataInput;
import gov.usgs.cida.n52.wps.algorithm.annotation.LiteralDataOutput;
import gov.usgs.cida.n52.wps.algorithm.annotation.Process;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author isuftin
 */
@Algorithm(version="1.0.0")
public class GetGridTimeRange extends AbstractAnnotatedAlgorithm {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(GetGridTimeRange.class);
    
    private static final String PARAM_CATALOG_URL = "catalog-url";
    private static final String PARAM_GRID = "grid";
    private static final String PARAM_USE_CACHE = "allow-cached-response";
    private static final String PARAM_RESULT = "result";
    
    private String catalogURL;
    private String grid;
    private boolean useCache = false;  // optional arguemnt, set default value
    private String result;
    
    @LiteralDataInput(identifier=PARAM_CATALOG_URL)
    public void setCatalogURL(String catalogURL) {
        this.catalogURL = catalogURL;
    }
    
    @LiteralDataInput(identifier=PARAM_GRID)
    public void setGrids(String grid) {
        this.grid = grid;
    }
    
    @LiteralDataInput(identifier=PARAM_USE_CACHE, minOccurs=0, defaultValue="false")
    public void setUseCache(boolean useCache) {
        this.useCache = useCache;
    }
    
    @LiteralDataOutput(identifier=PARAM_RESULT)
    public String getResult() {
        return result;
    }
    
    @Process
    public void process() {
        Preconditions.checkArgument(StringUtils.isNotBlank(catalogURL), "Invalid " + PARAM_CATALOG_URL);
        Preconditions.checkArgument(StringUtils.isNotBlank(grid), "Invalid " + PARAM_GRID);
        
        XmlResponse xmlResponse = null;
        CacheIdentifier cacheIdentifier = new CacheIdentifier(
                catalogURL,
                CacheIdentifier.CacheType.TIME_RANGE,
                grid);
        try {
            if (useCache && ResponseCache.hasCachedResponse(cacheIdentifier)) {
                xmlResponse = ResponseCache.readXmlFromCache(cacheIdentifier);
            }
            else {
                xmlResponse = OpendapServerHelper.getTimeBean(catalogURL, grid);
                if (useCache) {
                    ResponseCache.writeXmlToCache(cacheIdentifier, xmlResponse);
                }
            }
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage());
            addError(ex.getMessage());
            throw new RuntimeException("Error occured while getting time range.  Function halted.",ex);
        }
        result = xmlResponse.toXML();
    }
}