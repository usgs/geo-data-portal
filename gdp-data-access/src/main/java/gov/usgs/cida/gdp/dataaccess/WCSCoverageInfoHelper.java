/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usgs.cida.gdp.dataaccess;

import gov.usgs.cida.gdp.dataaccess.bean.WCSCoverageInfo;
import gov.usgs.cida.gdp.utilities.HTTPUtils;
import java.io.InputStream;
import java.net.URL;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.geometry.BoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.cs.AxisDirection;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

/**
 *
 * @author admin
 */
public class WCSCoverageInfoHelper {
	private static org.slf4j.Logger log = LoggerFactory.getLogger(WCSCoverageInfoHelper.class);
    private static final int MAX_COVERAGE_SIZE = 64 << 20; // 64 MB

    public static WCSCoverageInfo calculateWCSCoverageInfo(
                String wfsURL, String dataStore, String gridLowerCorner, String gridUpperCorner,
                String crs, String gridOffsets, String dataTypeString) throws Exception {

        // Get feature bounds

    	InputStream is = HTTPUtils.sendPacket(new URL(wfsURL + "?request=GetCapabilities"), "GET");
    	
    	DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document document = documentBuilder.parse(is);
		
		XPathFactory xpathFactory = XPathFactory.newInstance();
		XPath xpath = xpathFactory.newXPath();
		xpath.setNamespaceContext(new GetObservationNamespaceContext());

        String featureName;
        String featureLowerCorner;
        String featureUpperCorner;

        int i = 1;

        do {
            String XPATH_featureName = "/wfs:WFS_Capabilities/wfs:FeatureTypeList/"
                    + "wfs:FeatureType[" + i + "]/wfs:Name/text()";

            String XPATH_featureLowerCorner = "/wfs:WFS_Capabilities/wfs:FeatureTypeList/"
                    + "wfs:FeatureType[" + i + "]/ows:WGS84BoundingBox/ows:LowerCorner/text()";

            String XPATH_featureUpperCorner = "/wfs:WFS_Capabilities/wfs:FeatureTypeList/"
                    + "wfs:FeatureType[" + i + "]/ows:WGS84BoundingBox/ows:UpperCorner/text()";

            XPathExpression nameExpression =  xpath.compile(XPATH_featureName);
            XPathExpression lowerCornerExpression =  xpath.compile(XPATH_featureLowerCorner);
            XPathExpression upperCornerExpression =  xpath.compile(XPATH_featureUpperCorner);

            featureName = nameExpression.evaluate(document);

            // TODO: figure out what exception to throw
            // If we've searched through all the features
            if ("".equals(featureName)) throw new Exception("dataStore not found");

            featureLowerCorner = lowerCornerExpression.evaluate(document);
            featureUpperCorner = upperCornerExpression.evaluate(document);

            i++;
            
        } while (!dataStore.equals(featureName));

        String featureLowerCornerNums[] = featureLowerCorner.split(" ");
		String featureUpperCornerNums[] = featureUpperCorner.split(" ");

        double fX1 = Double.parseDouble(featureLowerCornerNums[0]);
		double fY1 = Double.parseDouble(featureLowerCornerNums[1]);
		double fX2 = Double.parseDouble(featureUpperCornerNums[0]);
		double fY2 = Double.parseDouble(featureUpperCornerNums[1]);

        // Bounds in GetCapabilities are always in WGS84
        CoordinateReferenceSystem featureCRS = CRS.decode("EPSG:4326");

		ReferencedEnvelope featureBounds =
                new ReferencedEnvelope(fX1, fX2, fY1, fY2, featureCRS);
		
		
    	// Get grid bounds
    	
		String gridLowerCornerNums[] = gridLowerCorner.split(" ");
		String gridUpperCornerNums[] = gridUpperCorner.split(" ");
        
		double gX1 = Double.parseDouble(gridLowerCornerNums[0]);
		double gY1 = Double.parseDouble(gridLowerCornerNums[1]);
		double gX2 = Double.parseDouble(gridUpperCornerNums[0]);
		double gY2 = Double.parseDouble(gridUpperCornerNums[1]);

        ReferencedEnvelope gridBounds;
        CoordinateReferenceSystem gridCRS;

        gridCRS = CRS.decode(crs);
        AxisDirection ad0 = gridCRS.getCoordinateSystem().getAxis(0).getDirection();
        AxisDirection ad1 = gridCRS.getCoordinateSystem().getAxis(1).getDirection();
        boolean  swapXYForTransform =
                    (ad0 == AxisDirection.NORTH || ad0 == AxisDirection.SOUTH)
                 && (ad1 == AxisDirection.EAST || ad1 == AxisDirection.WEST);

        gridBounds = swapXYForTransform
                ? new ReferencedEnvelope(gY1, gY2, gX1, gX2, gridCRS)
                : new ReferencedEnvelope(gX1, gX2, gY1, gY2, gridCRS);


        // Make sure grid completely covers feature

        boolean fullyCovers;
        int minResamplingFactor;
        String units, boundingBox;

        ReferencedEnvelope featureBoundsTrans = featureBounds.transform(gridCRS, true);

        // Explicitly cast to BoundingBox because there are
        // ambiguous 'contains' methods
        if (!gridBounds.contains((BoundingBox) featureBoundsTrans)) {
            fullyCovers = false;
        } else {
            fullyCovers = true;
        }


        /////// Estimate size of coverage request /////////

        String gridOffsetNums[] = gridOffsets.split(" ");
        double xOffset = Math.abs(Double.parseDouble(gridOffsetNums[0]));
        double yOffset = Math.abs(Double.parseDouble(gridOffsetNums[1]));

        // Size of data type in bytes
        int dataTypeSize;

        // We can't find the spec for what possible data types exist, so...
        // we have to check, and default to the max size (8) if we come
        // across an unrecognized type
        CoverageMetaData.DataType dataType = CoverageMetaData.findCoverageDataType(dataTypeString);
        if (dataType == CoverageMetaData.UnknownDataType) {
            log.info("Unrecognized wcs data type: " + dataType);
            dataTypeSize = 8;
        } else {
            dataTypeSize = dataType.getSizeBytes();
        }

        double size = (featureBounds.getHeight() / yOffset)
                * (featureBounds.getWidth() / xOffset)
                * dataTypeSize;

        if (size > MAX_COVERAGE_SIZE) {
            float factor = (float) size / MAX_COVERAGE_SIZE;

            minResamplingFactor = (int) Math.round(Math.ceil(factor));
        } else {
            minResamplingFactor = 1; // Coverage size is ok as is
        }

        // TODO, do what we can to figure this out.  I expect the logic
        // below might become quite complicated... The variable 'swapXYForTransform'
        // only shows the need of swap during the transform, but doesn't necessarily
        // flag a need to swap for the BBOX request. This is a function of:
        // 1) service/service-version/vendor/vendor-implementation-version
        // 2) CRS and namespace used to reference it
        // 3) ???
        boolean swapXYForRequest = swapXYForTransform && crs.matches("urn:(?:x-)?ogc(?:-x)?:def:crs:.*");

        units = "blah";
        boundingBox = swapXYForRequest
                ? Double.toString(featureBounds.getMinY()) + ","
                + Double.toString(featureBounds.getMinX()) + ","
                + Double.toString(featureBounds.getMaxY()) + ","
                + Double.toString(featureBounds.getMaxX())
                : Double.toString(featureBounds.getMinX()) + ","
                + Double.toString(featureBounds.getMinY()) + ","
                + Double.toString(featureBounds.getMaxX()) + ","
                + Double.toString(featureBounds.getMaxY());


        WCSCoverageInfo bean = new WCSCoverageInfo(minResamplingFactor,
                fullyCovers, units, boundingBox);

        return bean;
    }
    
    private static class GetObservationNamespaceContext implements NamespaceContext {

		public final static Map<String, String> namespaceMap; 
		static {
			namespaceMap = new HashMap<String, String>();
			namespaceMap.put("", "http://www.opengis.net/wfs");
			namespaceMap.put("wfs", "http://www.opengis.net/wfs");
			namespaceMap.put("ows", "http://www.opengis.net/ows");
			namespaceMap.put("gml", "http://www.opengis.net/gml");
			namespaceMap.put("ogc", "http://www.opengis.net/ogc");
			namespaceMap.put("xlink", "http://www.w3.org/1999/xlink");
		}
		
		@Override
		public String getNamespaceURI(String prefix) {
			if (prefix == null) {
				throw new NullPointerException("prefix is null");
			}
			String namespaceURI = namespaceMap.get(prefix);
			
			return namespaceURI;
		}

		@Override
		public String getPrefix(String namespaceURI) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Iterator<String> getPrefixes(String namespaceURI) {
			throw new UnsupportedOperationException();
		}
	}
}
