package gov.usgs.cida.gdp.wps.util;

import gov.usgs.cida.gdp.dataaccess.CoverageMetaData;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.geometry.BoundingBox;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.operation.TransformException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.iosp.geotiff.GeoTiffIOServiceProvider;

/**
 *
 * @author tkunicki
 */
public class WCSUtil {

    private static final int MAX_COVERAGE_SIZE = 64 << 20; // 64 MB

    public static class WCSNamespaceContext implements NamespaceContext {

        public final static Map<String, String> namespaceMap;

        static {
            namespaceMap = new HashMap<String, String>();
            namespaceMap.put("wcs", "http://www.opengis.net/wcs/1.1.1");
            namespaceMap.put("ows", "http://www.opengis.net/ows/1.1");
        }

        @Override
        public String getNamespaceURI(String prefix) {
            if (prefix == null) {
                throw new NullPointerException("prefix is null");
            }
            return namespaceMap.get(prefix);
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
    private final static String descriptionNodeXPath = "/wcs:CoverageDescriptions/wcs:CoverageDescription[wcs:Identifier='%s']";
    private final static String gridBaseCRSXPath = descriptionNodeXPath + "/wcs:Domain/wcs:SpatialDomain/wcs:GridCRS/wcs:GridBaseCRS";
    private final static String gridTypeXPath = descriptionNodeXPath + "/wcs:Domain/wcs:SpatialDomain/wcs:GridCRS/wcs:GridType";
    private final static String gridOffsetsXPath = descriptionNodeXPath + "/wcs:Domain/wcs:SpatialDomain/wcs:GridCRS/wcs:GridOffsets";
    private final static String gridBoundingBoxNodeXPath = descriptionNodeXPath + "/wcs:Domain/wcs:SpatialDomain/ows:BoundingBox[@crs='%s']";
    private final static String gridLowerCornerXPath = gridBoundingBoxNodeXPath + "/ows:LowerCorner";
    private final static String gridUpperCornerXPath = gridBoundingBoxNodeXPath + "/ows:UpperCorner";
    private final static String gridDataTypeXPath = descriptionNodeXPath + "/wcs:Range/wcs:Field/wcs:Axis/ows:DataType";
    private final static String gridSupportedFormatsXPath = descriptionNodeXPath + "/wcs:SupportedFormat";


    private final static DocumentBuilder DOCUMENT_BUILDER;

    static {

        DocumentBuilder documentBuilder = null;
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            System.err.println(e);
        }
        DOCUMENT_BUILDER = documentBuilder;
        
        try {
            NetcdfFile.registerIOProvider(GeoTiffIOServiceProvider.class);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(WCSUtil.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(WCSUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static URI extractWCSBaseURI(URI wcsURI) throws URISyntaxException {
        return new URI(
                    wcsURI.getScheme(),
                    wcsURI.getUserInfo(),
                    wcsURI.getHost(),
                    wcsURI.getPort(),
                    wcsURI.getPath(),
                    null /* drop query */,
                    null /* drop fragement */);
    }

    public static File generateTIFFFile(URI wcsURI, String wcsIdentifier, ReferencedEnvelope featureBounds) {
        File tiffFile = null;
        try {
            URI wcsBaseURI = extractWCSBaseURI(wcsURI);

            Document document = null;
            String wcsGetCapabilitiesURIString = wcsBaseURI.toString() + 
                        "?service=WCS&version=1.1.1&request=DescribeCoverage&Identifiers=" +
                        wcsIdentifier;
            try {
                document = DOCUMENT_BUILDER.parse(wcsGetCapabilitiesURIString);
            } catch (IOException e) {
                throw new RuntimeException("Error obtaining capabilities document from " +  wcsGetCapabilitiesURIString, e);
            } catch (SAXException e) {
                throw new RuntimeException("Error parsing capabilities document from " +  wcsGetCapabilitiesURIString, e);
            }

            // XPath infrastructure is not thread-safe, nor renetrant... bah.
            XPath xpath = XPathFactory.newInstance().newXPath();
            xpath.setNamespaceContext(new WCSNamespaceContext());
            XPathWrapper wrapper = new XPathWrapper(xpath, document);

            String gridBaseCRSString = wrapper.textAsString(String.format(gridBaseCRSXPath, wcsIdentifier));
            if (gridBaseCRSString.length() == 0) {
                throw new RuntimeException("Can't extract GridBaseCRS for WCS Coverage");
            }

            String gridTypeString = wrapper.textAsString(String.format(gridTypeXPath, wcsIdentifier));
            if (gridTypeString.length() == 0) {
                gridTypeString = "urn:ogc:def:method:WCS:1.1:2dSimpleGrid";
            }

            double[] gridOffsets = wrapper.textAsDoubleArray(String.format(gridOffsetsXPath, wcsIdentifier));
            if (gridOffsets.length == 0 || gridOffsets.length % 2 != 0) {
                throw new RuntimeException("Can't parse GridOffsets for WCS Coverage");
            }

            double[] gridLowerCorner = wrapper.textAsDoubleArray(String.format(gridLowerCornerXPath, wcsIdentifier, gridBaseCRSString));
            if (gridLowerCorner.length == 0 || gridLowerCorner.length != 2) {
                throw new RuntimeException("Can't parse Grid BoundingBox lower corner for WCS Coverage");
            }

            double[] gridUpperCorner = wrapper.textAsDoubleArray(String.format(gridUpperCornerXPath, wcsIdentifier, gridBaseCRSString));
            if (gridLowerCorner == null || gridLowerCorner.length != 2) {
                throw new RuntimeException("Can't parse Grid BoundingBox upper corner for WCS Coverage");
            }

            String gridDataTypeString = wrapper.textAsString(String.format(gridDataTypeXPath, wcsIdentifier));
            if (gridDataTypeString.length() == 0) {
                throw new RuntimeException("Can't extract Grid Range DataType for WCS Coverage");
            }

            String[] gridSupportedFormats = wrapper.nodeListTextContentAsStringArray(String.format(gridSupportedFormatsXPath, wcsIdentifier));
            if (gridSupportedFormats.length == 0) {
                throw new RuntimeException("Can't extract Supported Formats for WCS Coverage");
            }

            
            List<String> sharedFormats = new ArrayList(gridSupportedFormats.length);
            sharedFormats.addAll(Arrays.asList(gridSupportedFormats));
            sharedFormats.retainAll(GeoTIFFUtil.getAllowedMimeTypes());
            if (sharedFormats.size() < 1) {
                throw new RuntimeException("WCS coverage not available in an allowed format (geotiff)");
            }
            String requestGridFormat = sharedFormats.get(0);

            double gridXMin = gridLowerCorner[0];
            double gridXMax = gridUpperCorner[0];
            double gridYMin = gridLowerCorner[1];
            double gridYMax = gridUpperCorner[1];

            CoordinateReferenceSystem gridBaseCRS = CRS.decode(gridBaseCRSString);

            AxisDirection ad0 = gridBaseCRS.getCoordinateSystem().getAxis(0).getDirection();
            AxisDirection ad1 = gridBaseCRS.getCoordinateSystem().getAxis(1).getDirection();
            boolean swapXYForCalculations =
                    (ad0 == AxisDirection.NORTH || ad0 == AxisDirection.SOUTH)
                    && (ad1 == AxisDirection.EAST || ad1 == AxisDirection.WEST);

            ReferencedEnvelope gridBounds = swapXYForCalculations
                    ? new ReferencedEnvelope(gridYMin, gridYMax, gridXMin, gridXMax, gridBaseCRS)
                    : new ReferencedEnvelope(gridXMin, gridXMax, gridYMin, gridYMax, gridBaseCRS);

            ReferencedEnvelope featureBoundsTransformed = featureBounds.transform(gridBaseCRS, true);

            boolean fullyCovers = gridBounds.contains((BoundingBox) featureBoundsTransformed);
            if (!fullyCovers) {
                throw new RuntimeException("WCS Coverage does not fully cover feature bounding box");
            }

            CoverageMetaData.DataType gridDataType = CoverageMetaData.findCoverageDataType(gridDataTypeString);
            if (gridDataType == CoverageMetaData.UnknownDataType) {
                throw new RuntimeException("Unknown WCS Grid Range DataType: " + gridDataTypeString);
            }

            double gridXOffset = 0;
            double gridYOffset = 0;
            if (gridTypeString.endsWith("2dGridIn2dCrs")) {
                if (gridOffsets.length != 4) {
                    throw new RuntimeException("Unexpected value count for WCS Coverage GridOffsets");
                }
                if (gridOffsets[1] != 0 || gridOffsets[2] != 0) {
                    throw new RuntimeException("Use of skewed or rotated WCS Coverage is not currently supported.");
                }
                gridXOffset = gridOffsets[0];
                gridYOffset = gridOffsets[3];
            } else if (gridTypeString.endsWith("2dGridIn3dCrs")) {
                throw new RuntimeException("Use of WCS Coverage in 3D CRS is not currently supported.");
            } else if (gridTypeString.endsWith("2dSimpleGrid")) {
                if (gridOffsets.length != 2) {
                    throw new RuntimeException("Unexpected value count for WCS Coverage GridOffsets");
                }
                gridXOffset = gridOffsets[0];
                gridYOffset = gridOffsets[1];
            } else {
                throw new RuntimeException("Unexpected WCS Coverage GridType, " + gridTypeString);
            }

            if (swapXYForCalculations) {
                double temp = gridXOffset;
                gridXOffset = gridYOffset;
                gridYOffset = temp;
            }

            double requestSizeBytes =
                    (featureBoundsTransformed.getWidth() / gridXOffset)
                    * (featureBoundsTransformed.getHeight() / gridYOffset)
                    * gridDataType.getSizeBytes();

            if (requestSizeBytes < 0) { requestSizeBytes = -requestSizeBytes; }

            double requestSamplingFactor = (requestSizeBytes > MAX_COVERAGE_SIZE)
                    ? Math.ceil(Math.sqrt((double) requestSizeBytes / MAX_COVERAGE_SIZE))
                    : 1;

            // TODO: do what we can to figure this out.  I expect the logic
            // below might become quite complicated... The variable 'swapXYForTransform'
            // only shows the need of swap during the transform, but doesn't necessarily
            // flag a need to swap for the BBOX request. This is a function of:
            // 1) service/service-version/vendor/vendor-implementation-version
            // 2) CRS and namespace (type) used to reference it
            // 3) ???
            boolean swapXYForRequest = swapXYForCalculations && gridBaseCRSString.matches("urn:(?:x-)?ogc(?:-x)?:def:crs:.*");

            StringBuilder requestBoundingBoxBuilder = new StringBuilder();
            if (swapXYForRequest) {
                requestBoundingBoxBuilder.
                        append(featureBoundsTransformed.getMinY()).append(",").
                        append(featureBoundsTransformed.getMinX()).append(",").
                        append(featureBoundsTransformed.getMaxY()).append(",").
                        append(featureBoundsTransformed.getMaxX()).append(",");
            } else {
                requestBoundingBoxBuilder.
                        append(featureBoundsTransformed.getMinX()).append(",").
                        append(featureBoundsTransformed.getMinY()).append(",").
                        append(featureBoundsTransformed.getMaxX()).append(",").
                        append(featureBoundsTransformed.getMaxY()).append(",");
            }
            requestBoundingBoxBuilder.append(gridBaseCRSString);

            double requestGridXOffset = requestSamplingFactor * gridXOffset;
            double requestGridYOffset = requestSamplingFactor * gridYOffset;
            StringBuilder requestGridOffsetsBuilder = new StringBuilder();
            if (swapXYForRequest) {
                requestGridOffsetsBuilder.
                        append(requestGridYOffset).
                        append(",").
                        append(requestGridXOffset);
            } else {
                requestGridOffsetsBuilder.
                        append(requestGridXOffset).
                        append(",").
                        append(requestGridYOffset);
            }

            StringBuilder sb = new StringBuilder();
            sb.append(wcsBaseURI).
                    append("?service=WCS&version=1.1.1&request=GetCoverage").
                    append("&identifier=").append(wcsIdentifier).
                    append("&boundingBox=").append(requestBoundingBoxBuilder).
                    append("&gridBaseCRS=").append(gridBaseCRSString).
                    append("&gridOffsets=").append(requestGridOffsetsBuilder).
                    append("&format=").append(requestGridFormat);
            if (requestSamplingFactor > 1.0) {
                sb.append("&interpolationType=").append("nearest");
            }

            URL wcsCoverageURL = new URL(sb.toString());
            HttpURLConnection wcsCoverageConnection = (HttpURLConnection) wcsCoverageURL.openConnection();
            String wcsCoverageContentType = wcsCoverageConnection.getContentType();
            String[] split = wcsCoverageContentType.split("\\s*;\\s*");
            if (!("multipart/related".equals(split[0].trim()))) {
                throw new RuntimeException("Unexpected Content-Type, \"" + wcsCoverageContentType + "\", on WCS getCoverage response to " + sb.toString());
            }
            Pattern keyValuePattern = Pattern.compile("([^=]+)=\"([^\"]+)\"");
            String boundary = null;
            for (int i = 1; i < split.length && boundary == null; ++i) {
                Matcher keyValueMatcher = keyValuePattern.matcher(split[i]);
                if (keyValueMatcher.matches()) {
                    String key = keyValueMatcher.group(1);
                    if ("boundary".equals(key)) {
                        boundary = keyValueMatcher.group(2);
                    }
                }
            }

            MIMEMultipartStream mimeMultipartStream = new MIMEMultipartStream(
                    wcsCoverageConnection.getInputStream(),
                    boundary.getBytes());
            mimeMultipartStream.skipPreamble();
            boolean hasNext = true;
            while (hasNext && tiffFile == null) {
                Map<String, String> headerMap = mimeMultipartStream.readHeaders();
                String contentType = headerMap.get("Content-Type");
                if (GeoTIFFUtil.isAllowedMimeType(contentType)) {
                    String contentTransferEncoding = headerMap.get("Content-Transfer-Encoding");
                    if (contentTransferEncoding != null) {
                        tiffFile = File.createTempFile("gdp", ".tiff");
                        OutputStream tiffOutputStream = new BufferedOutputStream(new FileOutputStream(tiffFile));
                        mimeMultipartStream.readBodyData(tiffOutputStream, contentTransferEncoding);
                        tiffOutputStream.close();
                    }
                } else {
                    mimeMultipartStream.discardBodyData();
                }
                hasNext = mimeMultipartStream.readBoundary();
            }

        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        } catch (TransformException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAuthorityCodeException e) {
            throw new RuntimeException(e);
        } catch (FactoryException e) {
            throw new RuntimeException(e);
        } catch (XPathExpressionException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return tiffFile;
    }

    public static class XPathWrapper {

        public final static String DEFAULT_REGEX = "\\s+";
        private final XPath xpath;
        private final Document document;

        public XPathWrapper(XPath xpath, Document document) {
            this.xpath = xpath;
            this.document = document;
        }

        public String textAsString(String expression) throws XPathExpressionException {
            return xpath.evaluate(expression, document);
        }

        public String[] textAsStringArray(String expression) throws XPathExpressionException {
            return textAsStringArray(expression, DEFAULT_REGEX);
        }

        public String[] textAsStringArray(String expression, String regex) throws XPathExpressionException {
            String string = xpath.evaluate(expression, document);
            if (string != null && string.length() > 0) {
                return string.split(regex);
            } else {
                return new String[0];
            }
        }

        public double[] textAsDoubleArray(String expression) throws XPathExpressionException {
            return textAsDoubleArray(expression, DEFAULT_REGEX);
        }

        public double[] textAsDoubleArray(String expression, String regex) throws XPathExpressionException {
            String[] split = textAsStringArray(expression, regex);
            double[] doubles = new double[split.length];
            for (int i = 0; i < split.length; ++i) {
                doubles[i] = Double.parseDouble(split[i]);
            }
            return doubles;
        }

        public String[] nodeListTextContentAsStringArray(String expression) throws XPathExpressionException {
            Object object = xpath.evaluate(expression, document, XPathConstants.NODESET);
            if (object instanceof NodeList) {
                NodeList nodeList = (NodeList)object;
                String strings[] = new String[nodeList.getLength()];
                for (int i = 0; i < nodeList.getLength(); ++i) {
                    strings[i] = nodeList.item(i).getTextContent();
                }
                return strings;
            } else {
                return new String[0];
            }
        }
    }
}
