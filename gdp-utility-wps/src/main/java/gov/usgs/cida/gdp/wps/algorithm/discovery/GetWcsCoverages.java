package gov.usgs.cida.gdp.wps.algorithm.discovery;

import gov.usgs.cida.gdp.dataaccess.WCSCoverageInfoHelper;
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
 * @author razoerb
 */
public class GetWcsCoverages extends AbstractSelfDescribingAlgorithm {

    Logger log = LoggerFactory.getLogger(GetWcsCoverages.class);

    @Override
    public Map<String, IData> run(Map<String, List<IData>> inputData) {
        Map<String, IData> result = new HashMap<String, IData>();

        if (!inputData.containsKey("wcs-url"))
            throw new RuntimeException("Missing required parameter: wcs-url");

        String wcsURL = ((LiteralStringBinding) inputData.get("wcs-url").get(0)).getPayload();

        String xml;
        try {
            xml = WCSCoverageInfoHelper.getWcsDescribeCoverages(wcsURL);
        } catch (IOException ex) {
            log.error("An error occurred in algorithm GetWcsCoverages", ex);
            throw new RuntimeException("An error occured while processing request: " + ex.getMessage());
        }

        result.put("result", new LiteralStringBinding(xml));
        return result;
    }

    @Override
    public List<String> getInputIdentifiers() {
        List<String> result = new ArrayList<String>();
        result.add("wcs-url");
        return result;
    }

    @Override
    public Class getInputDataType(String id) {
        if (id.equalsIgnoreCase("wcs-url")) {
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
        if ("wcs-url".equals(identifier)) {
            return BigInteger.valueOf(1);
        }
        return super.getMaxOccurs(identifier);
    }

    @Override
    public BigInteger getMinOccurs(String identifier) {
        if ("wcs-url".equals(identifier)) {
            return BigInteger.valueOf(1);
        }
        return super.getMaxOccurs(identifier);
    }
}
