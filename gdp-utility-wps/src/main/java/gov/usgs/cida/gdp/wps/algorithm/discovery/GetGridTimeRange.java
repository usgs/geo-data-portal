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
    private static final Logger log = LoggerFactory.getLogger(GetGridTimeRange.class);
    private static final String PARAM_CATALOG_URL = "catalog-url";
    private static final String PARAM_GRID = "grid";
    private static final String PARAM_RESULT = "result";
    
    @Override
    public Map<String, IData> run(Map<String, List<IData>> inputData) {
        

        String datasetUrl;
        
        if (inputData == null)  {
            throw new RuntimeException("Error while allocating input parameters: Unable to find input parameters");
        }
        if (!inputData.containsKey(PARAM_CATALOG_URL))  {
            throw new RuntimeException("Error while allocating input parameters: missing required parameter: '"+PARAM_CATALOG_URL+"'");
        }
        if (!inputData.containsKey(PARAM_GRID)) {
            throw new RuntimeException("Error while allocating input parameters: missing required parameter(s): '"+PARAM_GRID+"'");
        }
        datasetUrl = ((LiteralStringBinding) inputData.get(PARAM_CATALOG_URL).get(0)).getPayload();

        List<String> grid = new ArrayList<String>(inputData.get(PARAM_GRID).size());
        for (int index = 0;index < inputData.get(PARAM_GRID).size();index++) {
            grid.add(((LiteralStringBinding) inputData.get(PARAM_GRID).get(index)).getPayload());
        }
        String gridSelection = grid.get(0);
        Time timeBean = null;
        try {
            timeBean = OpendapServerHelper.getTimeBean(datasetUrl, gridSelection);
        } catch (IOException ex) {
            log.error(ex.getMessage());
            throw new RuntimeException("Error occured while getting time range.  Function halted.",ex);
        } catch (ParseException ex) {
            log.error(ex.getMessage());
            throw new RuntimeException("Error occured while getting time range.  Function halted.",ex);
        }

        Map<String, IData> result = new HashMap<String, IData>(1);
        result.put(PARAM_RESULT, new LiteralStringBinding(timeBean.toXML()));
        return result;
    }

    @Override
    public List<String> getInputIdentifiers() {
        List<String> result = new ArrayList<String>(2);
        result.add(PARAM_CATALOG_URL);
        result.add(PARAM_GRID);
        return result;
    }

    @Override
    public List<String> getOutputIdentifiers() {
        List<String> result = new ArrayList<String>(1);
        result.add(PARAM_GRID);
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
        if (PARAM_CATALOG_URL.equals(identifier)) {
            return BigInteger.valueOf(1);
        }
        if (PARAM_GRID.equals(identifier)) {
            return BigInteger.valueOf(Long.MAX_VALUE);
        }
        return super.getMaxOccurs(identifier);
    }

    @Override
    public BigInteger getMinOccurs(String identifier) {
        if (PARAM_CATALOG_URL.equals(identifier)) {
            return BigInteger.valueOf(1);
        }
        if (PARAM_GRID.equals(identifier)) {
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