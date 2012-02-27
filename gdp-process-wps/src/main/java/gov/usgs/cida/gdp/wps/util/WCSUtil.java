package gov.usgs.cida.gdp.wps.util;

import gov.usgs.cida.gdp.dataaccess.CoverageMetaData;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.IOUtils;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.geometry.BoundingBox;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.operation.TransformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author tkunicki
 */
public class WCSUtil {
    
    public final static Logger LOGGER = LoggerFactory.getLogger(WCSUtil.class);

    private static final int MAX_COVERAGE_SIZE = 64 << 20; // 64 MB

    private final static String REGEX_OGC_URN = "urn:(?:x-)?ogc(?:-x)?:def:crs:([^:]*):[^:]*:([^:]*)";
    private final static Pattern PATTERN_OGC_URN = Pattern.compile(REGEX_OGC_URN);

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
    
    public static File generateTIFFFile(URI wcsURI, String wcsIdentifier, ReferencedEnvelope featureBounds, boolean requireFullCoverage) {
        File tiffFile = null;
        try {
            URI wcsBaseURI = extractWCSBaseURI(wcsURI);

            Document document = null;
            String wcsGetCapabilitiesURIString = wcsBaseURI.toString() + 
                        "?service=WCS&version=1.1.1&request=DescribeCoverage&Identifiers=" +
                        wcsIdentifier;
            
            InputStream wcsCapabilitiesInputStream = null;
            LOGGER.debug("DescribeCoverage Request : {}", wcsGetCapabilitiesURIString);
            try {
                URL wcsCapabilitiesURL = new URL(wcsGetCapabilitiesURIString);
                wcsCapabilitiesInputStream = wcsCapabilitiesURL.openStream();
                document = DocumentUtil.createDocument(wcsCapabilitiesInputStream);
            } catch (IOException e) {
                throw new RuntimeException("Error obtaining WCS DescribeCoverage document from " +  wcsGetCapabilitiesURIString, e);
            } catch (SAXException e) {
                throw new RuntimeException("Error parsing WCS DescribeCoverage document from " +  wcsGetCapabilitiesURIString, e);
            } finally {
                IOUtils.closeQuietly(wcsCapabilitiesInputStream);
            }

            WCSDescribeCoverageInspector_1_1_X inspector = new WCSDescribeCoverageInspector_1_1_X(document, wcsIdentifier);

            String gridBaseCRSString = inspector.getGridBaseCRSAsString();
            if (gridBaseCRSString.length() == 0) {
                throw new RuntimeException("Can't extract base CRS for WCS coverage");
            }

            String gridTypeString = inspector.getGridTypeAsString();
            if (gridTypeString.length() == 0) {
                gridTypeString = "urn:ogc:def:method:WCS:1.1:2dSimpleGrid";
            }

            double[] gridOffsets = inspector.getGridOffsets();
            if (gridOffsets.length == 0 || gridOffsets.length % 2 != 0) {
                throw new RuntimeException("Can't parse grid offsets for WCS coverage");
            }
            
            double[] gridOrigin = inspector.getGridOrigin();
            if (gridOrigin.length == 0 || gridOrigin.length % 2 != 0) {
                throw new RuntimeException("Can't parse grid origin for WCS coverage");
            }

            double[] gridLowerCorner = inspector.getGridLowerCorner();
            if (gridLowerCorner.length == 0 || gridLowerCorner.length != 2) {
                throw new RuntimeException("Can't parse grid bounding box lower corner for WCS coverage");
            }

            double[] gridUpperCorner = inspector.getGridUpperCorner();
            if (gridLowerCorner == null || gridLowerCorner.length != 2) {
                throw new RuntimeException("Can't parse bounding box upper corner for WCS coverage");
            }

            String gridDataTypeString = inspector.getGridDataTypeAsString();
            if (gridDataTypeString.length() == 0) {
//                throw new RuntimeException("Can't extract Grid Range DataType for WCS Coverage");
            }

            String[] gridSupportedFormats = inspector.getGridSupportedFormats();
            if (gridSupportedFormats.length == 0) {
                throw new RuntimeException("Can't extract supported Formats for WCS coverage");
            }

            String[] gridSupportedCRS = inspector.getGridSupportedCRS();
            if (gridSupportedCRS.length == 0) {
                throw new RuntimeException("Can't extract supported CRS from WCS coverage");
            }

            // Validate we can handle the grid data type
            CoverageMetaData.DataType gridDataType = CoverageMetaData.findCoverageDataType(gridDataTypeString);
            if (gridDataType == CoverageMetaData.UnknownDataType) {
//                throw new RuntimeException("Unknown WCS Grid Range DataType: " + gridDataTypeString);
                gridDataType = CoverageMetaData.PrimitiveDataType.FLOAT;
            }
            
            // Validate we can handle a supported format
            List<String> sharedFormats = new ArrayList(gridSupportedFormats.length);
            sharedFormats.addAll(Arrays.asList(gridSupportedFormats));
            sharedFormats.retainAll(GeoTIFFUtil.getAllowedMimeTypes());
            if (sharedFormats.size() < 1) {
                throw new RuntimeException("WCS coverage not available in an allowed format (geotiff)");
            }
            // BAH!  GeoServer Hack...  Refeactor this block (above/below) into GeoTIFFUtil...  It's
            // list is only case independent, we also need preferred ordering image/tiff 
            // is never wanted in the presense of another options...
            if (sharedFormats.size() > 1 && sharedFormats.contains("image/tiff")) {
                sharedFormats.remove("image/tiff");
            }
            String requestGridFormat = sharedFormats.get(0);

            final boolean gridBaseCRSIsOGC = isOGC(gridBaseCRSString);

            CoordinateReferenceSystem gridBaseCRS = CRS.decode(gridBaseCRSString);

            AxisDirection ad0 = gridBaseCRS.getCoordinateSystem().getAxis(0).getDirection();
            AxisDirection ad1 = gridBaseCRS.getCoordinateSystem().getAxis(1).getDirection();
            boolean gridBaseCRSRequiresSwapXY =
                    gridBaseCRSIsOGC &&
                    ad0 == AxisDirection.NORTH &&
                    ad1 == AxisDirection.EAST;

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
                                    
            double gridXMin = gridLowerCorner[0];
            double gridXMax = gridUpperCorner[0];
            double gridYMin = gridLowerCorner[1];
            double gridYMax = gridUpperCorner[1];
            
            // offsets and origin always in X,Y
            int crsXIndex = gridBaseCRSRequiresSwapXY ? 1 : 0;
            int crsYIndex = gridBaseCRSRequiresSwapXY ? 0 : 1;
            final boolean gridBoundsInCRSOrder =
                    !gridBaseCRSRequiresSwapXY ||
                    (   Math.abs((gridXOffset > 0 ? gridLowerCorner[crsXIndex] : gridUpperCorner[crsXIndex]) - gridOrigin[0]) < Math.abs(gridXOffset) &&
                        Math.abs((gridYOffset > 0 ? gridLowerCorner[crsYIndex] : gridUpperCorner[crsYIndex]) - gridOrigin[1]) < Math.abs(gridYOffset) );
            final boolean serviceRespectsCRSOrder = gridBaseCRSIsOGC && gridBoundsInCRSOrder;

            ReferencedEnvelope gridBounds = gridBaseCRSRequiresSwapXY && !gridBoundsInCRSOrder
                    ? new ReferencedEnvelope(gridYMin, gridYMax, gridXMin, gridXMax, gridBaseCRS)
                    : new ReferencedEnvelope(gridXMin, gridXMax, gridYMin, gridYMax, gridBaseCRS);

            ReferencedEnvelope requestBounds = featureBounds.transform(gridBaseCRS, true);

            if (requireFullCoverage) {
                if (!gridBounds.contains((BoundingBox) requestBounds)) {
                    throw new RuntimeException("WCS Coverage does not fully cover feature bounding box");
                }
            } else {
                if (!gridBounds.intersects((BoundingBox) requestBounds)) {
                    throw new RuntimeException("WCS Coverage does not intersect the feature bounding box");
                }
            }

            double requestSizeBytes =
                    (requestBounds.getWidth() / (gridBaseCRSRequiresSwapXY ? gridYOffset : gridXOffset))
                    * (requestBounds.getHeight() / (gridBaseCRSRequiresSwapXY ? gridXOffset : gridYOffset))
                    * gridDataType.getSizeBytes();

            if (requestSizeBytes < 0) { requestSizeBytes = -requestSizeBytes; }

            double requestSamplingFactor = (requestSizeBytes > MAX_COVERAGE_SIZE)
                    ? Math.ceil(Math.sqrt((double) requestSizeBytes / MAX_COVERAGE_SIZE))
                    : 1;

            String requestBaseCRSString = gridBaseCRSString;
            boolean requestBaseCRSCovertedToNonOGC = false;
            if (gridBaseCRSIsOGC) {
                String gridBaseNonOGCCCRSString = convertCRSToNonOGC(gridBaseCRSString);
                if (Arrays.asList(gridSupportedCRS).contains(gridBaseNonOGCCCRSString)) {
                    requestBaseCRSString = gridBaseNonOGCCCRSString;
                    requestBaseCRSCovertedToNonOGC = true;
                }
            }

            final boolean swapXYForRequest = 
                    gridBaseCRSIsOGC &&
                    gridBaseCRSRequiresSwapXY &&
                    (requestBaseCRSCovertedToNonOGC || !serviceRespectsCRSOrder);

            StringBuilder requestBoundingBoxBuilder = new StringBuilder();
            if (swapXYForRequest) {
                requestBoundingBoxBuilder.
                        append(requestBounds.getMinY()).append(",").
                        append(requestBounds.getMinX()).append(",").
                        append(requestBounds.getMaxY()).append(",").
                        append(requestBounds.getMaxX()).append(",");
            } else {
                requestBoundingBoxBuilder.
                        append(requestBounds.getMinX()).append(",").
                        append(requestBounds.getMinY()).append(",").
                        append(requestBounds.getMaxX()).append(",").
                        append(requestBounds.getMaxY()).append(",");
            }
            requestBoundingBoxBuilder.append(requestBaseCRSString);

            double requestGridXOffset = requestSamplingFactor * gridXOffset;
            double requestGridYOffset = requestSamplingFactor * gridYOffset;
            StringBuilder requestGridOffsetsBuilder = new StringBuilder();
            requestGridOffsetsBuilder.
                    append(requestGridXOffset).
                    append(",").
                    append(requestGridYOffset);

            StringBuilder sb = new StringBuilder();
            sb.append(wcsBaseURI).
                    append("?service=WCS&version=1.1.1&request=GetCoverage").
                    append("&identifier=").append(wcsIdentifier).
                    append("&boundingBox=").append(requestBoundingBoxBuilder).
                    append("&gridBaseCRS=").append(requestBaseCRSString).
                    append("&gridOffsets=").append(requestGridOffsetsBuilder).
                    append("&format=").append(requestGridFormat);
            if (requestSamplingFactor > 1.0) {
                sb.append("&interpolationType=").append("nearest");
            }

            String wcsGetCoverageURIString = sb.toString();
            LOGGER.debug("GetCoverage Request : {}", wcsGetCoverageURIString);
            URL wcsCoverageURL = new URL(wcsGetCoverageURIString);
            HttpURLConnection wcsCoverageConnection = (HttpURLConnection) wcsCoverageURL.openConnection();
            InputStream wcsCoverageInputStream = null;
            try {
            
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
            } finally {
                IOUtils.closeQuietly(wcsCoverageInputStream);
            }

        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        } catch (TransformException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAuthorityCodeException e) {
            throw new RuntimeException(e);
        } catch (FactoryException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return tiffFile;
    }

    public static boolean isOGC(String crs) {
        Matcher matcher = PATTERN_OGC_URN.matcher(crs);
        return matcher.matches();
    }

    public static String convertCRSToNonOGC(String crs) {
        Matcher matcher = PATTERN_OGC_URN.matcher(crs);
        if (matcher.matches()) {
            return (new StringBuilder()).
                    append(matcher.group(1)).
                    append(':').
                    append(matcher.group(2)).
                    toString();
        } else {
            throw new IllegalArgumentException("CRS " + crs + " is not OGC URN");
        }
    }
}
