package gov.usgs.cida.gdp.wps.cache;

import gov.usgs.cida.gdp.constants.AppConstant;
import gov.usgs.cida.gdp.utilities.bean.Response;
import gov.usgs.cida.gdp.wps.cache.ResponseCache.CacheIdentifier.CacheType;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jordan Walker <jiwalker@usgs.gov>
 */
public class ResponseCache {

    private static final Logger log = LoggerFactory.getLogger(
            ResponseCache.class);

    public static synchronized Response readXmlFromCache(CacheIdentifier ci) {
        File cacheFile = ci.getFile();
        log.debug("Reading from {} cache for {} at file location {}",
                  new Object[] { ci.cacheType.getType(), ci.datasetUri, cacheFile });
        RawXmlResponse xml = new RawXmlResponse(cacheFile);
        return xml;
    }

    public static synchronized boolean hasCachedResponse(CacheIdentifier ci) {
        File cacheFile = ci.getFile();
        return cacheFile.exists();
    }

    public static synchronized void writeXmlToCache(CacheIdentifier ci,
                                                    Response xml) throws
            IOException {
        File cacheFile = ci.getFile();
        log.debug("Writing to {} cache for {} at file location {}",
                  new Object[] { ci.cacheType.getType(), ci.datasetUri, cacheFile });
        FileUtils.write(cacheFile, xml.toXML());
    }

    public static class CacheIdentifier {

        public enum CacheType {

            DATA_TYPE,
            TIME_RANGE;

            public String getCacheDir() {
                String cacheDir = AppConstant.CACHE_LOCATION.getValue();
                switch (this) {
                    case DATA_TYPE:
                        return cacheDir + File.separator + "data_types";
                    case TIME_RANGE:
                        return cacheDir + File.separator + "time_ranges";
                    default:
                        return cacheDir;
                }
            }

            public String getType() {
                switch (this) {
                    case DATA_TYPE:
                        return "data type";
                    case TIME_RANGE:
                        return "time range";
                    default:
                        return "";
                }
            }
        }
        
        public String datasetUri;
        public CacheType cacheType;
        public String grid;
        
        public CacheIdentifier(String datasetUri, CacheType cacheType, String grid) {
            this.datasetUri = datasetUri;
            this.cacheType = cacheType;
            this.grid = grid;
        }
        
        public File getFile() {
            String replaced = datasetUri.replaceAll("[\\:/]", "-");
            if (grid != null && !grid.isEmpty()) {
                replaced += "-" + grid;
            }
            return new File(cacheType.getCacheDir() + File.separator + replaced + ".cache");
        }
    }
}
