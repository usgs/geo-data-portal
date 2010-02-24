/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usgs.gdp.analysis;

import com.google.common.base.Preconditions;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.GregorianCalendar;
import java.util.List;
import org.geotools.data.FeatureSource;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.coverage.grid.InvalidRangeException;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import ucar.nc2.VariableIF;
import ucar.nc2.VariableSimpleIF;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.ft.FeatureDataset;
import ucar.nc2.ft.FeatureDatasetFactoryManager;
import ucar.nc2.ft.FeatureDatasetPoint;
import ucar.nc2.ft.PointFeature;
import ucar.nc2.ft.PointFeatureIterator;
import ucar.nc2.ft.StationTimeSeriesFeature;
import ucar.nc2.ft.StationTimeSeriesFeatureCollection;
import ucar.nc2.units.DateRange;
import ucar.unidata.geoloc.LatLonPointImpl;
import ucar.unidata.geoloc.LatLonRect;
import ucar.unidata.geoloc.Station;

/**
 *
 * @author tkunicki
 */
public class StationDataCSVWriter {

    public final static String DATE_FORMAT = "yyyy-MM-dd";

    public final static GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    public static boolean write(
            FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection,
            StationTimeSeriesFeatureCollection stationTimeSeriesFeatureCollection,
            List<VariableSimpleIF> variableList,
            DateRange dateRange,
            BufferedWriter writer)
            throws IOException, InvalidRangeException {

        Preconditions.checkNotNull(featureCollection, "featureCollection may not be null");
        Preconditions.checkNotNull(stationTimeSeriesFeatureCollection, "stationTimeSeriesFeatureCollection may not be null");
        Preconditions.checkNotNull(variableList, "variableList may not be null");
        Preconditions.checkNotNull(dateRange, "dateRange may not be null");
        Preconditions.checkNotNull(writer, "writer may not be null");

        GregorianCalendar start = new GregorianCalendar();
        start.setTime(dateRange.getStart().getDate());

        GregorianCalendar end = new GregorianCalendar();
        end.setTime(dateRange.getEnd().getDate());

        Envelope envelope = featureCollection.getBounds();
        
        FeatureIterator<SimpleFeature> featureIterator = featureCollection.features();
        Geometry geometry = (Geometry)featureIterator.next().getDefaultGeometry();
        while (featureIterator.hasNext()) {
            geometry = geometry.union((Geometry)featureIterator.next().getDefaultGeometry());
        }

        LatLonRect latLonRect = new LatLonRect(
                new LatLonPointImpl(envelope.getMinY(), envelope.getMinX()),
                new LatLonPointImpl(envelope.getMaxY(), envelope.getMaxX()));

        List<Station> stationList = new ArrayList<Station>();
        List<PointFeatureIteratorCache> pointFeatureIteratorCacheList =
                new ArrayList<PointFeatureIteratorCache>();

        for (Station station : stationTimeSeriesFeatureCollection.getStations(latLonRect)) {
            Coordinate stationCoordinate = new Coordinate(station.getLongitude(), station.getLatitude());
            Geometry stationGeometry = GEOMETRY_FACTORY.createPoint(stationCoordinate);
            if (geometry.contains(stationGeometry)) {

                StationTimeSeriesFeature stationTimeSeriesFeature =
                        stationTimeSeriesFeatureCollection.getStationFeature(station).subset(dateRange);

                stationTimeSeriesFeature.calcBounds();

                if (stationTimeSeriesFeature.size() > 0) {
                    stationList.add(station);
                    pointFeatureIteratorCacheList.add(
                            new PointFeatureIteratorCache(stationTimeSeriesFeature.getPointFeatureIterator(-1)));
                }
            }
        }

        
        StringBuilder sb = new StringBuilder();
        // writer header
        for (Station station : stationList) {
            for (VariableSimpleIF variableIF : variableList) {
                sb.append(',').append(station.getName());
            }
        }
        writer.write(sb.toString());
        writer.newLine();

        sb.setLength(0);
        for (Station station : stationList) {
            for (VariableSimpleIF variableIF : variableList) {
                sb.append(',').append(variableIF.getShortName()).append(" (").append(variableIF.getUnitsString()).append(")");
            }
        }
        writer.write(sb.toString());
        writer.newLine();

        DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        GregorianCalendar current = new GregorianCalendar();
        current.setTime(start.getTime());
        while (current.before(end)) {
            sb.setLength(0);
            sb.append(dateFormat.format(current.getTime()));
            for (PointFeatureIteratorCache cache : pointFeatureIteratorCacheList) {
                PointFeature pf = cache.getFeatureForDate(current.getTime());
                for (VariableSimpleIF variableIF : variableList) {
                    float variable = pf == null ? Float.NaN : pf.getData().getScalarFloat(variableIF.getShortName());
                    sb.append(',').append(variable);
                }
            }
            writer.write(sb.toString());
            writer.newLine();
            current.add(Calendar.DATE, 1);
        }

        for (PointFeatureIteratorCache cache : pointFeatureIteratorCacheList) {
            cache.finish();
        }

        return true;
    }

    public static class PointFeatureIteratorCache {

        public final static long dayInMillis = 1000 * 60 * 60 * 24;
        private PointFeatureIterator pfi;
        private PointFeature pfNext;

        public PointFeatureIteratorCache(PointFeatureIterator pfi) {
            this.pfi = pfi;
        }

        public PointFeature getFeatureForDate(Date date) throws IOException {
            if (pfNext == null) {
                if (pfi.hasNext()) {
                    pfNext = pfi.next();
                } else {
                    return null;
                }
            }
            PointFeature pf = null;
            long delta = date.getTime() - pfNext.getObservationTimeAsDate().getTime();
            if (delta < 0) {
                delta = -delta;
            }
            if (delta < dayInMillis) {
                pf = pfNext;
                pfNext = null;
            }
            return pf;
        }

        public void finish() {
            pfi.finish();
            pfi = null;
        }
    }


    public static void main(String[] args) {


        String sfLocation = "src/main/resources/Sample_Files/Shapefiles/Yahara_River_HRUs_geo_WGS84.shp";
        String ncLocation = 
                "http://internal.cida.usgs.gov/thredds/dodsC/models/GSODX/gsod.nc";
//                "/Users/tkunicki/Downloads/GSOD/netcdfX/gsod.nc";

        String variableName = "temp";

        FileDataStore dataStore = null;
        FeatureDataset dataset = null;
        try {
            long startMillis = System.currentTimeMillis();

            // pull out shapefile

            dataStore = FileDataStoreFinder.getDataStore(new File(sfLocation));
            FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = dataStore.getFeatureSource();

            FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection = featureSource.getFeatures();

            // pull out StationTimeSeriesFeatureCollection
            StationTimeSeriesFeatureCollection stationTimeSeriesFeatureCollection = null;
            dataset = FeatureDatasetFactoryManager.open(FeatureType.STATION, ncLocation, null, new Formatter());
            if (dataset != null) {
                if (dataset instanceof FeatureDatasetPoint) {
                    FeatureDatasetPoint featureDataSetPoint = (FeatureDatasetPoint)dataset;
                    ucar.nc2.ft.FeatureCollection fc = featureDataSetPoint.getPointFeatureCollectionList().get(0);
                    if (fc instanceof StationTimeSeriesFeatureCollection) {
                        stationTimeSeriesFeatureCollection = (StationTimeSeriesFeatureCollection)fc;
                    }
                }
            }

            List<VariableSimpleIF> variableList = Arrays.asList(new VariableSimpleIF[] { dataset.getDataVariable(variableName) });

            BufferedWriter writer = null;
            try {
                writer = new BufferedWriter(new FileWriter("yo_mama.csv"));
                StationDataCSVWriter.write(
                        featureCollection,
                        stationTimeSeriesFeatureCollection,
                        variableList,
                        new DateRange(new Date(100, 0, 1), new Date(102, 11, 31)),
                        writer);
            } finally {
                if (writer != null) { try {  writer.close(); } catch (IOException e) {} }
            }
            System.out.println("Finished in " + (System.currentTimeMillis() - startMillis));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (dataStore != null) { dataStore.dispose(); }
            if (dataset != null) { try { dataset.close(); } catch (IOException e) { } }
        }
    }
}
