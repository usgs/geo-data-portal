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

    private static final Logger log = LoggerFactory.getLogger(GetWcsCoverages.class);
    private static final String PARAM_WCS_URL = "wcs-url";
    private static final String PARAM_RESULT = "result";
    @Override
    public Map<String, IData> run(Map<String, List<IData>> inputData) {
        if (!inputData.containsKey(PARAM_WCS_URL)){
            throw new RuntimeException("Missing required parameter: " + PARAM_WCS_URL);
        }

        String wcsURL = ((LiteralStringBinding) inputData.get(PARAM_WCS_URL).get(0)).getPayload();

        String xml;
        try {
            xml = WCSCoverageInfoHelper.getWcsDescribeCoverages(wcsURL);
        } catch (IOException ex) {
            throw new RuntimeException("An error occured while processing request: " + ex.getMessage(),ex);
        }

        Map<String, IData> result = new HashMap<String, IData>(1);
        result.put(PARAM_RESULT, new LiteralStringBinding(xml));
        return result;
    }

    @Override
    public List<String> getInputIdentifiers() {
        List<String> result = new ArrayList<String>(1);
        result.add(PARAM_WCS_URL);
        return result;
    }

    @Override
    public Class getInputDataType(String id) {
        if (id.equalsIgnoreCase(PARAM_WCS_URL)) {
            return LiteralStringBinding.class;
        }
        return null;
    }


    @Override
    public List<String> getOutputIdentifiers() {
        List<String> result = new ArrayList<String>();
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
        if (PARAM_WCS_URL.equals(identifier)) {
            return BigInteger.valueOf(1);
        }
        return super.getMaxOccurs(identifier);
    }

    @Override
    public BigInteger getMinOccurs(String identifier) {
        if (PARAM_WCS_URL.equals(identifier)) {
            return BigInteger.valueOf(1);
        }
        return super.getMaxOccurs(identifier);
    }
}
