package gov.usgs.cida.gdp.wps.algorithm.discovery;

import gov.usgs.cida.gdp.dataaccess.WmsHelper;
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
public class GetWmsCapabilities extends AbstractSelfDescribingAlgorithm {

    Logger log = LoggerFactory.getLogger(GetWmsCapabilities.class);

    @Override
    public Map<String, IData> run(Map<String, List<IData>> inputData) {
        Map<String, IData> result = new HashMap<String, IData>();

        if (!inputData.containsKey("wms-url"))
            throw new RuntimeException("Missing required parameter: wms-url");

        String wmsURL = ((LiteralStringBinding) inputData.get("wms-url").get(0)).getPayload();

        String xml;
        try {
            xml = WmsHelper.getCapabilities(wmsURL);
        } catch (IOException ex) {
            log.error("An error occurred in algorithm GetWmsCapabilities", ex);
            throw new RuntimeException("An error occurred while processing request: " + ex.getMessage());
        }

        // If getCaps has a DOCTYPE declaration, 52n wps will fail to parse it,
        // and throw an exception. So, take that declaration out.
        xml = xml.replaceFirst("<!DOCTYPE.*", "");

        result.put("result", new LiteralStringBinding(xml));
        return result;
    }

    @Override
    public List<String> getInputIdentifiers() {
        List<String> result = new ArrayList<String>();
        result.add("wms-url");
        return result;
    }

    @Override
    public Class getInputDataType(String id) {
        if (id.equalsIgnoreCase("wms-url")) {
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
        if ("wms-url".equals(identifier)) {
            return BigInteger.valueOf(1);
        }
        return super.getMaxOccurs(identifier);
    }

    @Override
    public BigInteger getMinOccurs(String identifier) {
        if ("wms-url".equals(identifier)) {
            return BigInteger.valueOf(1);
        }
        return super.getMaxOccurs(identifier);
    }
}
