package gov.usgs.cida.gdp.dataaccess.cache;

import gov.usgs.cida.gdp.dataaccess.cache.ResponseCache;
import gov.usgs.cida.gdp.dataaccess.bean.Response;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jordan Walker <jiwalker@usgs.gov>
 */
public class RawXmlResponse extends Response {

    private static final Logger log = LoggerFactory.getLogger(ResponseCache.class);
	private static final long serialVersionUID = 23423L;
    private File file = null;

    public RawXmlResponse(File location) {
        file = location;
    }

    @Override
    public String toXML() {
        BufferedReader buf = null;
        StringBuilder output = new StringBuilder();
        try {
            buf = new BufferedReader(new FileReader(file));
            String line = null;
            while ((line = buf.readLine()) != null) {
                output.append(line);
            }
        }
        catch (IOException ex) {
            log.error("Problem reading cache file: {}", file, ex);
        }
        finally {
            IOUtils.closeQuietly(buf);
        }
        return output.toString();
    }

	public static Response buildFromCache(ResponseCache.CacheIdentifier ci) {
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
		/**
	 *
	 * @param ci
	 * @return
	 */
	@Override
	public boolean writeToCache(ResponseCache.CacheIdentifier ci) {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
