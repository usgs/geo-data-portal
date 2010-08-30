package gov.usgs.cida.gdp.coreprocessing.writer;

import gov.usgs.cida.gdp.coreprocessing.DelimiterOption;
import gov.usgs.cida.gdp.coreprocessing.GroupBy;
import gov.usgs.cida.gdp.coreprocessing.GroupBy.StationOption;
import gov.usgs.cida.gdp.coreprocessing.analysis.StationDataCSVWriter;
import gov.usgs.cida.gdp.coreprocessing.analysis.grid.FeatureCategoricalGridCoverage;
import gov.usgs.cida.gdp.coreprocessing.analysis.grid.FeatureCoverageWeightedGridStatistics;
import gov.usgs.cida.gdp.coreprocessing.analysis.grid.FeatureCoverageWeightedGridStatisticsWriter.Statistic;
import gov.usgs.cida.gdp.coreprocessing.servlet.ProcessServlet;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.geotools.feature.FeatureCollection;
import org.geotools.feature.SchemaException;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import org.slf4j.LoggerFactory;

import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import ucar.nc2.VariableSimpleIF;
import ucar.nc2.dataset.CoordinateAxis1DTime;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.dt.grid.GridDataset;
import ucar.nc2.ft.FeatureDataset;
import ucar.nc2.ft.FeatureDatasetPoint;
import ucar.nc2.ft.StationTimeSeriesFeatureCollection;
import ucar.nc2.units.DateRange;

public class CSVWriter {
	
	public static boolean station(
            FeatureDataset featureDataset,
            FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection,
            Date fromDate, Date toDate, DelimiterOption delimiterOption,
            String[] dataTypes, String groupById, String outputFile)
            throws FactoryException, SchemaException,
            org.opengis.coverage.grid.InvalidRangeException,
            TransformException, IOException {

        FeatureDatasetPoint fdp = (FeatureDatasetPoint) featureDataset;
        List<ucar.nc2.ft.FeatureCollection> fcl = fdp.getPointFeatureCollectionList();
        if (fcl != null && fcl.size() == 1) {
            ucar.nc2.ft.FeatureCollection fc = fcl.get(0);
            if (fc != null && fc instanceof StationTimeSeriesFeatureCollection) {
                StationTimeSeriesFeatureCollection stsfc = (StationTimeSeriesFeatureCollection) fc;
                List<VariableSimpleIF> variableList = new ArrayList<VariableSimpleIF>();
                for (String variableName : dataTypes) {
                    VariableSimpleIF variable = featureDataset.getDataVariable(variableName);
                    if (variable != null) {
                        variableList.add(variable);
                    } else {
                        // do we care?
                    }
                }
                GroupBy.StationOption groupBy = null;
                if (groupById != null) {
                    try {
                        groupBy = GroupBy.StationOption.valueOf(groupById);
                    } catch (IllegalArgumentException e) {
                        /* failure handled below */
                    }
                }
                if (groupBy == null) {
                    groupBy = GroupBy.StationOption.getDefault();
                }
                BufferedWriter writer = null;
                try {
                    writer = new BufferedWriter(new FileWriter(new File(
                            System.getProperty("applicationWorkDir"),
                            outputFile)));
                    StationDataCSVWriter.write(featureCollection, stsfc,
                            variableList, new DateRange(fromDate, toDate),
                            writer, groupBy == StationOption.variable,
                            delimiterOption.delimiter);
                } finally {
                    if (writer != null) {
                        try {
                            writer.close();
                            return true;
                        } catch (IOException e) {
                            return true;
                        }
                    }
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
		return true;
    }
	
	public static boolean grid(
            FeatureDataset featureDataset,
            boolean categorical,
            FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection,
            String attributeName, DelimiterOption delimiterOption,
            Date fromDate, Date toDate, String[] dataTypes, String groupById,
            String[] outputStats, String outputFile) throws IOException,
            SchemaException, TransformException, IllegalArgumentException,
            FactoryException, InvalidRangeException {

        GridDataset gridDataset = (GridDataset) featureDataset;
        String gridName = dataTypes[0];
        GridDatatype gdt = gridDataset.findGridByName(gridName);
        categorical = gdt.getDataType().isIntegral();
        if (categorical) {
            BufferedWriter writer = null;
            try {
                writer = new BufferedWriter(new FileWriter(new File(
                        System.getProperty("applicationWorkDir"), outputFile)));
                // *** long running task ***
                FeatureCategoricalGridCoverage.execute(featureCollection,
                        attributeName, gridDataset, gridName, writer,
                        delimiterOption.delimiter);
            } finally {
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (IOException e) {
                        /* get bent */
                    }
                }
            }
        } else {
            try {
                Range timeRange = null;
                try {
                    CoordinateAxis1DTime timeAxis = gdt.getCoordinateSystem().getTimeAxis1D();
                    int timeIndexMin = 0;
                    int timeIndexMax = 0;
                    if (fromDate != null && toDate != null) {
                        timeIndexMin = timeAxis.findTimeIndexFromDate(fromDate);
                        timeIndexMax = timeAxis.findTimeIndexFromDate(toDate);
                        timeRange = new Range(timeIndexMin, timeIndexMax);
                    }
                } catch (NumberFormatException e) {
                    LoggerFactory.getLogger(ProcessServlet.class).error(
                            e.getMessage());
                } catch (InvalidRangeException e) {
                    LoggerFactory.getLogger(ProcessServlet.class).error(
                            e.getMessage());
                }
                GroupBy.GridOption groupBy = null;
                if (groupById != null) {
                    try {
                        groupBy = GroupBy.GridOption.valueOf(groupById);
                    } catch (IllegalArgumentException e) {
                        /* failure handled below */
                    }
                }
                if (groupBy == null) {
                    groupBy = GroupBy.GridOption.getDefault();
                }
                List<Statistic> statisticList = new ArrayList<Statistic>();
                if (outputStats != null && outputStats.length > 0) {
                    for (int i = 0; i < outputStats.length; ++i) {
                        // may throw exception if outputStats value doesn't
                        // map to Statistic enum value, ivan says let percolate
                        // up.
                        statisticList.add(Statistic.valueOf(outputStats[i]));
                    }
                }
                if (statisticList.isEmpty()) {
                    throw new IllegalArgumentException(
                            "no output statistics selected");
                }
                BufferedWriter writer = null;
                try {
                    writer = new BufferedWriter(new FileWriter(new File(
                            System.getProperty("applicationWorkDir"),
                            outputFile)));
                    // *** long running task ***
                    FeatureCoverageWeightedGridStatistics.execute(
                            featureCollection, attributeName, gridDataset,
                            gridName, timeRange, statisticList, writer,
                            groupBy == GroupBy.GridOption.statistics,
                            delimiterOption.delimiter);
                } finally {
                    if (writer != null) {
                        try {
                            writer.close();
                            return true;
                        } catch (IOException e) {
                        	return false;
                        }
                    }
                }
            } finally {
                try {
                    if (gridDataset != null) {
                        gridDataset.close();
                    }
                } catch (IOException e) {
                    
                }
            }
        }
		return true;
    }
}
