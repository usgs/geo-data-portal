package gov.usgs.cida.gdp.coreprocessing.writer;

import gov.usgs.cida.gdp.coreprocessing.Delimiter;
import gov.usgs.cida.gdp.coreprocessing.GroupBy;
import gov.usgs.cida.gdp.coreprocessing.GroupBy.StationOption;
import gov.usgs.cida.gdp.coreprocessing.analysis.StationDataCSVWriter;

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

import ucar.nc2.VariableSimpleIF;
import ucar.nc2.ft.FeatureDataset;
import ucar.nc2.ft.FeatureDatasetPoint;
import ucar.nc2.ft.StationTimeSeriesFeatureCollection;
import ucar.nc2.units.DateRange;

public class DelimitedWriter {
	
	public static boolean station(
            FeatureDataset featureDataset,
            FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection,
            Date fromDate, Date toDate, Delimiter delimiterOption,
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
	
}
