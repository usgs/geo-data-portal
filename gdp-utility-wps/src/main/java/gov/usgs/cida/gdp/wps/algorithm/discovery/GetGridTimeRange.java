package gov.usgs.cida.gdp.wps.algorithm.discovery;

import gov.usgs.cida.gdp.dataaccess.helper.OpendapServerHelper;
import gov.usgs.cida.gdp.utilities.bean.Time;
import java.io.IOException;
import java.math.BigInteger;
import java.text.ParseException;
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
 *
 * @author isuftin
 */
public class GetGridTimeRange extends AbstractSelfDescribingAlgorithm  {
    Logger log = LoggerFactory.getLogger(GetGridTimeRange.class);

    @Override
    public Map<String, IData> run(Map<String, List<IData>> inputData) {
        Map<String, IData> result = new HashMap<String, IData>();

        String datasetUrl;
        List<String> grid = new ArrayList();
        if (inputData == null)  throw new RuntimeException("Error while allocating input parameters: Unable to find input parameters");
        if (!inputData.containsKey("catalog-url"))  throw new RuntimeException("Error while allocating input parameters: missing required parameter: 'catalog-url'");
        if (!inputData.containsKey("grid"))  throw new RuntimeException("Error while allocating input parameters: missing required parameter(s): 'grid'");
        datasetUrl = ((LiteralStringBinding) inputData.get("catalog-url").get(0)).getPayload();

        for (int index = 0;index < inputData.get("grid").size();index++) {
            grid.add(((LiteralStringBinding) inputData.get("grid").get(index)).getPayload());
        }
        String gridSelection = grid.get(0);
        Time timeBean = null;
        try {
            timeBean = OpendapServerHelper.getTimeBean(datasetUrl, gridSelection);
        } catch (IOException ex) {
            log.error(ex.getMessage());
            throw new RuntimeException("Error occured while getting time range.  Function halted.");
        } catch (ParseException ex) {
            log.error(ex.getMessage());
            throw new RuntimeException("Error occured while getting time range.  Function halted.");
        }

        result.put("result", new LiteralStringBinding(timeBean.toXML()));
        return result;
    }

    @Override
    public List<String> getInputIdentifiers() {
        List<String> result = new ArrayList<String>();
        result.add("catalog-url");
        result.add("grid");
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
        return LiteralStringBinding.class;
    }

    @Override
    public Class getOutputDataType(String id) {
        return LiteralStringBinding.class;
    }


    @Override
    public BigInteger getMaxOccurs(String identifier) {
        if ("catalog-url".equals(identifier)) {
            return BigInteger.valueOf(1);
        }
        if ("grid".equals(identifier)) {
            return BigInteger.valueOf(Long.MAX_VALUE);
        }
        return super.getMaxOccurs(identifier);
    }

    @Override
    public BigInteger getMinOccurs(String identifier) {
        if ("catalog-url".equals(identifier)) {
            return BigInteger.valueOf(1);
        }
        if ("grid".equals(identifier)) {
            return BigInteger.valueOf(1);
        }
        return super.getMaxOccurs(identifier);
    }

}

/**
 
<wps:execute service="WPS" version="1.0.0" xmlns:wps="http://www.opengis.net/wps/1.0.0" xmlns:ows="http://www.opengis.net/ows/1.1" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.opengis.net/wps/1.0.0 http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd">
	<ows:identifier>gov.usgs.cida.gdp.wps.algorithm.discovery.GetGridTimeRange</ows:identifier>
	<wps:datainputs>
		<wps:input>
			<ows:identifier>catalog-url</ows:identifier>
			<wps:data>
				<wps:literaldata>cdmremote:http://internal.cida.usgs.gov:80/thredds/cdmremote/gsod/gsod_cleaned.nc</wps:literaldata>
			</wps:data>
		</wps:input>
		<wps:input>
			<ows:identifier>grid</ows:identifier>
			<wps:data>
				<wps:literaldata>max</wps:literaldata>
			</wps:data>
		</wps:input>
	</wps:datainputs>
	<wps:responseform>
		<wps:responsedocument>
			<wps:output>
				<ows:identifier>result</ows:identifier>
			</wps:output>
		</wps:responsedocument>
	</wps:responseform>
</wps:execute>
 */