/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.usgs.gdp.analysis;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import gov.usgs.gdp.analysis.statistics.StatisticsAccumulator1D;
import gov.usgs.gdp.servlet.FileProcessServlet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDataset;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.ft.FeatureDataset;
import ucar.nc2.ft.FeatureDatasetFactoryManager;
import ucar.unidata.geoloc.LatLonPointImpl;
import ucar.unidata.geoloc.LatLonRect;

/**
 *
 * @author tkunicki
 */
public class SimpleStatistics {

    public static StatisticsAccumulator1D calculate(
            SimpleFeature simpleFeature,
            FeatureDataset featureDataset,
            String variableName,
            Range timeRange) throws IOException {

        Geometry sfg = (Geometry)simpleFeature.getDefaultGeometry();
        Envelope sfge = sfg.getEnvelopeInternal();
        LatLonRect llr = new LatLonRect(
                new LatLonPointImpl(sfge.getMinY(), sfge.getMinX()),
                new LatLonPointImpl(sfge.getMaxY(), sfge.getMaxX()));

        GridDataset gd = (GridDataset)featureDataset;

        GridDatatype gdt = gd.findGridDatatype(variableName);
        if(gdt != null) {
            try {
                gdt = gdt.makeSubset(timeRange, null, llr, 1, 1, 1);
                System.out.println(gdt);
            } catch (InvalidRangeException ex) {
                Logger.getLogger(SimpleStatistics.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        GridCoverage gridCoverage = new GridCoverage(simpleFeature, gd, variableName);
        double[][] coverage = gridCoverage.getCoverage();

        StatisticsAccumulator1D coveredStats = new StatisticsAccumulator1D();
        StatisticsAccumulator1D envelopeStats = new StatisticsAccumulator1D();

        ucar.ma2.Array array = gdt.readVolumeData(-1);
        GridCoordSystem gcs = gdt.getCoordinateSystem();
        dumpGridBB(gcs);
        int tCount = (int) gcs.getTimeAxis().getSize();
        int yCount = (int) gcs.getYHorizAxis().getSize();
        int xCount = (int) gcs.getXHorizAxis().getSize();
        for (int tIndex = 0; tIndex < tCount; ++tIndex) {
            int tOffset = tIndex * yCount * xCount;
            for (int yIndex = 0; yIndex < yCount; ++yIndex) {
                int tyOffset = tOffset + (yIndex * xCount);
                for (int xIndex = 0; xIndex < xCount; ++xIndex) {
                    int index = tyOffset + xIndex;
                    double value = array.getDouble(index);
                    if(coverage[xIndex][yIndex] > 0d) {
                        coveredStats.accumulate(value);
                    }
                    envelopeStats.accumulate(value);
                }
            }
        }
        System.out.println("*** Covered ***");
        System.out.println(coveredStats);
        System.out.println("*** Envelope ***");
        System.out.println(envelopeStats);


        System.out.println(" geotools feature bounds "
                + sfge.getMinX() + ":" + sfge.getMinY()
                + sfge.getMaxX() + ":" + sfge.getMaxY());

        return coveredStats;
    }
    
    public static List<String> getStatisticsList(SimpleFeature simpleFeature,
            FeatureDataset featureDataset,
            String variableName,
            Range timeRange) throws IOException {
    	
    	List<String> result = new ArrayList<String>();

        Geometry sfg = (Geometry)simpleFeature.getDefaultGeometry();
        Envelope sfge = sfg.getEnvelopeInternal();
        LatLonRect llr = new LatLonRect(
                new LatLonPointImpl(sfge.getMinY(), sfge.getMinX()),
                new LatLonPointImpl(sfge.getMaxY(), sfge.getMaxX()));

        GridDataset gd = (GridDataset)featureDataset;

        GridDatatype gdt = gd.findGridDatatype(variableName);
        if(gdt != null) {
            try {
                gdt = gdt.makeSubset(timeRange, null, llr, 1, 1, 1);
                result.add(gdt.toString());
            } catch (InvalidRangeException ex) {
                Logger.getLogger(SimpleStatistics.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        StatisticsAccumulator1D s = new StatisticsAccumulator1D();
        ucar.ma2.Array array = gdt.readVolumeData(-1);
        GridCoordSystem gcs = gdt.getCoordinateSystem();
        dumpGridBB(gcs);
        while(array.hasNext()) {
            double value = array.nextDouble();
            // NaN / missing value check
            if (value == value) {
                s.accumulate(value);
            }
        }

        result.add(s.toString());
        result.add(" geotools feature bounds "
                + sfge.getMinX() + ":" + sfge.getMinY()
                + sfge.getMaxX() + ":" + sfge.getMaxY());
    	result.addAll(dumpGridBBToList(gcs));
    	return result;
    	
    }

    public static List<String> dumpGridBBToList(GridCoordSystem gcs) {
    	 List<String> result = new ArrayList<String>();
    	 
    	 result.add("netcdf feature bounds");
    	 result.add("  X min " + gcs.getXHorizAxis().getMinValue());
    	 result.add("  X max " + gcs.getXHorizAxis().getMaxValue());
    	 result.add("  Y min " + gcs.getYHorizAxis().getMinValue());
    	 result.add("  Y max " + gcs.getYHorizAxis().getMaxValue());
    	 result.add("  T min " + gcs.getTimeAxis().getMinValue());
    	 result.add("  T max " + gcs.getTimeAxis().getMaxValue());
    	 result.add("  D min " + gcs.getDateRange().getStart().toDateTimeStringISO());
    	 result.add("  D max " + gcs.getDateRange().getEnd().toDateTimeStringISO());
    	 return result;
    }
    
    public static void dumpGridBB(GridCoordSystem gcs) {
        StringBuffer sb = new StringBuffer("netcdf feature bounds").append('\n');
        sb.append("  X min ").append(gcs.getXHorizAxis().getMinValue()).append('\n');
        sb.append("  X max ").append(gcs.getXHorizAxis().getMaxValue()).append('\n');
        sb.append("  Y min ").append(gcs.getYHorizAxis().getMinValue()).append('\n');
        sb.append("  Y max ").append(gcs.getYHorizAxis().getMaxValue()).append('\n');
        sb.append("  T min ").append(gcs.getTimeAxis().getMinValue()).append('\n');
        sb.append("  T max ").append(gcs.getTimeAxis().getMaxValue()).append('\n');
        sb.append("  D min ").append(gcs.getDateRange().getStart().toDateTimeStringISO()).append('\n');
        sb.append("  D max ").append(gcs.getDateRange().getEnd().toDateTimeStringISO()).append('\n');
        System.out.print(sb.toString());
    }
    
    public static void main(String[] args) {

//        String ncLocation = "http://runoff.cr.usgs.gov:8086/thredds/dodsC/hydro/national/2.5arcmin";
        String ncLocation = "http://internal.cida.usgs.gov/thredds/dodsC/models/us_gfdl.A1.monthly.Tavg.1960-2099.nc";
        String sfLocation = "src/main/resources/Sample_Files/Shapefiles/Yahara_River_HRUs_geo_WGS84.shp";

        FeatureDataset dataset = null;
        try {

            dataset = FeatureDatasetFactoryManager.open(
                    null, ncLocation, null, new Formatter());

            FileDataStore shapeFileDataStore = FileDataStoreFinder.getDataStore(new File(sfLocation));
            FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = shapeFileDataStore.getFeatureSource();
            String attributeType = "GRIDCODE";
            String attributeValue = "10";
            Filter attributeFilter = CQL.toFilter(attributeType + " = '" + attributeValue + "'");
            FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection =
                    featureSource.getFeatures(attributeFilter);
//            featureCollection = featureSource.getFeatures();
            Iterator<SimpleFeature> filteredFeatureIterator =
                    featureCollection.iterator();
            while (filteredFeatureIterator.hasNext()) {
                SimpleFeature sf = filteredFeatureIterator.next();
                calculate(sf, dataset, "Tavg", new Range(0,100));
            }

        } catch (Exception ex) {
            Logger.getLogger(SimpleStatistics.class.getName()).log(Level.SEVERE, null, ex);
        }finally {
            if (dataset != null) {
                try {
                dataset.close();
                } catch (IOException ex) {
                    Logger.getLogger(SimpleStatistics.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

}
