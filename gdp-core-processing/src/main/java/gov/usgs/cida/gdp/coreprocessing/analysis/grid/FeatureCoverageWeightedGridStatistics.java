package gov.usgs.cida.gdp.coreprocessing.analysis.grid;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.geotools.feature.FeatureCollection;
import org.geotools.feature.SchemaException;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;

import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDataset;
import ucar.nc2.dt.GridDatatype;
import ucar.unidata.geoloc.LatLonRect;

import com.google.common.base.Preconditions;
import gov.usgs.cida.gdp.coreprocessing.analysis.GeoToolsNetCDFUtility;
import gov.usgs.cida.gdp.coreprocessing.analysis.grid.FeatureCoverageWeightedGridStatisticsWriter.Statistic;
import gov.usgs.cida.gdp.coreprocessing.analysis.statistics.WeightedStatistics1D;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.slf4j.LoggerFactory;
import ucar.nc2.dataset.CoordinateAxis1DTime;

public class FeatureCoverageWeightedGridStatistics {
	private static org.slf4j.Logger log = LoggerFactory.getLogger(FeatureCoverageWeightedGridStatistics.class);

    public static void execute(
            FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection,
            String attributeName,
            GridDataset gridDataset,
            String variableName,
            Range timeRange,
            List<Statistic> statisticList,
            BufferedWriter writer,
            boolean groupByStatistic,
            String delimiter)
            throws IOException, InvalidRangeException, FactoryException, TransformException, SchemaException
    {

        GridDatatype gdt = gridDataset.findGridDatatype(variableName);
        Preconditions.checkNotNull(gdt, "Variable named %s not found in gridDataset", variableName);

        GridType gt = GridType.findGridType(gdt.getCoordinateSystem());
        if( !(gt == GridType.YX || gt == GridType.TYX) ) {
            throw new IllegalStateException("Currently require y-x or t-y-x grid for this operation");
        }

        LatLonRect featureCollectionLLR = GeoToolsNetCDFUtility.getLatLonRectFromEnvelope(
                featureCollection.getBounds(),
                DefaultGeographicCRS.WGS84);
		LatLonRect gridLLR = gdt.getCoordinateSystem().getLatLonBoundingBox();
		if (gridLLR.containedIn(featureCollectionLLR)) {
			throw new RuntimeException("feature bounds (" + featureCollectionLLR + ") not contained in gridded dataset (" + gridLLR + ")");
		}

        try {
            Range[] ranges = GridUtility.getRangesFromLatLonRect(
                    featureCollectionLLR, gdt.getCoordinateSystem());
            gdt = gdt.makeSubset(null, null, timeRange, null, ranges[1], ranges[0]);
        } catch (InvalidRangeException ex) {
            log.error(null, ex);
            throw ex;  // rethrow requested by IS
        }

        GridCoordSystem gcs = gdt.getCoordinateSystem();

        Map<Object, GridCellCoverage> attributeCoverageMap =
                GridCellCoverageFactory.generateByFeatureAttribute(
                    featureCollection,
                    attributeName,
                    gdt.getCoordinateSystem());

        String variableUnits = gdt.getVariable().getUnitsString();

        List<Object> attributeList = Collections.unmodifiableList(new ArrayList<Object>(attributeCoverageMap.keySet()));

        FeatureCoverageWeightedGridStatisticsWriter writerX =
                new FeatureCoverageWeightedGridStatisticsWriter(
                    attributeList,
                    variableName,
                    variableUnits,
                    statisticList,
                    groupByStatistic,
                    delimiter,
                    writer);

        WeightedGridStatisticsVisitor v = null;
        switch (gt) {
            case YX:
                v = new WeightedGridStatisticsVisitor_YX(attributeCoverageMap, writerX);
            break;
            case TYX:
                v = new WeightedGridStatisticsVisitor_TYX(attributeCoverageMap, writerX, gcs.getTimeAxis1D(), timeRange);
            break;
            default:
                throw new IllegalStateException("Currently require y-x or t-y-x grid for this operation");
        }

        GridCellTraverser gct = new GridCellTraverser(gdt);

        gct.traverse(v);
    }

    public static abstract class FeatureCoverageGridCellVisitor extends GridCellVisitor {

        final protected Map<Object, GridCellCoverage> attributeCoverageMap;

        public FeatureCoverageGridCellVisitor(Map<Object, GridCellCoverage> attributeCoverageMap) {
            this.attributeCoverageMap = attributeCoverageMap;
        }

        @Override
        public void processGridCell(int xCellIndex, int yCellIndex, double value) {
            double coverageTotal = 0;
            for (Map.Entry<Object, GridCellCoverage> entry : this.attributeCoverageMap.entrySet()) {
                Object attribute = entry.getKey();
                GridCellCoverage gridCellCoverage = entry.getValue();
                double coverage = gridCellCoverage.getCellCoverageFraction(xCellIndex, yCellIndex);
                if (coverage > 0.0) {
                    processPerAttributeGridCellCoverage(value, coverage, attribute);
                }
                coverageTotal += coverage;
            }
            if (coverageTotal > 0.0) {
                processAllAttributeGridCellCoverage(value, coverageTotal);
            }
        }

        public abstract void processPerAttributeGridCellCoverage(double value, double coverage, Object attribute);

        public abstract void processAllAttributeGridCellCoverage(double value, double coverage);

    }


    protected static abstract class WeightedGridStatisticsVisitor extends FeatureCoverageGridCellVisitor {

        protected Map<Object, WeightedStatistics1D> perAttributeStatistics;
        protected WeightedStatistics1D allAttributeStatistics;

        public WeightedGridStatisticsVisitor(Map<Object, GridCellCoverage> attributeCoverageMap) {
            super(attributeCoverageMap);
        }

        protected Map<Object, WeightedStatistics1D> createPerAttributeStatisticsMap() {
            Map map = new LinkedHashMap<Object, WeightedStatistics1D>();
            for (Object attributeValue : attributeCoverageMap.keySet()) {
                map.put(attributeValue, new WeightedStatistics1D());
            }
            return map;
        }

        @Override
        public void yxStart() {
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

        FeatureCoverageWeightedGridStatisticsWriter writer;

        WeightedGridStatisticsVisitor_YX(
                Map<Object, GridCellCoverage> attributeCoverageMap,
                FeatureCoverageWeightedGridStatisticsWriter writer) {
            super(attributeCoverageMap);
            this.writer = writer;
        }

        @Override
        public void traverseStart(GridCoordSystem gridCoordSystem) {
            try {
                writer.writerHeader(null);
            } catch (IOException ex) {
                // TODO
            }
        }

        @Override
        public void traverseEnd() {
            try {
                writer.writeRow(
                        null,
                        perAttributeStatistics.values(),
                        allAttributeStatistics);
            } catch (IOException ex) {
                // TODO
            }
        }

    }

    protected static class WeightedGridStatisticsVisitor_TYX extends WeightedGridStatisticsVisitor {

        protected Map<Object, WeightedStatistics1D> allTimestepPerAttributeStatistics;
        protected WeightedStatistics1D allTimestepAllAttributeStatistics;

        protected FeatureCoverageWeightedGridStatisticsWriter writer;
        
        protected CoordinateAxis1DTime tAxis;
        protected int tIndexOffset;

        public WeightedGridStatisticsVisitor_TYX(
                Map<Object, GridCellCoverage> attributeCoverageMap,
                FeatureCoverageWeightedGridStatisticsWriter writer,
                CoordinateAxis1DTime tAxis,
                Range tRange) {
            super(attributeCoverageMap);
            this.writer = writer;
            this.tAxis = tAxis;
            this.tIndexOffset = tRange.first();
        }

        @Override
        public void traverseStart(GridCoordSystem gridCoordSystem) {
            
            try {
                writer.writerHeader(FeatureCoverageWeightedGridStatisticsWriter.TIMESTEPS_LABEL);
            } catch (IOException ex) {
                // TODO
            }

            allTimestepPerAttributeStatistics =
                    createPerAttributeStatisticsMap();
            allTimestepAllAttributeStatistics = new WeightedStatistics1D();
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
        public void tEnd(int tIndex) {
            try {
                writer.writeRow(
                        tAxis.getTimeDate(tIndexOffset + tIndex).toGMTString(),
                        perAttributeStatistics.values(),
                        allAttributeStatistics);
            } catch (IOException e) {
                // TODO
            }
        }

        @Override
        public void traverseEnd() {
            try {
                writer.writeRow(
                        FeatureCoverageWeightedGridStatisticsWriter.ALL_TIMESTEPS_LABEL,
                        allTimestepPerAttributeStatistics.values(),
                        allTimestepAllAttributeStatistics);
            } catch (IOException ex) {
                // TODO
            }
        }
    }
}
