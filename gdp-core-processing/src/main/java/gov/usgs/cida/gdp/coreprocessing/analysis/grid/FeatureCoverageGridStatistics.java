package gov.usgs.cida.gdp.coreprocessing.analysis.grid;

import static com.google.common.base.Preconditions.checkNotNull;
import gov.usgs.cida.gdp.coreprocessing.Delimiter;
import gov.usgs.cida.gdp.coreprocessing.analysis.grid.Statistics1DWriter.Statistic;
import gov.usgs.cida.gdp.coreprocessing.analysis.statistics.Statistics1D;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.prep.PreparedGeometry;
import com.vividsolutions.jts.geom.prep.PreparedGeometryFactory;
import gov.usgs.cida.gdp.coreprocessing.analysis.grid.GridUtility.IndexToCoordinateBuilder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.SchemaException;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.geometry.BoundingBox;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDataset;
import ucar.nc2.dt.GridDatatype;
import gov.usgs.cida.gdp.coreprocessing.analysis.grid.Statistics1DWriter.GroupBy;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Iterator;
import java.util.TimeZone;
import org.geotools.feature.FeatureIterator;
import ucar.nc2.dataset.CoordinateAxis1D;
import ucar.nc2.dataset.CoordinateAxis1DTime;

/**
 *
 * @author tkunicki
 */
public class FeatureCoverageGridStatistics {
    
    public final static String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    public final static String TIMEZONE = "UTC";

    public static void execute(
            FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection,
            String attributeName,
            GridDataset gridDataset,
            String variableName,
            Range timeRange,
            List<Statistic> statisticList,
            Writer writer,
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
                gridDatatype.makeSubset(null, null, timeRange, null, null, null),
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
            List<Statistic> statisticList,
            Writer writer,
            GroupBy groupBy,
            Delimiter delimiter,
            boolean requireFullCoverage,
            boolean summarizeTimeStep,
            boolean summarizeFeatures)
            throws IOException, InvalidRangeException, FactoryException, TransformException, SchemaException {

        GridCoordSystem gcs = gridDatatype.getCoordinateSystem();
        GridType gt = GridType.findGridType(gcs);
        if (gt != GridType.YX && gt != GridType.ZYX && gt != GridType.TZYX && gt != GridType.TYX) {
            throw new IllegalStateException("incompatible grid dimensions");
        }

        // these two calls are used to test for coverage/intersection based on 'requireFullCoverage',
        // if required coverage criterea is not fufilled an exception will be thrown.
        Range[] featureCollectionRanges = GridUtility.getXYRangesFromBoundingBox(featureCollection.getBounds(), gcs, requireFullCoverage);
        gridDatatype = gridDatatype.makeSubset(null, null, null, null, featureCollectionRanges[1], featureCollectionRanges[0]);

        CoordinateReferenceSystem gridCRS = CRSUtility.getCRSFromGridCoordSystem(gcs);
        CoordinateReferenceSystem featureCRS =
                featureCollection.getSchema().getCoordinateReferenceSystem();

        MathTransform gridToFeatureTransform = CRS.findMathTransform(
                gridCRS,
                featureCRS,
                true);

        AttributeDescriptor attributeDescriptor =
                featureCollection.getSchema().getDescriptor(attributeName);
        if (attributeDescriptor == null) {
            throw new IllegalArgumentException(
                    "Attribute " + attributeName + " not found in FeatureCollection.");
        }

        boolean attributeComparable = Comparable.class.isAssignableFrom(
                attributeDescriptor.getType().getBinding());

        Map<Object, Statistics1D> perTimestepPerAttributeStatisticsMap;
        Statistics1D perTimestepAllAttributeStatistics;
        
        Map<Object, Statistics1D> allTimestepPerAttributeStatisticsMap;
        Statistics1D allTimestepAllAttributeStatistics;
        

        allTimestepPerAttributeStatisticsMap = create(attributeComparable);
        allTimestepAllAttributeStatistics = new Statistics1D();
        
        Statistics1DWriter writerX = null;
        
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        dateFormat.setTimeZone(TimeZone.getTimeZone(TIMEZONE));
        
        CoordinateAxis1D zAxis = gcs.getVerticalAxis();
        CoordinateAxis1DTime tAxis = gcs.getTimeAxis1D();
        
        for (Range tRange : decompose(tAxis)) {
            String tLabel = tRange == null ? null : dateFormat.format(gcs.getTimeAxis1D().getCalendarDate(tRange.first()).toDate());
            for (Range zRange : decompose(zAxis)) {
                String zLabel = zRange == null ? null : Double.toString(gcs.getVerticalAxis().getCoordValue(zRange.first()));
                
                FeatureIterator<SimpleFeature> featureIterator = featureCollection.features();
                try {

                    perTimestepPerAttributeStatisticsMap = create(attributeComparable);
                    perTimestepAllAttributeStatistics = new Statistics1D();
                    
                    featureIterator = featureCollection.features();
                    
                    while (featureIterator.hasNext()) {

                        SimpleFeature feature = featureIterator.next();
                        Object attribute = feature.getAttribute(attributeName);

                        if (attribute != null) {

                            BoundingBox featureBoundingBox = feature.getBounds();

                            Geometry featureGeometry = (Geometry) feature.getDefaultGeometry();

                            try {
                                Range[] featureRanges = GridUtility.getXYRangesFromBoundingBox(
                                        featureBoundingBox, gridDatatype.getCoordinateSystem(), requireFullCoverage);

                                GridDatatype featureGridDataType = gridDatatype.makeSubset(null, null, tRange, zRange, featureRanges[1], featureRanges[0]);

                                PreparedGeometry preparedGeometry = PreparedGeometryFactory.prepare(featureGeometry);

                                Statistics1D perTimestepPerFeatureStatistics = new Statistics1D();
                                GridCellTraverser traverser = new GridCellTraverser(featureGridDataType);
                                traverser.traverse(new FeatureCoverageGridStatistics.FeatureGridCellVisitor(
                                        preparedGeometry,
                                        gridToFeatureTransform,
                                        perTimestepPerFeatureStatistics));

                                extract(perTimestepPerAttributeStatisticsMap, attribute).accumulate(perTimestepPerFeatureStatistics);
                                extract(allTimestepPerAttributeStatisticsMap, attribute).accumulate(perTimestepPerFeatureStatistics);                        
                                perTimestepAllAttributeStatistics.accumulate(perTimestepPerFeatureStatistics);
                                allTimestepAllAttributeStatistics.accumulate(perTimestepPerFeatureStatistics);

                            } catch (InvalidRangeException e) {
                                /* this may happen if the feature doesn't intersect the grid, this is OK */
                            }
                        }
                    }
                    if (writerX == null) {
                         writerX = new Statistics1DWriter(
                            new ArrayList<Object>(perTimestepPerAttributeStatisticsMap.keySet()),
                            gridDatatype.getName(),
                            gridDatatype.getVariable().getUnitsString(),
                            statisticList,
                            groupBy != GroupBy.FEATURE_ATTRIBUTE,  // != in case value equals null, default to GroupBy.STATISTIC
                            delimiter.delimiter,
                            null, // default block separator used
                            summarizeTimeStep,
                            summarizeFeatures,
                            writer);
                            writerX.writeHeader(Statistics1DWriter.buildRowLabel(
                                    tAxis == null ? "" : Statistics1DWriter.TIMESTEPS_LABEL,
                                    zAxis == null ? null : String.format("%s(%s)", zAxis.getShortName(), zAxis.getUnitsString())));
                    }
                    writerX.writeRow(
                            Statistics1DWriter.buildRowLabel(tLabel, zLabel),
                            perTimestepPerAttributeStatisticsMap.values(),
                            perTimestepAllAttributeStatistics);
                } finally {
                    featureIterator.close();
                }
            }
        }
        if (summarizeFeatures && writerX != null) {
            writerX.writeRow(
                Statistics1DWriter.buildRowLabel(Statistics1DWriter.ALL_TIMESTEPS_LABEL, zAxis == null ? null : ""),
                allTimestepPerAttributeStatisticsMap.values(),
                allTimestepAllAttributeStatistics);
        }
    }

    protected static class FeatureGridCellVisitor extends GridCellVisitor {

        private final GeometryFactory geometryFactory = new GeometryFactory();
        private final PreparedGeometry preparedGeometry;
        private final MathTransform gridToFeatureTransform;
        private final Statistics1D statistics;
        private IndexToCoordinateBuilder coordinateBuilder;

        protected FeatureGridCellVisitor(
                PreparedGeometry preparedGeometry,
                MathTransform gridToFeatureTransform,
                Statistics1D statistics) {
            this.preparedGeometry = preparedGeometry;
            this.gridToFeatureTransform = gridToFeatureTransform;
            this.statistics = statistics;
        }

        @Override
        public void traverseStart(GridDatatype gridDatatype) {
            coordinateBuilder = GridUtility.generateIndexToCellCenterCoordinateBuilder(gridDatatype.getCoordinateSystem());
        }

        @Override
        public void processGridCell(int xCellIndex, int yCellIndex, double value) {
            Coordinate coordinate =
                    coordinateBuilder.getCoordinate(xCellIndex, yCellIndex);
            try {
                JTS.transform(coordinate, coordinate, gridToFeatureTransform);
                if (preparedGeometry.contains(geometryFactory.createPoint(coordinate))) {
                    statistics.accumulate(value);
                }
            } catch (TransformException e) {
            }
        }
    }
    
    private static Map<Object, Statistics1D> create(boolean attributeComparable) {
        return attributeComparable ?
                // rely on Comparable to sort
                new TreeMap<Object, Statistics1D>() :
                // use order from FeatureCollection.iterator();
                new LinkedHashMap<Object, Statistics1D>();
    }
    
    private static Statistics1D extract(Map<Object, Statistics1D> map, Object attribute) {
        Statistics1D statistics = map.get(attribute);
        if (statistics == null) {
            statistics = new Statistics1D();
            map.put(attribute, statistics);
        }
        return statistics;
    }

    public static Iterable<Range> decompose(CoordinateAxis1D axis) {
        Range range = null;
        if (axis != null) {
            List<Range> rangeList = axis.getRanges();
            if (rangeList != null) {
                if (rangeList.size() == 1) {
                    range = rangeList.get(0);
                } else {
                    // decomposing along a single range for axes with more than
                    // one is something we don't want to do.  This should only
                    // happen with X and Y axes (and this method shouldn't be
                    // called for those)
                    throw new IllegalArgumentException(
                            "Axis \"" + axis.getFullName() + "\" has coordinates with more that 1 dimension");
                }
            }
        }
        return decompose(range);
    }
    
    public static Iterable<Range> decompose(Range range) {
        return new SingleElementRangeDecomposer(range);
    }
    
    public static class SingleElementRangeDecomposer implements Iterable<Range> {
        public final Range range;
        public SingleElementRangeDecomposer(Range range) {
            this.range = range;
        }
        @Override
        public Iterator<Range>iterator() {
            return range == null ?
                Arrays.asList((Range)null).iterator() :
                new SingleElementRangeIterator(range);
        }
        public class SingleElementRangeIterator implements Iterator<Range> {
            private final Range range;
            private int next = 0;
            public SingleElementRangeIterator(Range range) {
                this.range = range;
            }
            @Override
            public boolean hasNext() {
                return next < range.length();
            }
            @Override
            public Range next() {
                Range r = null;
                try {
                    int e = range.element(next++);
                    // yes!  upper-bound is inclusive!  Yes I agree it's wierd!
                    r = new Range(e, e, 1);
                } catch (InvalidRangeException ignore) { /* why is this a checked exception? */ }
                return r;
            }
            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }   
        }
    }
}
