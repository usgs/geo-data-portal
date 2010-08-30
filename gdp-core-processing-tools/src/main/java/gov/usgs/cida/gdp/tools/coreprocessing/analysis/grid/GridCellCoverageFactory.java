/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.usgs.cida.gdp.tools.coreprocessing.analysis.grid;

import com.vividsolutions.jts.geom.Geometry;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import org.geotools.feature.FeatureCollection;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;
import ucar.nc2.dt.GridCoordSystem;

/**
 *
 * @author tkunicki
 */
public abstract class GridCellCoverageFactory {

    public static Map<Object, GridCellCoverage> generateByFeatureAttribute(
            FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection,
            String attributeName,
            GridCoordSystem gridCoordinateSystem)
            throws FactoryException, TransformException
    {
        
        AttributeDescriptor attributeDescriptor =
                featureCollection.getSchema().getDescriptor(attributeName);
        if (attributeDescriptor == null) {
            throw new IllegalArgumentException(
                    "Attribute " + attributeName + " not found in FeatureCollection.");
        }

        boolean attributeComparable = Comparable.class.isAssignableFrom(
                attributeDescriptor.getType().getBinding());

        Map<Object, GridCellCoverage> attributeCoverageMap = attributeComparable ?
                // rely on Comparable to sort
                new TreeMap<Object, GridCellCoverage>() :
                // use order from FeatureCollection.iterator();
                new LinkedHashMap<Object, GridCellCoverage>(); 

        CoordinateReferenceSystem featureCoordinateSystem =
                featureCollection.getSchema().getCoordinateReferenceSystem();
        GridCellGeometry gridCellGeometry = new GridCellGeometry(gridCoordinateSystem);
        Iterator<SimpleFeature> featureIterator = featureCollection.iterator();
        try {
            while (featureIterator.hasNext()) {
                
                SimpleFeature simpleFeature = featureIterator.next();
                Object attribute = simpleFeature.getAttribute(attributeName);
                
                if (attribute != null) {
                    Geometry geometry = (Geometry)simpleFeature.getDefaultGeometry();
                    GridCellCoverage gridCellCoverage = attributeCoverageMap.get(attribute);
                    
                    if (gridCellCoverage == null) {
                        gridCellCoverage = new GridCellCoverage(
                                geometry,
                                featureCoordinateSystem,
                                gridCellGeometry);
                        attributeCoverageMap.put(attribute, gridCellCoverage);
                    } else {
                        gridCellCoverage.updateCoverage(
                                geometry,
                                featureCoordinateSystem,
                                gridCellGeometry);
                    }

                }
            }
        } finally {
            featureCollection.close(featureIterator);
        }
        return attributeCoverageMap;
    }

}
