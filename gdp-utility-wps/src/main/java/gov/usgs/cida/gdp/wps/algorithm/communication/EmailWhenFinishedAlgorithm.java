package gov.usgs.cida.gdp.wps.algorithm.communication;

import gov.usgs.cida.gdp.wps.servlet.CheckProcessCompletion;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.n52.wps.server.AbstractSelfDescribingAlgorithm;

import org.slf4j.LoggerFactory;

/**
 * Servlet implementation class EmailWhenFinishedServlet
 */
public class EmailWhenFinishedAlgorithm extends AbstractSelfDescribingAlgorithm {

    static org.slf4j.Logger log = LoggerFactory.getLogger(EmailWhenFinishedAlgorithm.class);
    private static final long serialVersionUID = 1L;

	@Override
    public Map<String, IData> run(Map<String, List<IData>> inputData) {

		if (inputData == null) throw new RuntimeException("Error while allocating input parameters.");

        if (!inputData.containsKey("wps-checkpoint")) throw new RuntimeException("Error: Missing input parameter 'wps-checkpoint'");
		if (!inputData.containsKey("email")) throw new RuntimeException("Error: Missing input parameter 'email'");

		List<IData> dataList = inputData.get("wps-checkpoint");
		String wpsCheckPoint = ((LiteralStringBinding)dataList.get(0)).getPayload();

		dataList = inputData.get("email");
		String emailAddr = ((LiteralStringBinding)dataList.get(0)).getPayload();

		try {
			CheckProcessCompletion processChecker = CheckProcessCompletion.getInstance();
			processChecker.addProcessToCheck(wpsCheckPoint, emailAddr);
		}
		catch (Exception e) {
			log.error("Unable to add process completion check");
			throw new RuntimeException("Error: Unable to add process check timer");
		}

		Map<String, IData> result = new HashMap<String, IData>();
		result.put("result", new LiteralStringBinding("OK: when " + wpsCheckPoint + " is complete, an email will be sent to  '" + emailAddr + "'"));

		return result;
	}

	@Override
	public List<String> getInputIdentifiers() {
		List<String> result = new ArrayList<String>();
        result.add("wps-checkpoint");
        result.add("email");
        return result;
	}

	@Override
	public List<String> getOutputIdentifiers() {
		List<String> result = new ArrayList<String>();
        result.add("result");
        return result;
	}

	@Override
	public Class getInputDataType(String id) {
		if ("wps-checkpoint".equals(id)) {
			return LiteralStringBinding.class;
		}
		if ("email".equals(id)) {
			return LiteralStringBinding.class;
		}
        return null;
	}

	@Override
	public Class getOutputDataType(String id) {
		if ("result".equals(id)) {
			return LiteralStringBinding.class;
		}
		return null;
	}

	@Override
	public BigInteger getMaxOccurs(String identifier) {
		if ("wps-checkpoint".equals(identifier)) {
			return BigInteger.valueOf(1);
		}
		if ("email".equals(identifier)) {
			return BigInteger.valueOf(1);
		}
        return super.getMaxOccurs(identifier);
	}

	@Override
	public BigInteger getMinOccurs(String identifier) {
		if ("wps-checkpoint".equals(identifier)) {
			return BigInteger.valueOf(1);
		}
		if ("email".equals(identifier)) {
			return BigInteger.valueOf(1);
		}
        return super.getMaxOccurs(identifier);
	}

}