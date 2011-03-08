package gov.usgs.cida.gdp.wps.util;

/**
 *
 * @author tkunicki
 */
public class MIMEUtil {

    public static String getSuffixFromMIMEType(String mimeType) {
        String[] mimeTypeSplit = mimeType.split("/");
        String suffix =  mimeTypeSplit[mimeTypeSplit.length - 1];
        if (suffix.equalsIgnoreCase("geotiff")) {
            suffix = "tiff";
        }
        return suffix;
    }
}
