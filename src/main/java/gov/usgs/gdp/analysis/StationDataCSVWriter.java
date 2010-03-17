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
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
import java.util.TimeZone;
import org.geotools.data.FeatureSource;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.opengis.coverage.grid.InvalidRangeException;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
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
            BufferedWriter writer,
            boolean groupByVariable,
            String delimitter)
            throws IOException, InvalidRangeException {

        Preconditions.checkNotNull(featureCollection, "featureCollection may not be null");
        Preconditions.checkNotNull(stationTimeSeriesFeatureCollection, "stationTimeSeriesFeatureCollection may not be null");
        Preconditions.checkNotNull(variableList, "variableList may not be null");
        Preconditions.checkNotNull(dateRange, "dateRange may not be null");
        Preconditions.checkNotNull(writer, "writer may not be null");

        if (delimitter == null || delimitter.length() == 0) { delimitter = ","; }

        TimeZone timeZone = TimeZone.getTimeZone("UTC");

        GregorianCalendar start = new GregorianCalendar(timeZone);
        start.setTime(dateRange.getStart().getDate());

        GregorianCalendar end = new GregorianCalendar(timeZone);
        end.setTime(dateRange.getEnd().getDate());

        Envelope envelope = featureCollection.getBounds();

        LatLonRect latLonRect = new LatLonRect(
                new LatLonPointImpl(envelope.getMinY(),envelope.getMinX()),
                new LatLonPointImpl(envelope.getMaxY(),envelope.getMaxX()));


        List<PointFeatureCache> pointFeatureCacheList = new ArrayList<PointFeatureCache>();
        for (Station station : stationTimeSeriesFeatureCollection.getStations(latLonRect)) {
            Coordinate stationCoordinate = new Coordinate(station.getLongitude(), station.getLatitude());
            Geometry stationGeometry = GEOMETRY_FACTORY.createPoint(stationCoordinate);
            boolean stationContained = false;
            FeatureIterator<SimpleFeature> featureIterator = featureCollection.features();
            try {
                while (featureIterator.hasNext() && !stationContained) {
                    stationContained = ((Geometry)featureIterator.next().getDefaultGeometry()).contains(stationGeometry);
                }
            } finally {
                featureCollection.close(featureIterator);
            }
            if (stationContained) {
                StationTimeSeriesFeature stationTimeSeriesFeature = stationTimeSeriesFeatureCollection.getStationFeature(station).subset(dateRange);
                PointFeatureCache pointFeatureCache =
                        new PointFeatureCache(stationTimeSeriesFeature, variableList);
                if (pointFeatureCache.getFeatureCount() > 0) {
                    pointFeatureCacheList.add(pointFeatureCache);
                } else {
                    pointFeatureCache.finish();
                }
            }
        }

        try {

            int stationCount = pointFeatureCacheList.size();
            int variableCount = variableList.size();

            StringBuilder lineBuffer = new StringBuilder();
            // HEADER LINE 1
            if (groupByVariable) {
                for (int variableIndex = 0; variableIndex < variableCount; ++variableIndex) {
                    for (int stationIndex = 0; stationIndex < stationCount; ++stationIndex) {
                        lineBuffer.append(delimitter).append(pointFeatureCacheList.get(stationIndex).getStation().getName());
                    }
                }
            } else {
                for (int stationIndex = 0; stationIndex < stationCount; ++stationIndex) {
                    for (int variableIndex = 0; variableIndex < variableCount; ++variableIndex) {
                        lineBuffer.append(delimitter).append(pointFeatureCacheList.get(stationIndex).getStation().getName());
                    }
                }
            }
            writer.write(lineBuffer.toString());
            writer.newLine();

            // HEADER LINE 2
            lineBuffer.setLength(0);
            if (groupByVariable) {
                for (int variableIndex = 0; variableIndex < variableCount; ++variableIndex) {
                    for (int stationIndex = 0; stationIndex < stationCount; ++stationIndex) {
                        VariableSimpleIF variable = variableList.get(variableIndex);
                        String name = variable.getShortName();
                        String units = variable.getUnitsString();
                        lineBuffer.append(delimitter).append(name);
                        if( units !=null && units.length() > 0) {
                            lineBuffer.append(" (").append(units).append(")");
                        }
                    }
                }
            } else {
                for (int stationIndex = 0; stationIndex < stationCount; ++stationIndex) {
                    for (int variableIndex = 0; variableIndex < variableCount; ++variableIndex) {
                        VariableSimpleIF variable = variableList.get(variableIndex);
                        String name = variable.getShortName();
                        String units = variable.getUnitsString();
                        lineBuffer.append(delimitter).append(name);
                        if( units !=null && units.length() > 0) {
                            lineBuffer.append(" (").append(units).append(")");
                        }
                    }
                }
            }
            writer.write(lineBuffer.toString());
            writer.newLine();

            DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
            dateFormat.setTimeZone(timeZone);
            GregorianCalendar current = new GregorianCalendar(timeZone);
            current.setTime(start.getTime());

            if (groupByVariable) {
                float[][] rowData = new float[stationCount][];
                while (current.before(end)) {
                    lineBuffer.setLength(0);
                    lineBuffer.append(dateFormat.format(current.getTime()));
                    for (int stationIndex = 0; stationIndex < stationCount; ++stationIndex) {
                        rowData[stationIndex] = pointFeatureCacheList.get(stationIndex).getFeatureForDate(current.getTime());
                    }
                    for (int variableIndex = 0; variableIndex < variableCount; ++variableIndex) {
                        for (int stationIndex = 0; stationIndex < stationCount; ++stationIndex) {
                            float variableValue = rowData[stationIndex] != null ?
                                rowData[stationIndex][variableIndex] :
                                Float.NaN;
                                lineBuffer.append(delimitter).append(variableValue);
                        }
                    }
                    writer.write(lineBuffer.toString());
                    writer.newLine();
                    current.add(Calendar.DATE, 1);
                }
            } else {
                float[][] rowData = new float[stationCount][];
                while (current.before(end)) {
                    lineBuffer.setLength(0);
                    lineBuffer.append(dateFormat.format(current.getTime()));
                    for (int stationIndex = 0; stationIndex < stationCount; ++stationIndex) {
                        rowData[stationIndex] = pointFeatureCacheList.get(stationIndex).getFeatureForDate(current.getTime());
                    }
                    for (int stationIndex = 0; stationIndex < stationCount; ++stationIndex) {
                        for (int variableIndex = 0; variableIndex < variableCount; ++variableIndex) {
                            float variableValue = rowData[stationIndex] != null ?
                                rowData[stationIndex][variableIndex] :
                                Float.NaN;
                                lineBuffer.append(delimitter).append(variableValue);
                        }
                    }
                    writer.write(lineBuffer.toString());
                    writer.newLine();
                    current.add(Calendar.DATE, 1);
                }
            }
        } finally {
            if( pointFeatureCacheList != null) {
                for (PointFeatureCache cache : pointFeatureCacheList) {
                    if (cache != null) {
                        cache.finish();
                    }
                }
            }
        }

        return true;
    }

    private static class PointFeatureCache {

        public final static long MILLIS_PER_DAY = 1000 * 60 * 60 * 24;
        public final static int BUFFER_SIZE = 16 << 10;

        private Station station;

        private int featureCount;
        private int variableCount;

        private File cacheFile;
        private DataInputStream cacheInputStream;

        long nextTimeMillis;

        public PointFeatureCache(StationTimeSeriesFeature stationTimeSeriesFeature, List<VariableSimpleIF> variables) throws IOException {

            this.station = stationTimeSeriesFeature;

            variableCount = variables.size();

            PointFeatureIterator pointFeatureIterator = null;
            try {

                try {
                    pointFeatureIterator = stationTimeSeriesFeature.getPointFeatureIterator(-1);
                } catch (IOException e) {
                    // cdmremote protocol implementation will throw exception if request
                    // results in empty set...  Ignore for now...
                }

                if (pointFeatureIterator != null) {

                    cacheFile = File.createTempFile("tmp.", ".cache");

                    DataOutputStream cacheOutputStream = null;
                    try {
                        cacheOutputStream = new DataOutputStream(
                                new BufferedOutputStream(
                                        new FileOutputStream(cacheFile), BUFFER_SIZE));
                        while(pointFeatureIterator.hasNext()) {
                            PointFeature pf = pointFeatureIterator.next();
                            cacheOutputStream.writeLong(pf.getNominalTimeAsDate().getTime());
                            for (VariableSimpleIF variable : variables) {
                                String shortName = variable.getShortName();
                                cacheOutputStream.writeFloat(pf.getData().getScalarFloat(shortName));
                            }
                            ++featureCount;
                        }
                    } finally {
                        if (cacheOutputStream != null) {
                            try { cacheOutputStream.close(); } catch (IOException e) { }
                        }
                    }

                    try {
                        if (featureCount > 0) {
                            cacheInputStream = new DataInputStream(
                                new BufferedInputStream(
                                        new FileInputStream(cacheFile), BUFFER_SIZE));
                            nextTimeMillis = cacheInputStream.readLong();
                        }
                    } catch (IOException e) {
                        if (cacheInputStream != null) {
                            try { cacheInputStream.close(); } catch (IOException ee) { }
                        }
                        throw e;
                    }
                }
            } finally {
                if (pointFeatureIterator != null) {
                    pointFeatureIterator.finish();
                }
                if (featureCount < 1 || cacheInputStream == null) {
                    if (cacheFile != null) {
                        cacheFile.delete();
                        cacheFile = null;
                    }
                    featureCount = 0;
                    nextTimeMillis = -1;
                }
            }
        }

        public Station getStation() {
            return station;
        }

        public int getFeatureCount() {
            return featureCount;
        }

        public float[] getFeatureForDate(Date date) throws IOException {
            float[] variableData = null;
            if (nextTimeMillis > 0) {
                 long requesteTimedMillis = date.getTime();
                 long delta = nextTimeMillis - requesteTimedMillis;
                 if(delta < 0) { delta = -delta; }
                 if (delta < MILLIS_PER_DAY) {
                     variableData = new float[variableCount];
                     try {
                         for (int variableIndex = 0; variableIndex < variableCount; ++variableIndex) {
                            variableData[variableIndex] = cacheInputStream.readFloat();
                         }
                         nextTimeMillis = cacheInputStream.readLong();
                     } catch (EOFException e) {
                         variableData = null;
                         nextTimeMillis = -1;
                     }
                 }
            }
            return variableData;
        }

        public void finish() {
            if (cacheInputStream != null) {
                try {  cacheInputStream.close(); } catch (IOException e) { }
                cacheInputStream = null;
            }
            if (cacheFile != null) {
                cacheFile.delete();
                cacheFile = null;
            }
            nextTimeMillis = -1;
        }
    }

    public static void main(String[] args) {


        String sfLocation =
//                "src/main/resources/Sample_Files/Shapefiles/Yahara_River_HRUs_geo_WGS84.shp"
                "/Users/tkunicki/Downloads/blob_2/blob.shp"
                ;
        String ncLocation = 
//                "cdmremote:http://internal.cida.usgs.gov/thredds/cdmremote/gsod/gsod.nc"
//                "cdmremote:http://cida-wiwsc-int-javadev1.er.usgs.gov/thredds/cdmremote/gsod/gsod.nc"
//                "cdmremote:http://igsarm-cida-javadev1.er.usgs.gov:8081/thredds/cdmremote/gsod/gsod.nc"
                "/Users/tkunicki/Downloads/GSOD/netcdf/gsod.c.uod.nc"
                ;

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

            List<VariableSimpleIF> variableList = Arrays.asList(new VariableSimpleIF[] {
                dataset.getDataVariable("min"),
                dataset.getDataVariable("max"),
                dataset.getDataVariable("prcp"),
            });

            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            df.setTimeZone(TimeZone.getTimeZone("UTC"));

            BufferedWriter writer = null;
            try {
                writer = new BufferedWriter(new FileWriter("yo_mamaX.csv"));
                StationDataCSVWriter.write(
                        featureCollection,
                        stationTimeSeriesFeatureCollection,
                        variableList,
                        new DateRange(df.parse("1999-01-01"), df.parse("1999-01-31")),
                        writer,
                        true,
                        ",");
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
