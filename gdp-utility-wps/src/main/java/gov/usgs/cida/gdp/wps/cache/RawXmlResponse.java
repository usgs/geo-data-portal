package gov.usgs.cida.gdp.wps.cache;

import gov.usgs.cida.gdp.utilities.bean.XmlResponse;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jordan Walker <jiwalker@usgs.gov>
 */
public class RawXmlResponse extends XmlResponse {

    private static final Logger log = LoggerFactory.getLogger(ResponseCache.class);
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
}
