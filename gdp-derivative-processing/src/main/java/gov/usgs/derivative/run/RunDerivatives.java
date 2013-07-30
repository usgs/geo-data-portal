package gov.usgs.derivative.run;

import com.google.common.base.Joiner;
import gov.usgs.cida.gdp.coreprocessing.analysis.grid.Statistics1DWriter;
import gov.usgs.derivative.AnnualScenarioEnsembleAveragingVisitor;
import gov.usgs.derivative.CoolingDegreeDayVisitor;
import gov.usgs.derivative.DaysAbovePrecipitationThresholdVisitor;
import gov.usgs.derivative.DaysAboveTemperatureThresholdVisitor;
import gov.usgs.derivative.DaysBelowTemperatureThresholdVisitor;
import gov.usgs.derivative.GrowingDegreeDayVisitor;
import gov.usgs.derivative.GrowingSeasonLengthVisitor;
import gov.usgs.derivative.HeatingDegreeDayVisitor;
import gov.usgs.derivative.IntervalTimeStepAveragingVisitor;
import gov.usgs.derivative.RepeatingPeriodTimeStepAveragingVisitor;
import gov.usgs.derivative.RunAboveTemperatureThresholdVisitor;
import gov.usgs.derivative.RunBelowPrecipitationThresholdVisitor;
import gov.usgs.derivative.TimeStepDeltaVisitor;
import gov.usgs.derivative.grid.GridTraverser;
import gov.usgs.derivative.grid.GridVisitor;
import gov.usgs.derivative.run.DerivativeOptions.VariableType;
import gov.usgs.derivative.spatial.DerivativeFeatureCoverageWeightedGridStatistics;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.SchemaException;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.dataset.CoordinateAxis1D;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDataset;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.ft.FeatureDataset;
import ucar.nc2.ft.FeatureDatasetFactoryManager;

/**
 *
 * @author Jordan Walker <jiwalker@usgs.gov>
 */
public class RunDerivatives {

    public final static Logger LOGGER = LoggerFactory.getLogger(RunDerivatives.class);

    public static void main(String[] args) {
        DerivativeOptions options = new DerivativeOptions();
        CmdLineParser parser = new CmdLineParser(options);
        try {
            parser.parseArgument(args);
            if (options.help) {
                parser.printUsage(System.out);
            }
            if (options.process != null) {
                switch (options.process) {
                    case P1D:
                        if (null == options.datasetLocation || null == options.outputDir
                                || null == options.precipVar || null == options.tminVar
                                || null == options.tmaxVar) {
                            throw new CmdLineException(parser, "Required attributes not provided for this process");
                        }
                        calculateP1DDerivatives(options);
                        break;
                    case P1M:
                        //calculateP1MAverage(options);
                        //break;
                    case P1Y:
                        //calculateP1YDerivativeEnsembleAverage(options);
                        //break;
                    case P1Y30D:
                        //calculateP1YAverageOverP30Y(options);
                        //break;
                    case P30Y:
                        //calculateP30YDerivatives(options);
                        //break;
                    case SPATIAL:
                        throw new UnsupportedOperationException("not yet implemented");
                        //calculateDerivativeFeatureWeightedGridStatistics(options);
                        //break;
                    default:
                        throw new CmdLineException(parser, "Unable to determine process type");
                }
            }
        } catch (CmdLineException ex) {
            System.err.println(ex.getMessage());
            ex.getParser().printUsage(System.err);
        } catch (Exception ex) {
            LOGGER.debug(ex.getMessage(), ex);
            System.err.println(ex.getMessage());
        }
    }

    // #########################################################################
    // From DerivativeAnalysisTest.java
    // #########################################################################
    public static void calculateP1DDerivatives(DerivativeOptions options) throws IOException {

        String dsName = options.datasetLocation;
        Map<VariableType, String> dsVariableMap = new HashMap<VariableType, String>();
        dsVariableMap.put(VariableType.PRECIP, options.precipVar);
        dsVariableMap.put(VariableType.T_MIN, options.tminVar);
        dsVariableMap.put(VariableType.T_MAX, options.tmaxVar);

        FeatureDataset fds = null;
        try {
            fds = FeatureDatasetFactoryManager.open(
                    FeatureType.GRID,
                    dsName,
                    null,
                    new Formatter(System.err));
            if (fds instanceof GridDataset) {
                GridDataset gds = (GridDataset) fds;
                List<GridDatatype> gdtl = gds.getGrids();
                for (final GridDatatype gdt : gdtl) {
                    try {
                        {
                            if (gdt.getName().endsWith(dsVariableMap.get(VariableType.PRECIP))) {
                                LOGGER.debug("running " + gdt.getName());
                                GridTraverser t = new GridTraverser(gdt);
                                t.traverse(Arrays.asList(new GridVisitor[]{
                                    new DaysAbovePrecipitationThresholdVisitor(options.datasetLocation),
                                    new RunBelowPrecipitationThresholdVisitor(options.datasetLocation)
                                }));
                            }
                            if (gdt.getName().endsWith(dsVariableMap.get(VariableType.T_MAX))) {
                                LOGGER.debug("running " + gdt.getName());
                                GridTraverser t = new GridTraverser(gdt);
                                t.traverse(Arrays.asList(new GridVisitor[]{
                                    new DaysAboveTemperatureThresholdVisitor(options.datasetLocation),
                                    new RunAboveTemperatureThresholdVisitor(options.datasetLocation)
                                }));
                            }
                            if (gdt.getName().endsWith(dsVariableMap.get(VariableType.T_MIN))) {
                                LOGGER.debug("running " + gdt.getName());
                                GridTraverser t = new GridTraverser(gdt);
                                t.traverse(Arrays.asList(new GridVisitor[]{
                                    new DaysBelowTemperatureThresholdVisitor(options.datasetLocation),
                                    new GrowingSeasonLengthVisitor(options.datasetLocation)
                                }));
                            }
                        }
                    } catch (Exception e) {
                        LOGGER.error("Failed to calculate derivatives", e);
                    }
                }
                Set<String> gsNameSet = new LinkedHashSet<String>();
                for (GridDatatype gdt : gdtl) {
                    String gridName = gdt.getName();
                    String gsName = gridName.substring(0, gridName.lastIndexOf("_"));
                    gsNameSet.add(gsName);
                }
                for (String gsName : gsNameSet) {
                    GridDatatype tMin = gds.findGridDatatype(gsName + "_" + dsVariableMap.get(VariableType.T_MIN));
                    GridDatatype tMax = gds.findGridDatatype(gsName + "_" + dsVariableMap.get(VariableType.T_MAX));
                    GridTraverser t = new GridTraverser(Arrays.asList(new GridDatatype[]{
                        tMin,
                        tMax
                    }));

                    if (options.lowMemory) {
                        System.out.println("GCM/Scenario " + gsName + " P1Y HDD");
                        t.traverse(Arrays.asList(new GridVisitor[]{
                            new HeatingDegreeDayVisitor(options.datasetLocation)
                        }));
                        System.out.println("GCM/Scenario " + gsName + " P1Y CDD");
                        t.traverse(Arrays.asList(new GridVisitor[]{
                            new CoolingDegreeDayVisitor(options.datasetLocation)
                        }));
                        System.out.println("GCM/Scenario " + gsName + " P1Y GDD");
                        t.traverse(Arrays.asList(new GridVisitor[]{
                            new GrowingDegreeDayVisitor(options.datasetLocation)
                        }));
                    } else {
                        LOGGER.debug("GCM/Scenario " + gsName + " P1Y HDD");
                        LOGGER.debug("GCM/Scenario " + gsName + " P1Y CDD");
                        LOGGER.debug("GCM/Scenario " + gsName + " P1Y GDD");
                        t.traverse(Arrays.asList(new GridVisitor[]{
                            new HeatingDegreeDayVisitor(options.datasetLocation),
                            new CoolingDegreeDayVisitor(options.datasetLocation),
                            new GrowingDegreeDayVisitor(options.datasetLocation)
                        }));
                    }
                }
            }

        } catch (Exception e) {
            LOGGER.error("Failed to calculate derivatives", e);
        } finally {
            if (fds != null) {
                fds.close();
            }
        }

    }

    public static void calculateP1YDerivativeEnsembleAverage(DerivativeOptions options) throws IOException {
        FeatureDataset fds = null;

        List<String> gridP1YList = Arrays.asList(
                new String[]{
            "/Users/tkunicki/Downloads/derivatives/derivative-days_above_threshold.pr.ncml",
            "/Users/tkunicki/Downloads/derivatives/derivative-days_above_threshold.tmax.ncml",
            "/Users/tkunicki/Downloads/derivatives/derivative-days_below_threshold.tmin.ncml",
            "/Users/tkunicki/Downloads/derivatives/derivative-spell_length_above_threshold.tmax.ncml",
            "/Users/tkunicki/Downloads/derivatives/derivative-spell_length_below_threshold.pr.ncml",
            "/Users/tkunicki/Downloads/derivatives/derivative-heating_degree_days.ncml",
            "/Users/tkunicki/Downloads/derivatives/derivative-cooling_degree_days.ncml",
            "/Users/tkunicki/Downloads/derivatives/derivative-growing_degree_days.ncml",
            "/Users/tkunicki/Downloads/derivatives/derivative-growing_season_length.ncml",});

        for (String gridP1Y : gridP1YList) {
            try {

                fds = FeatureDatasetFactoryManager.open(
                        FeatureType.GRID,
                        gridP1Y,
                        null,
                        new Formatter(System.err));
                if (fds instanceof GridDataset) {
                    GridDataset gds = (GridDataset) fds;
                    List<GridDatatype> gdtl = gds.getGrids();


                    List<GridDatatype> a1bList = new ArrayList<GridDatatype>();
                    List<GridDatatype> a1fiList = new ArrayList<GridDatatype>();
                    List<GridDatatype> a2List = new ArrayList<GridDatatype>();
                    List<GridDatatype> b1List = new ArrayList<GridDatatype>();
                    for (GridDatatype gdt : gdtl) {
                        String name = gdt.getName();
                        if (!name.contains("ensemble")) {
                            if (name.contains("a1b")) {
                                a1bList.add(gdt);
                            }
                            if (name.contains("a1fi")) {
                                a1fiList.add(gdt);
                            }
                            if (name.contains("a2")) {
                                a2List.add(gdt);
                            }
                            if (name.contains("b1")) {
                                b1List.add(gdt);
                            }
                        }
                    }
                    {
                        GridTraverser t = new GridTraverser(a1bList);
                        GridVisitor v = new AnnualScenarioEnsembleAveragingVisitor("a1b");
                        t.traverse(v);
                    }
                    {
                        GridTraverser t = new GridTraverser(a1fiList);
                        GridVisitor v = new AnnualScenarioEnsembleAveragingVisitor("a1fi");
                        t.traverse(v);
                    }
                    {
                        GridTraverser t = new GridTraverser(a2List);
                        GridVisitor v = new AnnualScenarioEnsembleAveragingVisitor("a2");
                        t.traverse(v);
                    }
                    {
                        GridTraverser t = new GridTraverser(b1List);
                        GridVisitor v = new AnnualScenarioEnsembleAveragingVisitor("b1");
                        t.traverse(v);
                    }
                }

            } finally {
                if (fds != null) {
                    fds.close();
                }
            }
        }
    }

    public static void calculateP1YAverageOverP30Y(DerivativeOptions options) throws IOException {
        FeatureDataset fds = null;

        List<String> gridP1YList = Arrays.asList(
                new String[]{
            "/Users/tkunicki/Downloads/derivatives/derivative-days_above_threshold.pr.ncml",
            "/Users/tkunicki/Downloads/derivatives/derivative-days_above_threshold.tmax.ncml",
            "/Users/tkunicki/Downloads/derivatives/derivative-days_below_threshold.tmin.ncml",
            "/Users/tkunicki/Downloads/derivatives/derivative-spell_length_above_threshold.tmax.ncml",
            "/Users/tkunicki/Downloads/derivatives/derivative-spell_length_below_threshold.pr.ncml",
            "/Users/tkunicki/Downloads/derivatives/derivative-heating_degree_days.ncml",
            "/Users/tkunicki/Downloads/derivatives/derivative-cooling_degree_days.ncml",
            "/Users/tkunicki/Downloads/derivatives/derivative-growing_degree_days.ncml",
            "/Users/tkunicki/Downloads/derivatives/derivative-growing_season_length.ncml",});

        for (String gridP1Y : gridP1YList) {
            try {

                fds = FeatureDatasetFactoryManager.open(
                        FeatureType.GRID,
                        gridP1Y,
                        null,
                        new Formatter(System.err));
                if (fds instanceof GridDataset) {
                    GridDataset gds = (GridDataset) fds;
                    List<GridDatatype> gdtl = gds.getGrids();
                    for (GridDatatype gdt : gdtl) {
                        System.out.println("running " + gdt.getName());
                        GridTraverser t = new GridTraverser(gdt);
                        GridVisitor v = new IntervalTimeStepAveragingVisitor(
                                Arrays.asList(new Interval[]{
                            new Interval("1961-01-01TZ/1991-01-01TZ"),
                            new Interval("2011-01-01TZ/2041-01-01TZ"),
                            new Interval("2041-01-01TZ/2071-01-01TZ"),
                            new Interval("2071-01-01TZ/2100-01-01TZ"),}));
                        t.traverse(v);
                    }
                }

            } finally {
                if (fds != null) {
                    fds.close();
                }
            }
        }
    }

    public static void calculateP30YDerivatives(DerivativeOptions options) throws IOException {
        FeatureDataset fds = null;

        List<String> gridP30YList = Arrays.asList(
                new String[]{
            "/Users/tkunicki/Downloads/derivatives/derivative-days_above_threshold.pr.P30Y.ncml",
            "/Users/tkunicki/Downloads/derivatives/derivative-days_above_threshold.tmax.P30Y.ncml",
            "/Users/tkunicki/Downloads/derivatives/derivative-days_below_threshold.tmin.P30Y.ncml",
            "/Users/tkunicki/Downloads/derivatives/derivative-spell_length_above_threshold.tmax.P30Y.ncml",
            "/Users/tkunicki/Downloads/derivatives/derivative-spell_length_below_threshold.pr.P30Y.ncml",
            "/Users/tkunicki/Downloads/derivatives/derivative-heating_degree_days.P30Y.ncml",
            "/Users/tkunicki/Downloads/derivatives/derivative-cooling_degree_days.P30Y.ncml",
            "/Users/tkunicki/Downloads/derivatives/derivative-growing_degree_days.P30Y.ncml",
            "/Users/tkunicki/Downloads/derivatives/derivative-growing_season_length.P30Y.ncml",});

        for (String gridP30Y : gridP30YList) {
            try {

                fds = FeatureDatasetFactoryManager.open(
                        FeatureType.GRID,
                        gridP30Y,
                        null,
                        new Formatter(System.err));
                if (fds instanceof GridDataset) {
                    GridDataset gds = (GridDataset) fds;
                    List<GridDatatype> gdtl = gds.getGrids();
                    for (GridDatatype gdt : gdtl) {
                        System.out.println("running " + gdt.getName());
                        GridTraverser t = new GridTraverser(gdt);
                        GridVisitor v = new TimeStepDeltaVisitor();
                        t.traverse(v);
                    }
                }

            } finally {
                if (fds != null) {
                    fds.close();
                }
            }
        }
    }

    // #########################################################################
    // End of DerivativeAnalysisTest methods
    // #########################################################################
    // #########################################################################
    // From SpatialDerivativeAnalysisTest.java
    // #########################################################################
    public static void calculateDerivativeFeatureWeightedGridStatistics(DerivativeOptions options) throws FactoryException, TransformException, SchemaException {

        File spatialDirectory = new File("/Users/tkunicki/Downloads/derivatives/spatial");
        if (!spatialDirectory.exists()) {
            spatialDirectory.mkdirs();
            if (!spatialDirectory.exists()) {
                throw new RuntimeException("Unable to create spatial data directory: " + spatialDirectory.getPath());
            }
            LOGGER.debug("created spatial data directory {}", spatialDirectory.getPath());
        }

        Map<String, String> shapefileMap = new LinkedHashMap<String, String>();
        shapefileMap.put("file:///Users/tkunicki/Downloads/derivatives/Shapefiles/CONUS_States.shp", "STATE");
        shapefileMap.put("file:///Users/tkunicki/Downloads/derivatives/Shapefiles/US_Counties.shp", "FIPS");
        shapefileMap.put("file:///Users/tkunicki/Downloads/derivatives/Shapefiles/Level_III_Ecoregions.shp", "LEVEL3_NAM");
        shapefileMap.put("file:///Users/tkunicki/Downloads/derivatives/Shapefiles/wbdhu8_alb_simp.shp", "HUC_8");
        shapefileMap.put("file:///Users/tkunicki/Downloads/derivatives/Shapefiles/FWS_LCC.shp", "area_names");
        shapefileMap.put("file:///Users/tkunicki/Downloads/derivatives/Shapefiles/NCA_Regions.shp", "NCA_Region");

        for (Map.Entry<String, String> shapefileEntry : shapefileMap.entrySet()) {
            String shapefile = shapefileEntry.getKey();
            String shapefileAttribute = shapefileEntry.getValue();
            ShapefileDataStore f = null;
            try {
                f = new ShapefileDataStore(new URL(shapefile));
            } catch (MalformedURLException e) {
                LOGGER.error("unable to open shapefile: {}", shapefile, e);
                continue;
            }
            if (f == null) {
                LOGGER.error("unable to open shapefile: {}", shapefile);
                continue;
            }

            FeatureCollection<SimpleFeatureType, SimpleFeature> fc = null;
            try {
                fc = f.getFeatureSource().getFeatures();
            } catch (IOException e) {
                LOGGER.error("unable to extract feature collection: {}", shapefile);
                continue;
            }

            String shapeFileBaseName = (new File(shapefile)).getName().replaceAll(".shp", "");

            List<String> gridP1YList = Arrays.asList(
                    new String[]{
                "/Users/tkunicki/Downloads/derivatives/derivative-days_above_threshold.pr.ncml",
                "/Users/tkunicki/Downloads/derivatives/derivative-days_above_threshold.tmax.ncml",
                "/Users/tkunicki/Downloads/derivatives/derivative-days_below_threshold.tmin.ncml",
                "/Users/tkunicki/Downloads/derivatives/derivative-spell_length_above_threshold.tmax.ncml",
                "/Users/tkunicki/Downloads/derivatives/derivative-spell_length_below_threshold.pr.ncml",
                "/Users/tkunicki/Downloads/derivatives/derivative-heating_degree_days.ncml",
                "/Users/tkunicki/Downloads/derivatives/derivative-cooling_degree_days.ncml",
                "/Users/tkunicki/Downloads/derivatives/derivative-growing_degree_days.ncml",
                "/Users/tkunicki/Downloads/derivatives/derivative-growing_season_length.ncml",});

            File shapefileDirectory = new File(spatialDirectory, shapeFileBaseName);
            if (!shapefileDirectory.exists()) {
                shapefileDirectory.mkdirs();
                if (!shapefileDirectory.exists()) {
                    throw new RuntimeException("Unable to create shapefile data directory: " + shapefileDirectory.getPath());
                }
                LOGGER.debug("created shapefile data directory {}", shapefileDirectory.getPath());
            }

            for (String dataset : gridP1YList) {
                try {
                    FeatureDataset fd = null;
                    try {
                        fd = FeatureDatasetFactoryManager.open(
                                FeatureType.GRID,
                                dataset,
                                null,
                                new Formatter(System.out));
                    } catch (IOException e) {
                        LOGGER.error("error opening feature dataset: {}", dataset, e);
                        continue;
                    }
                    if (fd == null) {
                        LOGGER.error("error opening feature dataset: {}", dataset);
                        continue;
                    }
                    if (!(fd instanceof GridDataset)) {
                        LOGGER.error("feature dataset not instance of grid: {}", dataset);
                        continue;
                    }

                    GridDataset gd = (GridDataset) fd;

                    try {

                        for (GridDatatype gdt : gd.getGrids()) {
                            GridCoordSystem gcs = gdt.getCoordinateSystem();
                            CoordinateAxis1D zAxis = gcs.getVerticalAxis();
                            Range zRange = zAxis.getRanges().get(0);



                            for (int zIndex = zRange.first(); zIndex <= zRange.last(); zIndex += zRange.stride()) {
                                double zValue = zAxis.getCoordValue(zIndex);

                                File outputFile = new File(
                                        shapefileDirectory,
                                        Joiner.on(",").join(gdt.getName(), zValue, "dsg") + ".nc");
                                String outputFileName = outputFile.getPath();

                                LOGGER.debug("Generating {} ", outputFileName);

                                GridDatatype zgdt = gdt.makeSubset(null, new Range(zIndex, zIndex), null, 1, 1, 1);

                                DerivativeFeatureCoverageWeightedGridStatistics.execute(
                                        fc,
                                        shapefileAttribute,
                                        zgdt,
                                        null,
                                        Arrays.asList(new Statistics1DWriter.Statistic[]{Statistics1DWriter.Statistic.MEAN}),
                                        false,
                                        outputFile);
                            }
                        }
                    } catch (IOException e) {
                        LOGGER.error(String.format("error creating output files for grid: %s, %s", dataset, shapefile), e);
                    } catch (InvalidRangeException e) {
                        LOGGER.error(String.format("error creating output files for grid: %s, %s", dataset, shapefile), e);
                    }

                    fd.close();
                } catch (IOException e) {
                    LOGGER.error("error closing feature dataset: {}", dataset, e);
                }
            }
            f.dispose();
        }
    }

    // #########################################################################
    // End of SpatialDerivativeAnalysisTest.java
    // #########################################################################
    // #########################################################################
    // From TimeStepAveragingTest.java
    // #########################################################################
    public static void calculateP1MAverage(DerivativeOptions options) throws IOException {
        FeatureDataset fds = null;

        List<String> gridFileList = Arrays.asList(
                new String[]{
            "/Users/tkunicki/Data/thredds/misc/markstro_grid/union.ncml"
        });

        for (String gridFile : gridFileList) {
            try {

                fds = FeatureDatasetFactoryManager.open(
                        FeatureType.GRID,
                        gridFile,
                        null,
                        new Formatter(System.err));
                if (fds instanceof GridDataset) {
                    GridDataset gds = (GridDataset) fds;
                    List<GridDatatype> gdtl = gds.getGrids();
                    for (GridDatatype gdt : gdtl) {
                        if (gdt.getName().startsWith("x")) {
                            System.out.println("running " + gdt.getName());
                            GridTraverser t = new GridTraverser(gdt);
                            GridVisitor v = new RepeatingPeriodTimeStepAveragingVisitor(Period.months(1));
                            t.traverse(v);
                        }
                    }
                }

            } finally {
                if (fds != null) {
                    fds.close();
                }
            }
        }
    }
    // #########################################################################
    // End of TimeStepAveragingTest.java
    // #########################################################################
}
