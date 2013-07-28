package gov.usgs.cida.gdp.dataaccess.cache;

import gov.usgs.cida.gdp.constants.AppConstant;
import gov.usgs.cida.gdp.dataaccess.cache.ResponseCache.CacheIdentifier.CacheType;
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

	private static final Logger log = LoggerFactory.getLogger(ResponseCache.class);

	public static synchronized boolean hasCachedResponse(CacheIdentifier ci) {
		File cacheFile;
		try {
			cacheFile = ci.getFile();
		} catch (IOException ex) {
			return false;
		}
		return cacheFile.exists();
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

		public File getFile() throws IOException {
			String replaced = datasetUri.replaceAll("[\\:/]", "-");
			if (grid != null && !grid.isEmpty()) {
				replaced += "-" + grid;
			}
			File cacheDir = new File(cacheType.getCacheDir());
			if (!cacheDir.exists()) {
				FileUtils.forceMkdir(new File(cacheType.getCacheDir()));
			}

			return new File(cacheDir, replaced + ".cache");
		}
	}
}