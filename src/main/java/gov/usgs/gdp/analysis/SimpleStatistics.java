/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.usgs.gdp.analysis;

import gov.usgs.gdp.analysis.statistics.StatisticsAccumulator1D;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.data.FeatureSource;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.filter.text.cql2.CQL;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;

import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDataset;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.ft.FeatureDataset;
import ucar.nc2.ft.FeatureDatasetFactoryManager;
import ucar.unidata.geoloc.LatLonPointImpl;
import ucar.unidata.geoloc.LatLonRect;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

/**
 * 
 * @author tkunicki
 */
public class SimpleStatistics {

    public static List getStatisticsList(FeatureCollection featureCollection,
            String featureId, FeatureDataset featureDataset,
            String variableName, Range timeRange) throws IOException {
        List list = new ArrayList();
        list.add("delete this method");
        return list;
    }

    public static List<String> getStatisticsList(SimpleFeature simpleFeature,
            FeatureDataset featureDataset, String variableName, Range timeRange)
            throws IOException {
        List list = new ArrayList();
        list.add("delete this method");
        return list;

    }

}
