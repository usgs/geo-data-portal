package gov.usgs.cida.gdp.coreprocessing.analysis.grid;

import ucar.nc2.dataset.CoordinateAxis1D;
import org.slf4j.Logger;
import gov.usgs.cida.gdp.coreprocessing.Delimiter;
import gov.usgs.cida.gdp.coreprocessing.analysis.grid.FeatureCoverageWeightedGridStatisticsWriter.Statistic;
import gov.usgs.cida.gdp.coreprocessing.analysis.statistics.WeightedStatistics1D;
import gov.usgs.cida.gdp.coreprocessing.analysis.grid.GridCellCoverageFactory.GridCellCoverageByIndex;
import gov.usgs.cida.gdp.coreprocessing.analysis.grid.GridCellCoverageFactory.GridCellIndexCoverage;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
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
import ucar.nc2.dt.GridDataset;
import ucar.nc2.dt.GridDatatype;

import static com.google.common.base.Preconditions.checkNotNull;

public class FeatureCoverageWeightedGridStatistics {
    
    public final static Logger LOGGER = LoggerFactory.getLogger(FeatureCoverageWeightedGridStatistics.class);

    public enum GroupBy {
        STATISTIC,
        FEATURE_ATTRIBUTE;
    }

    public static void execute(
            FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection,
            String attributeName,
            GridDataset gridDataset,
            String variableName,
            Range timeRange,
            List<Statistic> statisticList,
            BufferedWriter writer,
            GroupBy groupBy,
            Delimiter delimiter)
            throws IOException, InvalidRangeException, FactoryException, TransformException, SchemaException
    {
        GridDatatype gridDatatype = checkNotNull(
                    gridDataset.findGridDatatype(variableName),
                    "Variable named %s not found in girdded dataset %s",
                    variableName);

        execute(featureCollection,
                attributeName,
                gridDatatype,
                timeRange,
                statisticList,
                writer,
                groupBy,
                delimiter,
                true,
                false,
                false);
    }

    public static void execute(
            FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection,
            String attributeName,
            GridDatatype gridDatatype,
            Range timeRange,
            List<Statistic> statisticList,
            BufferedWriter writer,
            GroupBy groupBy,
            Delimiter delimiter,
            boolean requireFullCoverage,
            boolean summarizeTimeStep,
            boolean summarizeFeatures)
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

        GridCellCoverageByIndex coverageByIndex =
				GridCellCoverageFactory.generateFeatureAttributeCoverageByIndex(
                    featureCollection,
                    attributeName,
                    gridDatatype.getCoordinateSystem());

        String variableUnits = gridDatatype.getVariable().getUnitsString();

        List<Object> attributeList = coverageByIndex.getAttributeValueList();

        FeatureCoverageWeightedGridStatisticsWriter writerX =
                new FeatureCoverageWeightedGridStatisticsWriter(
                    attributeList,
                    gridDatatype.getName(),
                    variableUnits,
                    statisticList,
                    groupBy != GroupBy.FEATURE_ATTRIBUTE,  // != in case value equals null, default to GroupBy.STATISTIC
                    delimiter.delimiter,
                    summarizeTimeStep,
                    summarizeFeatures,
                    writer);

        WeightedGridStatisticsVisitor v = null;
        switch (gt) {
            case YX:
            case ZYX:
                v = new WeightedGridStatisticsVisitor_YX(coverageByIndex, writerX);
            break;
            case TYX:
            case TZYX:
                v = new WeightedGridStatisticsVisitor_TYX(coverageByIndex, writerX);
            break;
            default:
                throw new IllegalStateException("Currently require y-x or t-y-x grid for this operation");
        }

        GridCellTraverser gct = new GridCellTraverser(gridDatatype);

        gct.traverse(v);
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

        protected final FeatureCoverageWeightedGridStatisticsWriter writer;
        
        protected Map<Object, WeightedStatistics1D> perAttributeStatistics;
        protected WeightedStatistics1D allAttributeStatistics;

        protected CoordinateAxis1D zAxis;
        protected int zIndex;
        protected double zValue;
        
        public WeightedGridStatisticsVisitor(GridCellCoverageByIndex coverageByIndex, FeatureCoverageWeightedGridStatisticsWriter writer) {
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
                FeatureCoverageWeightedGridStatisticsWriter writer) {
            super(coverageByIndex, writer);
        }

        @Override
        public void traverseStart(GridDatatype gridDatatype) {
            super.traverseStart(gridDatatype);
            try {
                List<String> rowLabelList = new ArrayList<String>();
                rowLabelList.add("");
                if (zAxis != null) {
                    rowLabelList.add(zAxis.getOriginalName() + "(" + zAxis.getUnitsString() + ")");
                }
                writer.writerHeader(rowLabelList);
            } catch (IOException ex) {
                // TODO
            }
        }
        
        @Override
        public void zEnd(int zIndex) {
            super.zEnd(zIndex);
            try {
                List<String> rowLabelList = new ArrayList<String>();
                rowLabelList.add("");
                if (zAxis != null) {
                    rowLabelList.add("");
                }
                writer.writeRow(
                        rowLabelList,
                        perAttributeStatistics.values(),
                        allAttributeStatistics);
            } catch (IOException ex) {
                // TODO
            }
        }
        
        @Override
        public void traverseEnd() {
            super.traverseEnd();
            if (zAxis == null) {
                try {
                    List<String> rowLabelList = new ArrayList<String>();
                    rowLabelList.add("");
                    if (zAxis != null) {
                        rowLabelList.add("");
                    }
                    writer.writeRow(
                            rowLabelList,
                            perAttributeStatistics.values(),
                            allAttributeStatistics);
                } catch (IOException ex) {
                    // TODO
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
                FeatureCoverageWeightedGridStatisticsWriter writer) {
            super(coverageByIndex, writer);
            
            dateFormat = new SimpleDateFormat(DATE_FORMAT);
            dateFormat.setTimeZone(TimeZone.getTimeZone(TIMEZONE));
        }

        @Override
        public void traverseStart(GridDatatype gridDatatype) {
            super.traverseStart(gridDatatype);
            try {
                List<String> rowLabelList = new ArrayList<String>();
                rowLabelList.add(FeatureCoverageWeightedGridStatisticsWriter.TIMESTEPS_LABEL);
                if (zAxis != null) {
                    String zAxisName = zAxis.getName();
                    rowLabelList.add(zAxisName + "(" + zAxis.getUnitsString() + ")");
                }
                writer.writerHeader(rowLabelList);
            } catch (IOException ex) {
                // TODO
            }

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
            try {
                Date date = tAxis.getTimeDate(tIndex);
                List<String> rowLabelList = new ArrayList<String>();
                rowLabelList.add(dateFormat.format(date));
                rowLabelList.add(Double.toString(zValue));
                writer.writeRow(
                        rowLabelList,
                        perAttributeStatistics.values(),
                        allAttributeStatistics);
            } catch (IOException e) {
                // TODO
            }
        }
        
        @Override
        public void tEnd(int tIndex) {
            super.tEnd(tIndex);
            if (zAxis == null) {
                try {
                    Date date = tAxis.getTimeDate(tIndex);
                    List<String> rowLabelList = new ArrayList<String>();
                    rowLabelList.add(dateFormat.format(date));
                    writer.writeRow(
                            rowLabelList,
                            perAttributeStatistics.values(),
                            allAttributeStatistics);
                } catch (IOException e) {
                    // TODO
                }
            }
        }

        @Override
        public void traverseEnd() {
            super.traverseEnd();
            try {
                if (writer.isSummarizeFeatureAttribute()) {
                    List<String> rowLabelList = new ArrayList<String>();
                    rowLabelList.add(FeatureCoverageWeightedGridStatisticsWriter.ALL_TIMESTEPS_LABEL);
                    if (zAxis != null) {
                        rowLabelList.add("");
                    }
                    writer.writeRow(
                            rowLabelList,
                            allTimestepPerAttributeStatistics.values(),
                            allTimestepAllAttributeStatistics);
                }
            } catch (IOException ex) {
                // TODO
            }
        }
    }

}
