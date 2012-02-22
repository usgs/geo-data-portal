/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.usgs.cida.gdp.wps.util;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author tkunicki
 */
public class GeoTIFFUtil {

    public final static String MIMETYPE_DEFAULT = "image/geotiff";

    public final static Set<String> MIMETYPE_SET;
    static {
        Set<String> allowedFormats = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        // this list grows as we encounter new mime-types for geotiff.
        allowedFormats.add(MIMETYPE_DEFAULT);
        allowedFormats.add("image/tiff;subtype=\"geotiff\"");
        allowedFormats.add("image/x-geotiff");
        allowedFormats.add("application/geotiff");
        allowedFormats.add("application/x-geotiff");
        allowedFormats.add("image/tiff");
        allowedFormats.add("image/x-tiff");
        MIMETYPE_SET = Collections.unmodifiableSet(allowedFormats);
    }

    public static String getDefaultMimeType() {
        return MIMETYPE_DEFAULT;
    }

    public static Collection<String> getAllowedMimeTypes() {
        return MIMETYPE_SET;
    }

    public static boolean isAllowedMimeType(String mimeType) {
        return MIMETYPE_SET.contains(mimeType);
    }
}
