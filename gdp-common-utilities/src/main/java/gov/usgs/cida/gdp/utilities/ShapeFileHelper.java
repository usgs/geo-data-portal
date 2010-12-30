package gov.usgs.cida.gdp.utilities;

import gov.usgs.cida.gdp.utilities.bean.ShapeFileSet;
import java.io.File;
import java.util.List; 

/**
 *
 * @author admin
 */
public class ShapeFileHelper {

    static public File getShapeFileFromShapeSetName(final String shapeSetName, final List<ShapeFileSet> shapeBeanList) {
        for (ShapeFileSet sfsb : shapeBeanList) {
            if (shapeSetName.equals(sfsb.getName())) {
                return sfsb.getShapeFile();
//                shapefilePath = shapeFile.getAbsolutePath();
            }
        }
        return null;
    }

    static public String getShapeFilePathFromShapeSetName(final String shapeSetName, final List<ShapeFileSet> shapeBeanList) {
        for (ShapeFileSet sfsb : shapeBeanList) {
            if (shapeSetName.equals(sfsb.getName())) {
                return sfsb.getShapeFile().getAbsolutePath();
            }
        }
        return null;
    }

}
