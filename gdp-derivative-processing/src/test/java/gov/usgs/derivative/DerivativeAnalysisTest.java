package gov.usgs.derivative;

import gov.usgs.derivative.grid.GridTraverser;
import gov.usgs.derivative.grid.GridVisitor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Ignore;
import org.junit.Test;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.dt.GridDataset;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.ft.FeatureDataset;
import ucar.nc2.ft.FeatureDatasetFactoryManager;

/**
 *
 * @author tkunicki
 */
public class DerivativeAnalysisTest {
    
    public enum VariableType {
        PRECIP,
        T_MIN,
        T_MAX
    };
    
    @Test
    @Ignore
    public void calculateP1DDerivatives() throws IOException {
        
        String dsName = "/Users/tkunicki/Data/thredds/gmo/GMO.ncml";
        Map<VariableType, String> dsVariableMap = new HashMap<VariableType,String>();
        dsVariableMap.put(VariableType.PRECIP, "pr");
        dsVariableMap.put(VariableType.T_MIN, "tmin");
        dsVariableMap.put(VariableType.T_MAX, "tmax");
        
        FeatureDataset fds = null;
        try {
            fds = FeatureDatasetFactoryManager.open(
                FeatureType.GRID,
                dsName,
                null,
                new Formatter(System.err));
            if (fds instanceof GridDataset) {
                GridDataset gds = (GridDataset)fds;
                List<GridDatatype> gdtl = gds.getGrids();
                for (final GridDatatype gdt : gdtl) {
                    try {
                        {
                            if (gdt.getName().endsWith(dsVariableMap.get(VariableType.PRECIP))) {
                                System.out.println("running " + gdt.getName());
                                GridTraverser t = new GridTraverser(gdt);
                                t.traverse(Arrays.asList(new GridVisitor[] {
                                    new DaysAbovePrecipitationThresholdVisitor(),
                                    new RunBelowPrecipitationThresholdVisitor()
                                }));
                            }
                            if (gdt.getName().endsWith(dsVariableMap.get(VariableType.T_MAX))) {
                                System.out.println("running " + gdt.getName());
                                GridTraverser t = new GridTraverser(gdt);
                                t.traverse(Arrays.asList(new GridVisitor[] {
                                    new DaysAboveTemperatureThresholdVisitor(),
                                    new RunAboveTemperatureThresholdVisitor()
                                }));
                            }
                            if (gdt.getName().endsWith(dsVariableMap.get(VariableType.T_MIN))) {
                                System.out.println("running " + gdt.getName());
                                GridTraverser t = new GridTraverser(gdt);
                                t.traverse(Arrays.asList(new GridVisitor[] {
                                    new DaysBelowTemperatureThresholdVisitor(),
                                    new GrowingSeasonLengthVisitor(),
                                }));
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                Set<String> gsNameSet = new LinkedHashSet<String>();
                for (GridDatatype gdt : gdtl) {
                    String gridName = gdt.getName();
                    String gsName = gridName.substring(0, gridName.lastIndexOf("_"));
                            gsNameSet.add(gsName);
                        }
                for (String gsName : gsNameSet) {
                    {
                    GridDatatype tMin = gds.findGridDatatype(gsName + "_" + dsVariableMap.get(VariableType.T_MIN));
                    GridDatatype tMax = gds.findGridDatatype(gsName + "_" + dsVariableMap.get(VariableType.T_MAX));
                    GridTraverser t = new GridTraverser(Arrays.asList(new GridDatatype[] {
                        tMin,
                        tMax
                    }));
//                    //Low memory footprint
//                    {
//                    System.out.println("GCM/Scenario " + gsName + " P1Y HDD");
//                    t.traverse(Arrays.asList(new GridVisitor[] {
//                            new HeatingDegreeDayVisitor(),
//                        }));
//                    System.out.println("GCM/Scenario " + gsName + " P1Y CDD");
//                    t.traverse(Arrays.asList(new GridVisitor[] {
//                            new CoolingDegreeDayVisitor(),
//                        }));
//                    System.out.println("GCM/Scenario " + gsName + " P1Y GDD");
//                    t.traverse(Arrays.asList(new GridVisitor[] {
//                            new GrowingDegreeDayVisitor(),
//                        }));
//                    }
                    // High memory footprint
                    {
                        System.out.println("GCM/Scenario " + gsName + " P1Y HDD");
                        System.out.println("GCM/Scenario " + gsName + " P1Y CDD");
                        System.out.println("GCM/Scenario " + gsName + " P1Y GDD");
                        t.traverse(Arrays.asList(new GridVisitor[] {
                                new HeatingDegreeDayVisitor(),
                                new CoolingDegreeDayVisitor(),
                                new GrowingDegreeDayVisitor(),
                            }));
                        }
                    }
                } 
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fds != null) fds.close();
        }

    }
    
    @Test
    @Ignore
    public void calculateP1YDerivativeEnsembleAverage() throws IOException {
        FeatureDataset fds = null;
        
        List<String> gridP1YList = Arrays.asList(
                new String[] {
                    "/Users/tkunicki/Downloads/derivatives/derivative-days_above_threshold.pr.ncml",
                    "/Users/tkunicki/Downloads/derivatives/derivative-days_above_threshold.tmax.ncml",
                    "/Users/tkunicki/Downloads/derivatives/derivative-days_below_threshold.tmin.ncml",
                    "/Users/tkunicki/Downloads/derivatives/derivative-spell_length_above_threshold.tmax.ncml",
                    "/Users/tkunicki/Downloads/derivatives/derivative-spell_length_below_threshold.pr.ncml",
                    "/Users/tkunicki/Downloads/derivatives/derivative-heating_degree_days.ncml",
                    "/Users/tkunicki/Downloads/derivatives/derivative-cooling_degree_days.ncml",
                    "/Users/tkunicki/Downloads/derivatives/derivative-growing_degree_days.ncml",
                    "/Users/tkunicki/Downloads/derivatives/derivative-growing_season_length.ncml",
                });

        for (String gridP1Y : gridP1YList) {
            try {

                fds = FeatureDatasetFactoryManager.open(
                    FeatureType.GRID,
                    gridP1Y,
                    null,
                    new Formatter(System.err));
                if (fds instanceof GridDataset) {
                    GridDataset gds = (GridDataset)fds;
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
                if (fds != null) fds.close();
            }
        }
    }
    
    @Test
    @Ignore
    public void calculateP1YAverageOverP30Y() throws IOException {
        FeatureDataset fds = null;
        
        List<String> gridP1YList = Arrays.asList(
                new String[] {
                    "/Users/tkunicki/Downloads/derivatives/derivative-days_above_threshold.pr.ncml",
                    "/Users/tkunicki/Downloads/derivatives/derivative-days_above_threshold.tmax.ncml",
                    "/Users/tkunicki/Downloads/derivatives/derivative-days_below_threshold.tmin.ncml",
                    "/Users/tkunicki/Downloads/derivatives/derivative-spell_length_above_threshold.tmax.ncml",
                    "/Users/tkunicki/Downloads/derivatives/derivative-spell_length_below_threshold.pr.ncml",
                    "/Users/tkunicki/Downloads/derivatives/derivative-heating_degree_days.ncml",
                    "/Users/tkunicki/Downloads/derivatives/derivative-cooling_degree_days.ncml",
                    "/Users/tkunicki/Downloads/derivatives/derivative-growing_degree_days.ncml",
                    "/Users/tkunicki/Downloads/derivatives/derivative-growing_season_length.ncml",
                });

        for (String gridP1Y : gridP1YList) {
            try {

                fds = FeatureDatasetFactoryManager.open(
                    FeatureType.GRID,
                    gridP1Y,
                    null,
                    new Formatter(System.err));
                if (fds instanceof GridDataset) {
                    GridDataset gds = (GridDataset)fds;
                    List<GridDatatype> gdtl = gds.getGrids();
                    for (GridDatatype gdt : gdtl) {
                        System.out.println("running " + gdt.getName());
                        GridTraverser t = new GridTraverser(gdt);
                        GridVisitor v = new TimeStepAveragingVisitor();
                        t.traverse(v);
                    }
                }

            } finally {
                if (fds != null) fds.close();
            }
        }
    }
    
    @Test
    @Ignore
    public void calculateP30Derivatives() throws IOException {
        FeatureDataset fds = null;
        
        List<String> gridP30YList = Arrays.asList(
                new String[] {
                    "/Users/tkunicki/Downloads/derivatives/derivative-days_above_threshold.pr.P30Y.ncml",
                    "/Users/tkunicki/Downloads/derivatives/derivative-days_above_threshold.tmax.P30Y.ncml",
                    "/Users/tkunicki/Downloads/derivatives/derivative-days_below_threshold.tmin.P30Y.ncml",
                    "/Users/tkunicki/Downloads/derivatives/derivative-spell_length_above_threshold.tmax.P30Y.ncml",
                    "/Users/tkunicki/Downloads/derivatives/derivative-spell_length_below_threshold.pr.P30Y.ncml",
                    "/Users/tkunicki/Downloads/derivatives/derivative-heating_degree_days.P30Y.ncml",
                    "/Users/tkunicki/Downloads/derivatives/derivative-cooling_degree_days.P30Y.ncml",
                    "/Users/tkunicki/Downloads/derivatives/derivative-growing_degree_days.P30Y.ncml",
                    "/Users/tkunicki/Downloads/derivatives/derivative-growing_season_length.P30Y.ncml",
                });

        for (String gridP30Y : gridP30YList) {
            try {

                fds = FeatureDatasetFactoryManager.open(
                    FeatureType.GRID,
                    gridP30Y,
                    null,
                    new Formatter(System.err));
                if (fds instanceof GridDataset) {
                    GridDataset gds = (GridDataset)fds;
                    List<GridDatatype> gdtl = gds.getGrids();
                    for (GridDatatype gdt : gdtl) { 
                        System.out.println("running " + gdt.getName());
                        GridTraverser t = new GridTraverser(gdt);
                        GridVisitor v = new TimeStepDeltaVisitor();
                        t.traverse(v);
                    }
                }

            } finally {
                if (fds != null) fds.close();
            }
        }
    }
    
    
    @Test
    @Ignore
    public void testIOSpeed() throws IOException, InvalidRangeException {
        FeatureDataset fds = null;
        try {
            fds = FeatureDatasetFactoryManager.open(
                FeatureType.GRID,
                "/Users/tkunicki/Data/thredds/dcp-reencode-dfl0/conus_grid.ncml",
                null,
                new Formatter(System.err));
            if (fds instanceof GridDataset) {
                GridDataset gds = (GridDataset)fds;
                List<GridDatatype> gdt = Arrays.asList(new GridDatatype[] {
                    gds.findGridDatatype("ccsm3_a1fi_pr"),
//                    gds.findGridDatatype("gfdl_2-1_a1fi_tmax"),
//                    gds.findGridDatatype("gfdl_2-1_a1fi_pr"),
                });
                Range r = gdt.get(0).getCoordinateSystem().getTimeAxis1D().getRanges().get(0);
                int tCount = 1;
                for (int i = r.first(); i < r.length(); i += tCount /* 1 year */) {
                    Range tRange = new Range(i, Math.min(i + tCount -1, r.last()));
                    for (GridDatatype g : gdt) {
                        GridDatatype subset = g.makeSubset(tRange, null, null, 1, 1, 1);
                        Array a = subset.readDataSlice(-1, -1, -1, -1);
                        float[] d = (float[])a.get1DJavaArray(float.class);
                        System.out.println(g.getName() + " " + (i / tCount) + " of " + (r.length() / tCount) + " : " + d.length + " entries");
                    }
                    
                }
            }
        } finally {
            if (fds != null) fds.close();
        }
    }
}
