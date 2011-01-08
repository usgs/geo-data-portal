package gov.usgs.cida.gdp.wps.algorithm.discovery;

import gov.usgs.cida.gdp.dataaccess.WCSCoverageInfoHelper;
import gov.usgs.cida.gdp.dataaccess.bean.WCSCoverageInfo;

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
 * @author isuftin
 */
public class CalculateWCSCoverageInfo extends AbstractSelfDescribingAlgorithm {
    Logger log = LoggerFactory.getLogger(CalculateWCSCoverageInfo.class);

    @Override
    public Map<String, IData> run(Map<String, List<IData>> inputData) {
        Map<String, IData> result = new HashMap<String, IData>();

        String dataStore, crs, lowerCorner, upperCorner, gridOffsets, dataTypeString, wfsURL;

        if (inputData == null)  throw new RuntimeException("Error while allocating input parameters: Unable to find input parameters");
        if (!inputData.containsKey("crs"))  throw new RuntimeException("Error while allocating input parameters: missing required parameter: 'crs'");
        if (!inputData.containsKey("lower-corner")) throw new RuntimeException("Error while allocating input parameters: missing required parameter: 'lower-corner'");
        if (!inputData.containsKey("upper-corner")) throw new RuntimeException("Error while allocating input parameters: missing required parameter: 'upper-corner'");
        if (!inputData.containsKey("grid-offsets")) throw new RuntimeException("Error while allocating input parameters: missing required parameter: 'grid-offsets'");
        if (!inputData.containsKey("data-type")) throw new RuntimeException("Error while allocating input parameters: missing required parameter: 'data-type'");
        if (!inputData.containsKey("wfs-url")) throw new RuntimeException("Error while allocating input parameters: missing required parameter: 'wfs-url'");
        if (!inputData.containsKey("datastore")) throw new RuntimeException("Error while allocating input parameters: missing required parameter: 'datastore'");

        dataStore = ((LiteralStringBinding) inputData.get("datastore").get(0)).getPayload();
        crs = ((LiteralStringBinding) inputData.get("crs").get(0)).getPayload();
        lowerCorner = ((LiteralStringBinding) inputData.get("lower-corner").get(0)).getPayload();
        upperCorner = ((LiteralStringBinding) inputData.get("upper-corner").get(0)).getPayload();
        gridOffsets = ((LiteralStringBinding) inputData.get("grid-offsets").get(0)).getPayload();
        dataTypeString = ((LiteralStringBinding) inputData.get("data-type").get(0)).getPayload();
        wfsURL = ((LiteralStringBinding) inputData.get("wfs-url").get(0)).getPayload();

        if ("".equals(crs) ||
                "".equals(lowerCorner) ||
                "".equals(upperCorner) ||
                "".equals(gridOffsets) ||
                "".equals(dataTypeString) ||
                "".equals(wfsURL)) {
            throw new RuntimeException("Error while allocating input parameters: All input parameters must contain data.");
        }

        WCSCoverageInfo wcsCoverageInfoBean = null;
        try {
        	wcsCoverageInfoBean = WCSCoverageInfoHelper.calculateWCSCoverageInfo(wfsURL, dataStore,
                    lowerCorner, upperCorner, crs, gridOffsets, dataTypeString);
        } catch (Exception e) {
        	System.out.println(e.getMessage());
        }

        result.put("result", new LiteralStringBinding(wcsCoverageInfoBean.toXML()));
        return result;
    }

    @Override
    public List<String> getInputIdentifiers() {
        List<String> result = new ArrayList<String>();
        result.add("wfs-url");
        result.add("datastore");
        result.add("lat");
        result.add("lon");
        result.add("crs");
        result.add("lower-corner");
        result.add("upper-corner");
        result.add("grid-offsets");
        result.add("data-type");
        return result;
    }


    @Override
    public BigInteger getMaxOccurs(String identifier) {
        return BigInteger.valueOf(1);
    }

    @Override
    public BigInteger getMinOccurs(String identifier) {
        if ("datastore".equals(identifier)) {
            return BigInteger.valueOf(0);
        }
        if ("lat".equals(identifier)) {
            return BigInteger.valueOf(0);
        }
        if ("lon".equals(identifier)) {
            return BigInteger.valueOf(0);
        }
        return super.getMaxOccurs(identifier);
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
}

/**
 Datastore Example
<?xml version="1.0" encoding="UTF-8"?>
<wps:Execute service="WPS" version="1.0.0" xmlns:wps="http://www.opengis.net/wps/1.0.0" xmlns:ows="http://www.opengis.net/ows/1.1" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.opengis.net/wps/1.0.0 http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd">
    <ows:Identifier>gov.usgs.cida.gdp.wps.algorithm.discovery.CalculateWCSCoverageInfo</ows:Identifier>
    <wps:DataInputs>
        <wps:Input>
            <ows:Identifier>wfs-url</ows:Identifier>
            <wps:Data>
                <wps:LiteralData>http://localhost:8081/geoserver/wfs</wps:LiteralData>
            </wps:Data>
        </wps:Input>
        <wps:Input>
            <ows:Identifier>datastore</ows:Identifier>
            <wps:Data>
                <wps:LiteralData>sample:demo_HUCs</wps:LiteralData>
            </wps:Data>
        </wps:Input>
        <wps:Input>
            <ows:Identifier>crs</ows:Identifier>
            <wps:Data>
                <wps:LiteralData>urn:ogc:def:crs:EPSG::4269</wps:LiteralData>
            </wps:Data>
        </wps:Input>
        <wps:Input>
            <ows:Identifier>lower-corner</ows:Identifier>
            <wps:Data>
                <wps:LiteralData>-125.100000000078 14.499999997758103</wps:LiteralData>
            </wps:Data>
        </wps:Input>
        <wps:Input>
            <ows:Identifier>upper-corner</ows:Identifier>
            <wps:Data>
                <wps:LiteralData>-65.998333287103392 50.001666666814096</wps:LiteralData>
            </wps:Data>
        </wps:Input>
        <wps:Input>
            <ows:Identifier>grid-offsets</ows:Identifier>
            <wps:Data>
                <wps:LiteralData>0.00027777777799542502 -0.00027777777779647273</wps:LiteralData>
            </wps:Data>
        </wps:Input>
        <wps:Input>
            <ows:Identifier>data-type</ows:Identifier>
            <wps:Data>
                <wps:LiteralData>Float32</wps:LiteralData>
            </wps:Data>
        </wps:Input>
    </wps:DataInputs>
    <wps:ResponseForm>
        <wps:ResponseDocument>
            <wps:Output>
                <ows:Identifier>result</ows:Identifier>
            </wps:Output>
        </wps:ResponseDocument>
    </wps:ResponseForm>
</wps:Execute>
  *
<?xml version="1.0" encoding="UTF-8"?>
<ns:ExecuteResponse xmlns:ns="http://www.opengis.net/wps/1.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.opengis.net/wps/1.0.0 http://schemas.opengis.net/wps/1.0.0/wpsExecute_response.xsd" serviceInstance="http://localhost:8080/wps/WebProcessingService?REQUEST=GetCapabilities&amp;SERVICE=WPS" xml:lang="en-US" service="WPS" version="1.0.0">
    <ns:Process ns:processVersion="1.0.0">
        <ns1:Identifier xmlns:ns1="http://www.opengis.net/ows/1.1">gov.usgs.cida.gdp.wps.algorithm.discovery.CalculateWCSCoverageInfo</ns1:Identifier>
        <ns1:Title xmlns:ns1="http://www.opengis.net/ows/1.1">gov.usgs.cida.gdp.wps.algorithm.discovery.CalculateWCSCoverageInfo</ns1:Title>
    </ns:Process>
    <ns:Status creationTime="2010-10-12T09:06:00.621-05:00">
        <ns:ProcessSucceeded>The service succesfully processed the request.</ns:ProcessSucceeded>
    </ns:Status>
    <ns:ProcessOutputs>
        <ns:Output>
            <ns1:Identifier xmlns:ns1="http://www.opengis.net/ows/1.1">result</ns1:Identifier>
            <ns1:Title xmlns:ns1="http://www.opengis.net/ows/1.1">result</ns1:Title>
            <ns:Data>
                <ns:LiteralData>
                    <WCSCoverageInfo>
                        <minResamplingFactor>11</minResamplingFactor>
                        <fullyCovers>true</fullyCovers>
                        <units>blah</units>
                        <boundingBox>30.225,-89.172,34.749,-86.134</boundingBox>
                    </WCSCoverageInfo>
                </ns:LiteralData>
            </ns:Data>
        </ns:Output>
    </ns:ProcessOutputs>
</ns:ExecuteResponse>
 *
 */