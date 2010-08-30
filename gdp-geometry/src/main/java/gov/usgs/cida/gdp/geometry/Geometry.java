/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usgs.cida.gdp.geometry;

import gov.usgs.cida.gdp.utilities.FileHelper;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.SchemaException;
import org.geotools.filter.text.cql2.CQLException;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;

/**
 *
 * @author admin
 */
public class Geometry {

    public static FeatureCollection<SimpleFeatureType, SimpleFeature> getFeatureCollection(
            final String lat, final String lon, final String userspacePath, final String userDirectory,
            final File uploadDirectory, final String appTempDir, final String shapeSet,
            final String[] features, final String outputFile, final String attribute)
            throws MalformedURLException, IOException, SchemaException, NoSuchAuthorityCodeException, FactoryException, CQLException {

        // Define a feature collection isiong lat/lon or shapefile
        if (lat != null && lon != null) {

            String fullUserDir = userspacePath + userDirectory + FileHelper.getSeparator();

            // When getting the geometry from the epa nhd service, there
            // won't be any attributes. But the attribute cannot be null, so set it
            // to a placeholder, "placeholder"
            String shapefilePath = fullUserDir + "latlon.shp";
            String shxFilePath = fullUserDir + "latlon.shx";

            return NHDService.getGeometry(lon, lat, shapefilePath, shxFilePath);
        } else {
            return ShapefileToFeatureCollection.shapefileToFeatureCollection(
                    uploadDirectory, outputFile, userDirectory, userspacePath, appTempDir, shapeSet, features, attribute);
        }
    }
}
