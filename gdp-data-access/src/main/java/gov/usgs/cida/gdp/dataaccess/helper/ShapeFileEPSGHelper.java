package gov.usgs.cida.gdp.dataaccess.helper;

import gov.usgs.cida.gdp.utilities.FileHelper;
import java.io.File;
import java.io.IOException;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author isuftin
 */
public class ShapeFileEPSGHelper {
    private static Logger log = LoggerFactory.getLogger(ShapeFileEPSGHelper.class);

    private File prjFile;

    ShapeFileEPSGHelper(final File prjFile)  {
        this.prjFile = prjFile;
    }

    public String getEpsgFromPrj() {
        String result = null;
        return result;
    }

    public static String getEpsgFromPrj(final File prjFile) throws IOException, FactoryException {
        String result = null;
        if (prjFile == null || !prjFile.exists()) return result;

        byte[] wktByteArray = FileHelper.getByteArrayFromFile(prjFile);
        result = ShapeFileEPSGHelper.getEpsgFromWkt(new String(wktByteArray));

        return result;
    }

    public static String getEpsgFromWkt(final String wkt) throws FactoryException {
        String result = null;
        if (wkt == null || "".equals(wkt)) return result;

        CoordinateReferenceSystem crs = null;
        try {
            crs = CRS.parseWKT(wkt);
        } catch (FactoryException ex) {
            log.error(ex.getMessage());
            throw ex;
        }

        result = CRS.lookupIdentifier(crs, true);

        return result;
    }

    /**
     * @return the prjFile
     */
    public File getPrjFile() {
        return this.prjFile;
    }

    /**
     * @param prjFile the prjFile to set
     */
    public void setPrjFile(File prjFile) {
        this.prjFile = prjFile;
    }

}
