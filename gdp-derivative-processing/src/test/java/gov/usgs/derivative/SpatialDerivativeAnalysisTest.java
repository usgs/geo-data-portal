/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usgs.derivative;

import java.util.Map;
import java.util.logging.Level;
import org.slf4j.Logger;
import com.google.common.base.Joiner;
import java.io.File;
import gov.usgs.cida.gdp.coreprocessing.analysis.grid.FeatureCoverageWeightedGridStatisticsWriter.Statistic;
import gov.usgs.derivative.spatial.DerivativeFeatureCoverageWeightedGridStatistics;
import org.geotools.feature.SchemaException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.dt.GridDatatype;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Formatter;
import java.util.LinkedHashMap;
import java.util.List;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.feature.FeatureCollection;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.slf4j.LoggerFactory;
import ucar.ma2.Range;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.dataset.CoordinateAxis1D;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDataset;
import ucar.nc2.ft.FeatureDataset;
import ucar.nc2.ft.FeatureDatasetFactoryManager;

/**
 *
 * @author tkunicki
 */
public class SpatialDerivativeAnalysisTest {
    
    public final static Logger LOGGER = LoggerFactory.getLogger(SpatialDerivativeAnalysisTest.class); 
    
    public SpatialDerivativeAnalysisTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testSomeMethod() throws FactoryException, TransformException, SchemaException {
        
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
        
        for (Map.Entry<String,String> shapefileEntry : shapefileMap.entrySet()) {
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
            
            File shapefileDirectory = new File(spatialDirectory, shapeFileBaseName);
            if (!shapefileDirectory.exists()) {
                shapefileDirectory.mkdirs();
                if (!shapefileDirectory.exists()) {
                throw new RuntimeException("Unable to create shapefile data directory: " + shapefileDirectory.getPath());
                }
                LOGGER.debug("created shapefile data directory {}", shapefileDirectory.getPath());
            }
            
            for(String dataset : gridP1YList) {
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

                    GridDataset gd = (GridDataset)fd;

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

                                GridDatatype zgdt = gdt.makeSubset(null, new Range(zIndex,zIndex), null, 1, 1, 1);

                                DerivativeFeatureCoverageWeightedGridStatistics.execute(
                                        fc,
                                        shapefileAttribute,
                                        zgdt,
                                        null,
                                        Arrays.asList(new Statistic[] { Statistic.MEAN }),
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
}
