/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usgs.cida.gdp.geometry;

import gov.usgs.cida.gdp.utilities.FileHelper;
import gov.usgs.cida.gdp.utilities.ShapeFileHelper;
import gov.usgs.cida.gdp.utilities.bean.AvailableFiles;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureSource;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.SchemaException;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.filter.text.cql2.CQLException;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;

/**
 *
 * @author admin
 */
public class Geometry {

    public static FeatureCollection<SimpleFeatureType, SimpleFeature> getFeatureCollection(
            final String userspacePath, final String userDirectory,
            final File uploadDirectory, final String appTempDir, final String shapeSet,
            final String[] features, final String outputFile, final String attribute) 
        throws MalformedURLException, IOException, SchemaException,
            NoSuchAuthorityCodeException, FactoryException, CQLException {
        
        //NHDService.getGeometry(lon, lat, shapefilePath, shxFilePath);

        return shapefileToFeatureCollection(uploadDirectory, outputFile,
                userDirectory, userspacePath, appTempDir, shapeSet, features, attribute);
    }

    static public FeatureCollection<SimpleFeatureType, SimpleFeature> shapefileToFeatureCollection(
                final File uploadDirectory, final String outputFile, final String userDirectory,
                final String userSpacePath, final String appTempDir, final String shapeSet,
                final String[] features, final String attribute
            ) throws IOException, CQLException {
        
        FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection = null;
        FileHelper.deleteFile(uploadDirectory.getPath() + outputFile);
        String shapefilePath = null;

        FileDataStore shapeFileDataStore;
        FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = null;

        // Set up the shapefile

        String userDir = userDirectory;
        if (userDir != null && !"".equals(userSpacePath + userDir)) {
            if (FileHelper.doesDirectoryOrFileExist(userSpacePath + userDir)) {
                FileHelper.updateTimestamp(userSpacePath + userDir, false); // Update the timestamp
                userDir = userSpacePath + userDir;
            } else {
                userDir = "";
            }
        }


        AvailableFiles afb = AvailableFiles.getAvailableFilesBean(appTempDir, userDir);
        File shapeFile = ShapeFileHelper.getShapeFileFromShapeSetName(shapeSet, afb.getShapeSetList());

        shapeFileDataStore = FileDataStoreFinder.getDataStore(shapeFile);
        featureSource = shapeFileDataStore.getFeatureSource();

        if (features[0].equals("*")) {
            featureCollection = featureSource.getFeatures();
        } else {
            //Implementing a filter using the CQL language
            // http://docs.codehaus.org/display/GEOTOOLS/CQL+Parser+Design
            String cqlQuery = attribute + " == '" + features[0] + "'";
            Filter attributeFilter = null;
            for (int index = 1; index < features.length; index++) {
                cqlQuery = cqlQuery + " OR " + attribute + " == '" + features[index] + "'";
            }

            attributeFilter = CQL.toFilter(cqlQuery);
            featureCollection = featureSource.getFeatures(
                    new DefaultQuery(
                    featureSource.getSchema().getTypeName(),
                    attributeFilter));
        }
        return featureCollection;
    }
}
