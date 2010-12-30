package gov.usgs.cida.gdp.dataaccess.helper;

import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author isuftin
 */
public class ShapeFileEPSGHelper {
    Logger log = LoggerFactory.getLogger(ShapeFileEPSGHelper.class);

    private File zippedShapeFile;

    ShapeFileEPSGHelper() {}

    ShapeFileEPSGHelper(final File zippedShapeFile)  {
        this.zippedShapeFile = zippedShapeFile;
    }

    public File getPRJFileFromZip(final File zippedShapeFile) {
        File result = null;

        

        return result;
    }

    /**
     * @return the zippedShapeFile
     */
    public File getZippedShapeFile() {
        return zippedShapeFile;
    }

    /**
     * @param zippedShapeFile the zippedShapeFile to set
     */
    public void setZippedShapeFile(File zippedShapeFile) {
        this.zippedShapeFile = zippedShapeFile;
    }

}
