package gov.usgs.cida.gdp.wps.algorithm.discovery;

import gov.usgs.cida.client.csw.util.CSWGetServiceURL;
import gov.usgs.cida.client.csw.util.CSWRecord;
import gov.usgs.cida.gdp.constants.AppConstant;
import java.math.BigInteger;
import java.net.URL;
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
public class GetCswRecords extends AbstractSelfDescribingAlgorithm {

    Logger log = LoggerFactory.getLogger(GetCswRecords.class);

    @Override
    public Map<String, IData> run(Map<String, List<IData>> inputData) {
        Map<String, IData> result = new HashMap<String, IData>();

        List<IData> queryInput = inputData.get("query");

        String query;
        if (queryInput != null) query = ((LiteralStringBinding) queryInput.get(0)).getPayload();
        else                    query = null;

        List<CSWRecord> records;
        try {
            URL cswURL = new URL(AppConstant.CSW_ENDPOINT.toString());
            records = CSWGetServiceURL.getServerReference(cswURL, query, "gdp", "gdpgdp");
        } catch (Exception ex) {
            log.error("An error occurred in algorithm GetCswRecords", ex);
            throw new RuntimeException("An error occured while processing request: " + ex.getMessage());
        }

        String xml = "<records>";
        for (CSWRecord record : records) {
            xml += record.toXML();
        }
        xml += "</records>";

        result.put("result", new LiteralStringBinding(xml));
        return result;
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
    public List<String> getInputIdentifiers() {
        List<String> inputs = new ArrayList<String>();
        inputs.add("query");
        
        return inputs;
    }

    @Override
    public Class getInputDataType(String string) {
        return LiteralStringBinding.class;
    }

    @Override
    public BigInteger getMaxOccurs(String identifier) {
        if ("query".equals(identifier)) {
            return BigInteger.valueOf(1);
        }
        return super.getMaxOccurs(identifier);
    }

    @Override
    public BigInteger getMinOccurs(String identifier) {
        if ("query".equals(identifier)) {
            return BigInteger.valueOf(0);
        }
        return super.getMaxOccurs(identifier);
    }
}
