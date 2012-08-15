package gov.usgs.cida.gdp.wps.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;

/**
 *
 * @author tkunicki
 */
public class WCSDescribeCoverageInspector_1_1_X {

    private final static String ROOT_ELEMENT_NAME = "CoverageDescriptions";

    // server implementations observed returning /1.1 vs /1.1.0"
    private final static String WCS_NAMESPACE_URI_BASE = "http://www.opengis.net/wcs/1.1";
    private final static String WCS_NAMESPACE_PREFIX = "wcs"; // used for xpath below...

    // server implementations observed returning / vs /1.1"
    private final static String OWS_NAMESPACE_URI_BASE = "http://www.opengis.net/ows";
    private final static String OWS_NAMESPACE_PREFIX = "ows"; // used for xpath below...

    // server implementations observed returning / vs /1.1"
    private final static String OWCS_NAMESPACE_URI_BASE = "http://www.opengis.net/wcs/1.1/ows";
    private final static String OWCS_NAMESPACE_PREFIX = "owcs"; // used for xpath below...

//    private final static String COVERAGE_DESCRIPTION_XPATH = "/wcs:CoverageDescriptions/wcs:CoverageDescription[wcs:Identifier='%s']";
    private final static String COVERAGE_DESCRIPTION_XPATH = "/wcs:CoverageDescriptions/wcs:CoverageDescription";
    private final static String IDENTIFIER_XPATH = COVERAGE_DESCRIPTION_XPATH + "/wcs:Identifier";
    private final static String GRID_BASE_CRS_XPATH = COVERAGE_DESCRIPTION_XPATH + "/wcs:Domain/wcs:SpatialDomain/wcs:GridCRS/wcs:GridBaseCRS";
    private final static String GRID_TYPE_XPATH = COVERAGE_DESCRIPTION_XPATH + "/wcs:Domain/wcs:SpatialDomain/wcs:GridCRS/wcs:GridType";
    private final static String GRID_OFFSETS_XPATH = COVERAGE_DESCRIPTION_XPATH + "/wcs:Domain/wcs:SpatialDomain/wcs:GridCRS/wcs:GridOffsets";
    private final static String GRID_ORIGIN_XPATH = COVERAGE_DESCRIPTION_XPATH + "/wcs:Domain/wcs:SpatialDomain/wcs:GridCRS/wcs:GridOrigin";
    private final static String GRID_BOUNDING_BOX_XPATH = COVERAGE_DESCRIPTION_XPATH + "/wcs:Domain/wcs:SpatialDomain/ows:BoundingBox[@crs='%s']";
    private final static String GRID_LOWER_CORNER_XPATH = GRID_BOUNDING_BOX_XPATH + "/ows:LowerCorner";
    private final static String GRID_UPPER_CORNER_XPATH = GRID_BOUNDING_BOX_XPATH + "/ows:UpperCorner";
    private final static String GRID_DATA_TYPE_XPATH = COVERAGE_DESCRIPTION_XPATH + "/wcs:Range/wcs:Field/wcs:Axis/owcs:DataType";
    private final static String GRID_SUPPORTED_FORMATS_XPATH = COVERAGE_DESCRIPTION_XPATH + "/wcs:SupportedFormat";
    private final static String GRID_SUPPORTED_CRS_XPATH = COVERAGE_DESCRIPTION_XPATH + "/wcs:SupportedCRS";

    private final XPathWrapper wrapper;
    private final String wcsIdentifier;

    public WCSDescribeCoverageInspector_1_1_X(Document describeCoverageDocument, String coverageIdentifier) {

        WCSNamespaceContext namespaceContext = new WCSNamespaceContext();

        XPath xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(namespaceContext);
        wrapper = new XPathWrapper(xpath, describeCoverageDocument);

        this.wcsIdentifier = coverageIdentifier;

        try {
            // 1) Validate root element
            String rootElementName = wrapper.textAsString("local-name(/*)");
            if (!rootElementName.equals(ROOT_ELEMENT_NAME)) {
                throw new IllegalStateException("Unexpected root element name in reponse document, expected \"CoverageDescriptions\" encountered \"" + rootElementName + "\"");
            }

            String rootElementNamespace = wrapper.textAsString("namespace-uri(/*)");
            if (rootElementNamespace.startsWith(WCS_NAMESPACE_URI_BASE)) {
                namespaceContext.putNamespaceURI(WCS_NAMESPACE_PREFIX, rootElementNamespace);
            }

            // 3) Verify (and set) OWS Namespace URI by
            String[] namespaceURIs = wrapper.nodeListTextContentAsStringArray("//*/namespace::*");
            for (String namespaceURI : namespaceURIs) {
                if (namespaceURI.startsWith(OWS_NAMESPACE_URI_BASE)) {
                    namespaceContext.putNamespaceURI(OWS_NAMESPACE_PREFIX, namespaceURI);
                }
                if (namespaceURI.startsWith(OWCS_NAMESPACE_URI_BASE)) {
                    namespaceContext.putNamespaceURI(OWCS_NAMESPACE_PREFIX, namespaceURI);
                }
            }

            if (namespaceContext.getNamespaceURI(WCS_NAMESPACE_PREFIX) == null) {
                throw new IllegalStateException("Could not resolve WCS namespace URI in reponse document");
            }
            if (namespaceContext.getNamespaceURI(OWS_NAMESPACE_PREFIX) == null) {
                throw new IllegalStateException("Could not resolve OWS namespace URI in reponse document");
            }
            if (namespaceContext.getNamespaceURI(OWCS_NAMESPACE_PREFIX) == null) {
                // try to use OWS if OWCS is missing, this is a guess...
                namespaceContext.putNamespaceURI(OWCS_NAMESPACE_PREFIX, namespaceContext.getNamespaceURI(OWS_NAMESPACE_PREFIX));
            }
            
            String [] wcsIdentifiers= wrapper.nodeListTextContentAsStringArray(IDENTIFIER_XPATH);
            // Test for expected identifier
            // 1) verify identifier list not less than 1
            // 2) verify identifier list not greater than 1
            // 3) verify identifier name ends with expected identifier
            //    NOTE we use 'ends with' since GeoServer (at least through 2.1.x)
            //    prepends the name space to the identifier in DescribeProcess even
            //    though it is not advertised that way in GetCapabilities
            if (wcsIdentifiers == null || wcsIdentifiers.length == 0) {
                throw new IllegalStateException("Unable to extract coverage identifier from response document");
            } else if (wcsIdentifiers.length > 1) {
                throw new IllegalStateException("More than coverage contained in response document");
            } else if (wcsIdentifiers[0] != null && !wcsIdentifiers[0].endsWith(wcsIdentifier)) {
                throw new IllegalStateException("Unexpected coverage identifier in reponse document, expected \"" + wcsIdentifier + "\" found \"" + wcsIdentifiers[0] + "\"");
            }

        } catch (XPathExpressionException ex) {
            Logger.getLogger(WCSDescribeCoverageInspector_1_1_X.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String getGridBaseCRSAsString() {
        try {
            return wrapper.textAsString(GRID_BASE_CRS_XPATH);
        } catch (XPathExpressionException ex) {
            Logger.getLogger(WCSDescribeCoverageInspector_1_1_X.class.getName()).log(Level.SEVERE, null, ex);
            return "";
        }
    }

    public String getGridTypeAsString() {
        try {
            return wrapper.textAsString(GRID_TYPE_XPATH);
        } catch (XPathExpressionException ex) {
            Logger.getLogger(WCSDescribeCoverageInspector_1_1_X.class.getName()).log(Level.SEVERE, null, ex);
            return "";
        }
    }

    public double[] getGridOffsets() {
        try {
            return wrapper.textAsDoubleArray(GRID_OFFSETS_XPATH);
        } catch (XPathExpressionException ex) {
            Logger.getLogger(WCSDescribeCoverageInspector_1_1_X.class.getName()).log(Level.SEVERE, null, ex);
            return new double[0];
        }
    }
    
    public double[] getGridOrigin() {
        try {
            return wrapper.textAsDoubleArray(GRID_ORIGIN_XPATH);
        } catch (XPathExpressionException ex) {
            Logger.getLogger(WCSDescribeCoverageInspector_1_1_X.class.getName()).log(Level.SEVERE, null, ex);
            return new double[0];
        }
    }
    
    public double[] getGridLowerCorner() {
        try {
            return wrapper.textAsDoubleArray(String.format(GRID_LOWER_CORNER_XPATH, getGridBaseCRSAsString()));
        } catch (XPathExpressionException ex) {
            Logger.getLogger(WCSDescribeCoverageInspector_1_1_X.class.getName()).log(Level.SEVERE, null, ex);
            return new double[0];
        }
    }

    public double[] getGridUpperCorner() {
        try {
            return wrapper.textAsDoubleArray(String.format(GRID_UPPER_CORNER_XPATH, getGridBaseCRSAsString()));
        } catch (XPathExpressionException ex) {
            Logger.getLogger(WCSDescribeCoverageInspector_1_1_X.class.getName()).log(Level.SEVERE, null, ex);
            return new double[0];
        }
    }

    public String getGridDataTypeAsString() {
        try {
            return wrapper.textAsString(GRID_DATA_TYPE_XPATH);
        } catch (XPathExpressionException ex) {
            Logger.getLogger(WCSDescribeCoverageInspector_1_1_X.class.getName()).log(Level.SEVERE, null, ex);
            return "";
        }
    }

    public String[] getGridSupportedFormats() {
        try {
            return wrapper.nodeListTextContentAsStringArray(GRID_SUPPORTED_FORMATS_XPATH);
        } catch (XPathExpressionException ex) {
            Logger.getLogger(WCSDescribeCoverageInspector_1_1_X.class.getName()).log(Level.SEVERE, null, ex);
            return new String[0];
        }
    }

    public String[] getGridSupportedCRS() {
        try {
            return wrapper.nodeListTextContentAsStringArray(GRID_SUPPORTED_CRS_XPATH);
        } catch (XPathExpressionException ex) {
            Logger.getLogger(WCSDescribeCoverageInspector_1_1_X.class.getName()).log(Level.SEVERE, null, ex);
            return new String[0];
        }
    }

    private static class WCSNamespaceContext implements NamespaceContext {

        private final Map<String, String> map;

        private WCSNamespaceContext() {
            map = new HashMap<String, String>();
            // need to resolve these from document, eh...
//            map.put("wcs", "http://www.opengis.net//1.1.1");
//            map.put("ows", "http://www.opengis.net/ows/1.1");
        }

        @Override
        public String getNamespaceURI(String prefix) {
            if (prefix == null) {
                throw new NullPointerException("prefix is null");
            }
            return map.get(prefix);
        }

        private void putNamespaceURI(String prefix, String namespaceURI) {
            map.put(prefix, namespaceURI);
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
