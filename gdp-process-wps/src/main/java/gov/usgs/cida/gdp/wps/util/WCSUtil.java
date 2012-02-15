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
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author tkunicki
 */
public class WCSUtil {

    private static final int MAX_COVERAGE_SIZE = 64 << 20; // 64 MB

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

            WCSDescribeCoverageInspector_1_1 inspector = new WCSDescribeCoverageInspector_1_1(document, wcsIdentifier);

            String gridBaseCRSString = inspector.getGridBaseCRSAsString();
            if (gridBaseCRSString.length() == 0) {
                throw new RuntimeException("Can't extract GridBaseCRS for WCS Coverage");
            }

            String gridTypeString = inspector.getGridTypeAsString();
            if (gridTypeString.length() == 0) {
                gridTypeString = "urn:ogc:def:method:WCS:1.1:2dSimpleGrid";
            }

            double[] gridOffsets = inspector.getGridOffsets();
            if (gridOffsets.length == 0 || gridOffsets.length % 2 != 0) {
                throw new RuntimeException("Can't parse GridOffsets for WCS Coverage");
            }

            double[] gridLowerCorner = inspector.getGridLowerCorner();
            if (gridLowerCorner.length == 0 || gridLowerCorner.length != 2) {
                throw new RuntimeException("Can't parse Grid BoundingBox lower corner for WCS Coverage");
            }

            double[] gridUpperCorner = inspector.getGridUpperCorner();
            if (gridLowerCorner == null || gridLowerCorner.length != 2) {
                throw new RuntimeException("Can't parse Grid BoundingBox upper corner for WCS Coverage");
            }

            String gridDataTypeString = inspector.getGridDataTypeAsString();
            if (gridDataTypeString.length() == 0) {
                throw new RuntimeException("Can't extract Grid Range DataType for WCS Coverage");
            }

            String[] gridSupportedFormats = inspector.getGridSupportedFormats();
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

            if (requireFullCoverage) {
                if (!gridBounds.contains((BoundingBox) featureBoundsTransformed)) {
                    throw new RuntimeException("WCS Coverage does not fully cover feature bounding box");
                }
            } else {
                if (!gridBounds.intersects((BoundingBox) featureBoundsTransformed)) {
                    throw new RuntimeException("WCS Coverage does not intersect the feature bounding box");
                }
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
            requestSamplingFactor = 1;

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
}
