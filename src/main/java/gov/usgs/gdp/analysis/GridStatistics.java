package gov.usgs.gdp.analysis;

import gov.usgs.gdp.analysis.statistics.WeightedStatisticsAccumulator1D;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Formatter;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.data.FeatureSource;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;

import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDataset;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.ft.FeatureDataset;
import ucar.nc2.ft.FeatureDatasetFactoryManager;
import ucar.nc2.units.DateRange;
import ucar.unidata.geoloc.LatLonPointImpl;
import ucar.unidata.geoloc.LatLonRect;

import com.google.common.base.Preconditions;
import com.vividsolutions.jts.geom.Geometry;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.geometry.BoundingBox;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import ucar.ma2.Array;
import ucar.ma2.Index;

public class GridStatistics {

    private String attributeName;
    private List<? extends Object> attributeValues;
    
    private DateRange timeStepRange;
    private List<Date> timeStepValues;

    private String variableName;
    private String variableUnits;
    
    public Map<Date, Map<Object, WeightedStatisticsAccumulator1D>> perTimestepPerAttributeValueStatistics;
    public Map<Date, WeightedStatisticsAccumulator1D> perTimestepAllAttributeValueStatistics;
    public Map<Object, WeightedStatisticsAccumulator1D> perAttributeValueAllTimestepStatistics;
    public WeightedStatisticsAccumulator1D allTimestepAllAttributeValueStatistics;
    
    private GridStatistics() {
        
    }
    
    public String getAttributeName() {
        return attributeName;
    }
    
    public List<? extends Object> getAttributeValues() {
        return attributeValues;
    }
    
    public DateRange getTimeStepRange() {
        return timeStepRange;
    }
    
    public List<Date> getTimeStepValues() {
        return timeStepValues;
    }

    public String getVariableName() {
        return variableName;
    }

    public String getVariableUnits() {
        return variableUnits;
    }
    
    //NOTE, This API will change once we figure out usage sematics...
    public Map<Date, Map<Object, WeightedStatisticsAccumulator1D>> getPerTimestepPerAttributeValueStatistics() {
        return perTimestepPerAttributeValueStatistics;
    }
    
    public  Map<Date, WeightedStatisticsAccumulator1D> getPerTimestepAllAttributeValueStatistics() {
        return perTimestepAllAttributeValueStatistics;
    }
    
    public Map<Object, WeightedStatisticsAccumulator1D> getPerAttributeValueAllTimestepStatistics() {
        return perAttributeValueAllTimestepStatistics;
    }
    
    public WeightedStatisticsAccumulator1D getAllTimestepAllAttributeValueStatistics() {
        return allTimestepAllAttributeValueStatistics;
    }
    
    // TODO: docs and error handling
    public static GridStatistics generate(
            FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection,
            String attributeName,
            GridDataset gridDataset,
            String variableName,
            Range timeRange)
            throws IOException, InvalidRangeException, FactoryException, TransformException
    {
        
        ReferencedEnvelope envelope = featureCollection.getBounds();
        BoundingBox latLonBoundingBox = envelope.toBounds(DefaultGeographicCRS.WGS84);
        LatLonRect llr = new LatLonRect(
                new LatLonPointImpl(latLonBoundingBox.getMinY(), latLonBoundingBox.getMinX()),
                new LatLonPointImpl(latLonBoundingBox.getMaxY(), latLonBoundingBox.getMaxX()));


        GridDatatype gdt = gridDataset.findGridDatatype(variableName);
        Preconditions.checkNotNull(gdt, "Variable named %s not found in gridDataset", variableName);

        try {
            gdt = gdt.makeSubset(timeRange, null, llr, 1, 1, 1);
        } catch (InvalidRangeException ex) {
            Logger.getLogger(GridStatistics.class.getName()).log(Level.SEVERE, null, ex);
            throw ex;  // rethrow requested by IS
        }
        
        // verify attribute exists in featureCollection. 
        AttributeDescriptor attributeDescriptor = featureCollection.getSchema().getDescriptor(attributeName);
        Preconditions.checkNotNull(
                attributeDescriptor, "Attibute named %s not found in featureCollection", attributeName);

        // check attribute type binding, if possible we want to sort it's values
        AttributeType attributeType = attributeDescriptor.getType();
        boolean isAttributeValueComparable = Comparable.class.isAssignableFrom(attributeType.getBinding());
        Map<Object, Geometry> attributeValueToGeometryMap = isAttributeValueComparable ?
                new TreeMap<Object, Geometry>() :     // rely on on Comparable to sort
                new LinkedHashMap<Object, Geometry>(); // use order from featureCollection.iterator();

        Iterator<SimpleFeature> fi = featureCollection.iterator();
        while (fi.hasNext()) {
            SimpleFeature sf = fi.next();
            Object av = sf.getAttribute(attributeName);

            if (av != null) {
                Geometry g0 = (Geometry)sf.getDefaultGeometry(); 
                Geometry g1 = attributeValueToGeometryMap.get(av);
                if (g1 == null) {
                    attributeValueToGeometryMap.put(av, g0);
                } else {
                    attributeValueToGeometryMap.put(av, g1.union(g0));
                }
            } 
        }

        int attributeValueMapSize = (int) Math.ceil( (float) attributeValueToGeometryMap.size() / 0.75f);
        Map<Object, GridCellCoverage> attributeValueToCoverageMap =
                new LinkedHashMap<Object, GridCellCoverage>(attributeValueMapSize);
        
        GridCoordSystem gcs = gdt.getCoordinateSystem();
        if (gcs.getCoordinateAxes().size() != 3) {
            // FIXME: In the future, we ought to be able to handle other grid types, especially t-z-y-x and y-x.
            // Until then, we should actively reject them because types like t-z-y-x will actually run without error
            // in the code below, but will produce incomplete results (e.g. only for z=0).
            throw new IllegalArgumentException("\"" + variableName  + "\" is not a t-y-x grid.");
        }
        
        CoordinateReferenceSystem crs = featureCollection.getSchema().getCoordinateReferenceSystem();
        GridCellGeometry gcc = new GridCellGeometry(gcs);
        for(Map.Entry<Object, Geometry> entry : attributeValueToGeometryMap.entrySet()) {
            attributeValueToCoverageMap.put(entry.getKey(), new GridCellCoverage(entry.getValue(), crs, gcc));
        }

        // Theset statements will throw a NullPointerException if gdt's 3 coordinate axes are not t-y-x.
        int tCount = (int) gcs.getTimeAxis().getSize();
        int yCount = (int) gcs.getYHorizAxis().getSize();
        int xCount = (int) gcs.getXHorizAxis().getSize();
        
        GridStatistics gs = new GridStatistics();
        gs.variableName = variableName;
        gs.variableUnits = gdt.getVariable().getUnitsString();
        
        gs.attributeValues = Collections.unmodifiableList(new ArrayList<Object>(attributeValueToGeometryMap.keySet()));
        gs.timeStepValues = Collections.unmodifiableList(Arrays.asList(gcs.getTimeAxis1D().getTimeDates()));
        gs.timeStepRange = gcs.getTimeAxis1D().getDateRange();
        
        gs.allTimestepAllAttributeValueStatistics = new WeightedStatisticsAccumulator1D() ;
        
        gs.perAttributeValueAllTimestepStatistics =
                new LinkedHashMap<Object, WeightedStatisticsAccumulator1D>(attributeValueMapSize);
        for (Object attributeValue : gs.attributeValues) {
            gs.perAttributeValueAllTimestepStatistics.put(attributeValue, new WeightedStatisticsAccumulator1D());
        }
        
        gs.perTimestepPerAttributeValueStatistics = new LinkedHashMap<Date, Map<Object, WeightedStatisticsAccumulator1D>>();
        gs.perTimestepAllAttributeValueStatistics = new LinkedHashMap<Date,WeightedStatisticsAccumulator1D>();
        
        int tBase = timeRange.first();
        for (int tIndex = 0; tIndex < tCount; ++tIndex) {
            Map<Object, WeightedStatisticsAccumulator1D> timeStepAttributeValueStatisticsMap =
                    new LinkedHashMap<Object, WeightedStatisticsAccumulator1D>(attributeValueMapSize);
            for (Object attributeValue : gs.attributeValues) {
                timeStepAttributeValueStatisticsMap.put(attributeValue, new WeightedStatisticsAccumulator1D());
            }

            Array array = gdt.readVolumeData(tIndex);
            Index arrayIndex = array.getIndex();
            WeightedStatisticsAccumulator1D timeStepStatistics = new WeightedStatisticsAccumulator1D();
            
            for (int yIndex = 0; yIndex < yCount; ++yIndex) {
                for (int xIndex = 0; xIndex < xCount; ++xIndex) {
                    double cellCoverageFractionTotal = 0;
                    double value = array.getDouble(arrayIndex.set(yIndex, xIndex));

                    for (Map.Entry<Object, GridCellCoverage> entry : attributeValueToCoverageMap.entrySet()) {
                        Object av = entry.getKey();
                        GridCellCoverage gc = entry.getValue();
                        double cellCoverageFraction = gc.getCellCoverageFraction(xIndex, yIndex);

                        if(cellCoverageFraction > 0d) {
                            gs.perAttributeValueAllTimestepStatistics.get(av).accumulate(value, cellCoverageFraction);
                            timeStepAttributeValueStatisticsMap.get(av).accumulate(value, cellCoverageFraction);
                        }
                        cellCoverageFractionTotal += cellCoverageFraction;
                    }
                    
                    if (cellCoverageFractionTotal > 0) {
                        timeStepStatistics.accumulate(value, cellCoverageFractionTotal);
                        gs.allTimestepAllAttributeValueStatistics.accumulate(value, cellCoverageFractionTotal);
                    }
                }
            }
            
            Date timestep = gcs.getTimeAxis1D().getTimeDate(tBase + tIndex);
            gs.perTimestepPerAttributeValueStatistics.put(timestep, timeStepAttributeValueStatisticsMap);
            gs.perTimestepAllAttributeValueStatistics.put(timestep, timeStepStatistics);
        }
        
        return gs;
    }
    
    // SIMPLE inline testing only, need unit tests...
    public static void main(String[] args) {
//        String ncLocation = "http://runoff.cr.usgs.gov:8086/thredds/dodsC/hydro/national/2.5arcmin";
//        String ncLocation = "http://internal.cida.usgs.gov/thredds/dodsC/misc/us_gfdl.A1.monthly.Tavg.1960-2099.nc";
//        String ncLocation = "http://localhost:18080/thredds/dodsC/ncml/gridded_obs.daily.Wind.ncml";
//        String ncLocation = "/Users/tkunicki/Downloads/thredds-data/CONUS_2001-2010.ncml";
        String ncLocation = "/Users/tkunicki/Downloads/thredds-data/gridded_obs.daily.Wind.ncml";
        String sfLocation = "src/main/resources/Sample_Files/Shapefiles/serap_hru_239.shp";
    
        FeatureDataset dataset = null;
        FileDataStore dataStore = null;
        long start = System.currentTimeMillis();
        try {
            dataset = FeatureDatasetFactoryManager.open(null, ncLocation, null, new Formatter());
            dataStore = FileDataStoreFinder.getDataStore(new File(sfLocation));
            FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = dataStore.getFeatureSource();
            String attributeName = "GRID_CODE";
            FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection = featureSource.getFeatures();

            GridStatistics gs = GridStatistics.generate(
                    featureCollection, attributeName, (GridDataset)dataset, "Wind", new Range(0, 10));

            // example csv dump...
            BufferedWriter writer = null;
            try {
                writer = new BufferedWriter(new FileWriter("temp0.csv"));
                GridStatisticsCSVWriter csv = new GridStatisticsCSVWriter(
                        gs,
                        Arrays.asList(new GridStatisticsCSVWriter.Statistic[] {
                            GridStatisticsCSVWriter.Statistic.mean,
                            GridStatisticsCSVWriter.Statistic.maximum, }),
                        false,
                        ",");
                csv.write(writer);
            } catch (IOException ex) {
                Logger.getLogger(GridStatistics.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (IOException ex) {
                        Logger.getLogger(GridStatistics.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }

            try {
                writer = new BufferedWriter(new FileWriter("temp1.csv"));
                GridStatisticsCSVWriter csv = new GridStatisticsCSVWriter(
                        gs,
                        Arrays.asList(new GridStatisticsCSVWriter.Statistic[] {
                            GridStatisticsCSVWriter.Statistic.mean,
                            GridStatisticsCSVWriter.Statistic.maximum, }),
                        true,
                        ",");
                csv.write(writer);
            } catch (IOException ex) {
                Logger.getLogger(GridStatistics.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (IOException ex) {
                        Logger.getLogger(GridStatistics.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }

            try {
                writer = new BufferedWriter(new FileWriter("temp2.csv"));
                GridStatisticsCSVWriter csv = new GridStatisticsCSVWriter(
                        gs,
                        Arrays.asList(GridStatisticsCSVWriter.Statistic.values()),
                        true,
                        ",");
                csv.write(writer);
            } catch (IOException ex) {
                Logger.getLogger(GridStatistics.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (IOException ex) {
                        Logger.getLogger(GridStatistics.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }



            try {
                writer = new BufferedWriter(new FileWriter("temp3.csv"));
                GridStatisticsCSVWriter csv = new GridStatisticsCSVWriter(
                        gs,
                        Arrays.asList(GridStatisticsCSVWriter.Statistic.values()),
                        false,
                        ",");
                csv.write(writer);
            } catch (IOException ex) {
                Logger.getLogger(GridStatistics.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (IOException ex) {
                        Logger.getLogger(GridStatistics.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }

        } catch (Exception ex) {
            Logger.getLogger(GridStatistics.class.getName()).log(Level.SEVERE, null, ex);
        }finally {
            if (dataset != null) {
                try {
                    dataset.close();
                } catch (IOException ex) {
                    Logger.getLogger(GridStatistics.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if (dataStore != null) {
                dataStore.dispose();
            }
        }
        System.out.println("Completed in " + (System.currentTimeMillis() - start) + " ms.");
    }
}
