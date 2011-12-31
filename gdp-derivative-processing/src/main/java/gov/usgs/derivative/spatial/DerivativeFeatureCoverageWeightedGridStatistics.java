package gov.usgs.derivative.spatial;

import gov.usgs.cida.netcdf.jna.NCUtil.XType;
import ucar.nc2.dataset.CoordinateAxis1D;
import org.slf4j.Logger;
import gov.usgs.cida.gdp.coreprocessing.analysis.grid.FeatureCoverageWeightedGridStatisticsWriter.Statistic;
import gov.usgs.cida.gdp.coreprocessing.analysis.grid.GridCellCoverageFactory;
import gov.usgs.cida.gdp.coreprocessing.analysis.statistics.WeightedStatistics1D;
import gov.usgs.cida.gdp.coreprocessing.analysis.grid.GridCellCoverageFactory.GridCellCoverageByIndex;
import gov.usgs.cida.gdp.coreprocessing.analysis.grid.GridCellCoverageFactory.GridCellIndexCoverage;

import gov.usgs.cida.gdp.coreprocessing.analysis.grid.GridCellTraverser;
import gov.usgs.cida.gdp.coreprocessing.analysis.grid.GridCellVisitor;
import gov.usgs.cida.gdp.coreprocessing.analysis.grid.GridType;
import gov.usgs.cida.gdp.coreprocessing.analysis.grid.GridUtility;
import gov.usgs.cida.netcdf.dsg.Observation;
import gov.usgs.cida.netcdf.dsg.RecordType;
import gov.usgs.cida.netcdf.dsg.Station;
import gov.usgs.cida.netcdf.dsg.StationTimeSeriesNetCDFFile;
import gov.usgs.cida.netcdf.dsg.Variable;
import java.io.File;
import java.text.SimpleDateFormat;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.geotools.feature.FeatureCollection;
import org.geotools.feature.SchemaException;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import org.slf4j.LoggerFactory;

import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import ucar.nc2.dataset.CoordinateAxis1DTime;
import ucar.nc2.dt.GridDatatype;


public class DerivativeFeatureCoverageWeightedGridStatistics {
    
    static FeatureCollection<SimpleFeatureType, SimpleFeature>
            featureCollectionLAST;
    static GridCellCoverageByIndex coverageByIndexLAST;
    
    public final static Logger LOGGER = LoggerFactory.getLogger(DerivativeFeatureCoverageWeightedGridStatistics.class);

    public enum GroupBy {
        STATISTIC,
        FEATURE_ATTRIBUTE;
    }

    public static void execute(
            FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection,
            String attributeName,
            GridDatatype gridDatatype,
            Range timeRange,
            List<Statistic> statisticList,
            boolean requireFullCoverage,
            File file)
            throws IOException, InvalidRangeException, FactoryException, TransformException, SchemaException
    {
        
        GridType gt = GridType.findGridType(gridDatatype.getCoordinateSystem());
        
        if( !(gt == GridType.ZYX || gt == GridType.TZYX || gt == GridType.YX || gt == GridType.TYX) ) {
            throw new IllegalStateException("Currently require y-x or t-y-x grid with this operation");
        }

        Range[] ranges = GridUtility.getXYRangesFromBoundingBox(
                featureCollection.getBounds(),
                gridDatatype.getCoordinateSystem(),
                requireFullCoverage);
        gridDatatype = gridDatatype.makeSubset(null, null, timeRange, null, ranges[1], ranges[0]);

        GridCellCoverageByIndex coverageByIndex = null;
        if (featureCollectionLAST == featureCollection && coverageByIndexLAST != null) {
            coverageByIndex = coverageByIndexLAST;
            LOGGER.debug("Using cached grid feature coverage");
        } else {
            coverageByIndex =
				GridCellCoverageFactory.generateFeatureAttributeCoverageByIndex(
                    featureCollection,
                    attributeName,
                    gridDatatype.getCoordinateSystem());
            coverageByIndexLAST = coverageByIndex;
            featureCollectionLAST = featureCollection;
            LOGGER.debug("Creating grid feature coverage");
        }

        String variableUnits = gridDatatype.getVariable().getUnitsString();

        List<Object> attributeList = coverageByIndex.getAttributeValueList();

        // START HACK
        RecordType rt = new RecordType(gridDatatype.getCoordinateSystem().getTimeAxis1D().getUnitsString());
        for (Statistic statistic : statisticList) {
            Map<String, Object> attributeMap = new LinkedHashMap<String, Object>();
            if (statistic.getNeedsUnits()) {
                attributeMap.put("units", String.format(statistic.getUnitFormat(), variableUnits));
            }
            rt.addType(new Variable(statistic.name().toLowerCase(), XType.NC_DOUBLE, attributeMap));
        }
        
        int stationCount = attributeList.size();
        Station[] st = new Station[stationCount];
        for (int stationIndex = 0; stationIndex < stationCount; ++stationIndex) {
            st[stationIndex] = new Station((float)stationIndex, (float)stationIndex, attributeList.get(stationIndex).toString());
        }
        StationTimeSeriesNetCDFFile stationFile = new StationTimeSeriesNetCDFFile(
                file,
                rt,
                false,
                st);
        // END HACK        
        

        WeightedGridStatisticsVisitor v = null;
        switch (gt) {
            case YX:
            case ZYX:
                v = new WeightedGridStatisticsVisitor_YX(coverageByIndex, stationFile);
            break;
            case TYX:
            case TZYX:
                v = new WeightedGridStatisticsVisitor_TYX(coverageByIndex, stationFile);
            break;
            default:
                throw new IllegalStateException("Currently require y-x or t-y-x grid for this operation");
        }

        GridCellTraverser gct = new GridCellTraverser(gridDatatype);

        gct.traverse(v);
        
        stationFile.close();
    }

    public static abstract class FeatureCoverageGridCellVisitor extends GridCellVisitor {

        final protected GridCellCoverageByIndex coverageByIndex;

        public FeatureCoverageGridCellVisitor(GridCellCoverageByIndex coverageByIndex) {
            this.coverageByIndex = coverageByIndex;
        }

        @Override
        public void processGridCell(int xCellIndex, int yCellIndex, double value) {
            double coverageTotal = 0;
			List<GridCellIndexCoverage> list = coverageByIndex.getCoverageList(xCellIndex, yCellIndex);
			if (list != null) {
				for (GridCellIndexCoverage c : list) {
					if (c.coverage > 0.0) {
						processPerAttributeGridCellCoverage(value, c.coverage, c.attribute);
					}
					coverageTotal += c.coverage;
				}
			}
            if (coverageTotal > 0.0) {
                processAllAttributeGridCellCoverage(value, coverageTotal);
            }
        }

        public abstract void processPerAttributeGridCellCoverage(double value, double coverage, Object attribute);

        public abstract void processAllAttributeGridCellCoverage(double value, double coverage);

    }


    protected static abstract class WeightedGridStatisticsVisitor extends FeatureCoverageGridCellVisitor {

        protected final StationTimeSeriesNetCDFFile writer;
        
        protected Map<Object, WeightedStatistics1D> perAttributeStatistics;
        protected WeightedStatistics1D allAttributeStatistics;

        protected CoordinateAxis1D zAxis;
        protected int zIndex;
        protected double zValue;
        
        public WeightedGridStatisticsVisitor(GridCellCoverageByIndex coverageByIndex, StationTimeSeriesNetCDFFile writer) {
            super(coverageByIndex);
            this.writer = writer;
        }

        protected Map<Object, WeightedStatistics1D> createPerAttributeStatisticsMap() {
            Map map = new LinkedHashMap<Object, WeightedStatistics1D>();
            for (Object attributeValue : coverageByIndex.getAttributeValueList()) {
                map.put(attributeValue, new WeightedStatistics1D());
            }
            return map;
        }

        @Override
        public void traverseStart(GridDatatype gridDataType) {
            super.traverseStart(gridDataType);
            zAxis = gridDataType.getCoordinateSystem().getVerticalAxis();
        }
        
        @Override
        public boolean zStart(int zIndex) {
            super.zStart(zIndex);
            this.zIndex = zIndex;
            zValue = zAxis.getCoordValue(zIndex);
            return true;
        }
        
        @Override
        public void yxStart() {
            super.yxStart();
            perAttributeStatistics = createPerAttributeStatisticsMap();
            allAttributeStatistics = new WeightedStatistics1D();
        }

        @Override
        public void processPerAttributeGridCellCoverage(double value, double coverage, Object attribute) {
            perAttributeStatistics.get(attribute).accumulate(value, coverage);
        }

        @Override
        public void processAllAttributeGridCellCoverage(double value, double coverage) {
            allAttributeStatistics.accumulate(value, coverage);
        }
    }

    protected static class WeightedGridStatisticsVisitor_YX extends WeightedGridStatisticsVisitor {

        WeightedGridStatisticsVisitor_YX(
                GridCellCoverageByIndex coverageByIndex,
                StationTimeSeriesNetCDFFile writer) {
            super(coverageByIndex, writer);
        }

        @Override
        public void traverseStart(GridDatatype gridDatatype) {
            super.traverseStart(gridDatatype);
        }
        
        @Override
        public void zEnd(int zIndex) {
            super.zEnd(zIndex);
            if (zAxis != null) {
                int stationIndex = 0;
                for (WeightedStatistics1D statistic : perAttributeStatistics.values()) {
                    writer.putObservation(new Observation(0, stationIndex++, statistic.getMean()));
                }
            }
        }
        
        @Override
        public void traverseEnd() {
            super.traverseEnd();
            if (zAxis == null) {
                int stationIndex = 0;
                for (WeightedStatistics1D statistic : perAttributeStatistics.values()) {
                    writer.putObservation(new Observation(0, stationIndex++, statistic.getMean()));
                }
            }
        }

    }

    protected static class WeightedGridStatisticsVisitor_TYX extends WeightedGridStatisticsVisitor {
        
        public final static String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
        public final static String TIMEZONE = "UTC";

        protected Map<Object, WeightedStatistics1D> allTimestepPerAttributeStatistics;
        protected WeightedStatistics1D allTimestepAllAttributeStatistics;
        
        protected CoordinateAxis1DTime tAxis;
        protected int tIndex;
        
        protected SimpleDateFormat dateFormat;

        public WeightedGridStatisticsVisitor_TYX(
                GridCellCoverageByIndex coverageByIndex,
                StationTimeSeriesNetCDFFile writer) {
            super(coverageByIndex, writer);
            
            dateFormat = new SimpleDateFormat(DATE_FORMAT);
            dateFormat.setTimeZone(TimeZone.getTimeZone(TIMEZONE));
        }

        @Override
        public void traverseStart(GridDatatype gridDatatype) {
            super.traverseStart(gridDatatype);

            allTimestepPerAttributeStatistics = createPerAttributeStatisticsMap();
            allTimestepAllAttributeStatistics = new WeightedStatistics1D();
            
            tAxis = gridDatatype.getCoordinateSystem().getTimeAxis1D();
        }

        @Override
        public boolean tStart(int tIndex) {
            super.tStart(tIndex);
            this.tIndex = tIndex;
            return true;
        }
        
        @Override
        public void processPerAttributeGridCellCoverage(double value, double coverage, Object attribute) {
            super.processPerAttributeGridCellCoverage(value, coverage, attribute);
            allTimestepPerAttributeStatistics.get(attribute).accumulate(value, coverage);
        }

        @Override
        public void processAllAttributeGridCellCoverage(double value, double coverage) {
            super.processAllAttributeGridCellCoverage(value, coverage);
            allTimestepAllAttributeStatistics.accumulate(value, coverage);
        }

        @Override
        public void zEnd(int zIndex) {
            super.zEnd(zIndex);
            if (zAxis != null) {
                int stationIndex = 0;
                for (WeightedStatistics1D statistic : perAttributeStatistics.values()) {
                    writer.putObservation(new Observation((int)tAxis.getCoordValue(tIndex), stationIndex++, statistic.getMean()));
                }
            }
        }
        
        @Override
        public void tEnd(int tIndex) {
            super.tEnd(tIndex);
            if (zAxis == null) {
                int stationIndex = 0;
                for (WeightedStatistics1D statistic : perAttributeStatistics.values()) {
                    writer.putObservation(new Observation((int)tAxis.getCoordValue(tIndex), stationIndex++, statistic.getMean()));
                }
            }
        }

        @Override
        public void traverseEnd() {
            super.traverseEnd();

        }
    }

}
