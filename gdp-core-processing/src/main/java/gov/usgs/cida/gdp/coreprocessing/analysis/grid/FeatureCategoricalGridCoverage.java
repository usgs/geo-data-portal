package gov.usgs.cida.gdp.coreprocessing.analysis.grid;

import com.google.common.base.Preconditions;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.prep.PreparedGeometry;
import com.vividsolutions.jts.geom.prep.PreparedGeometryFactory;
import gov.usgs.cida.gdp.coreprocessing.analysis.grid.GridUtility.IndexToCoordinateBuilder;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import org.geotools.data.crs.ReprojectFeatureResults;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.SchemaException;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.geometry.BoundingBox;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDataset;
import ucar.nc2.dt.GridDatatype;
import ucar.unidata.geoloc.LatLonPointImpl;
import ucar.unidata.geoloc.LatLonRect;

/**
 *
 * @author tkunicki
 */
public class FeatureCategoricalGridCoverage {

    public static void execute(
            FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection,
            String attributeName,
            GridDataset gridDataset,
            String variableName,
            BufferedWriter writer,
            String delimiter)
            throws IOException, InvalidRangeException, FactoryException, TransformException, SchemaException
    {

        GridDatatype gdt = gridDataset.findGridDatatype(variableName);
        Preconditions.checkNotNull(gdt, "Variable named %s not found in gridDataset", variableName);

        GridType gt = GridType.findGridType(gdt.getCoordinateSystem());
        if (gt != GridType.YX) {
            throw new IllegalStateException("Currently require y-x or t-y-x grid for this operation");
        }

        AttributeDescriptor attributeDescriptor =
                featureCollection.getSchema().getDescriptor(attributeName);
        if (attributeDescriptor == null) {
            throw new IllegalArgumentException(
                    "Attribute " + attributeName + " not found in FeatureCollection.");
        }

        boolean attributeComparable = Comparable.class.isAssignableFrom(
                attributeDescriptor.getType().getBinding());

        Map<Object, Map<Integer, Integer>> attributeToCategoricalCoverageMap = attributeComparable ?
                // rely on Comparable to sort
                new TreeMap<Object, Map<Integer, Integer>>() :
                // use order from FeatureCollection.iterator();
                new LinkedHashMap<Object, Map<Integer, Integer>>();
        SortedSet<Integer> categorySet = new TreeSet<Integer>();


        // not sure how well this will work... alternative
        featureCollection = new ReprojectFeatureResults(
                featureCollection,
                DefaultGeographicCRS.WGS84);

        Iterator<SimpleFeature> featureIterator = featureCollection.iterator();

        try {
            while (featureIterator.hasNext()) {

                SimpleFeature feature = featureIterator.next();
                Object attribute = feature.getAttribute(attributeName);

                if (attribute != null) {

                    Map<Integer, Integer> categoricalCoverageMap =
                            attributeToCategoricalCoverageMap.get(attribute);
                    if (categoricalCoverageMap == null) {
                        categoricalCoverageMap = new TreeMap<Integer, Integer>();
                        attributeToCategoricalCoverageMap.put(attribute, categoricalCoverageMap);
                    }

                    BoundingBox featureBoundingBox = feature.getBounds();

                    LatLonRect featureLatLonRect = new LatLonRect(
                            new LatLonPointImpl(
                                featureBoundingBox.getMinY(), featureBoundingBox.getMinX()),
                            new LatLonPointImpl(
                                featureBoundingBox.getMaxY(), featureBoundingBox.getMaxX()));

                    Geometry featureGeometry = (Geometry)feature.getDefaultGeometry();

                    Range[] ranges = GridUtility.getRangesFromLatLonRect(
                            featureLatLonRect, gdt.getCoordinateSystem());

                    GridDatatype featureGridDataType = gdt.makeSubset(null, null, null, null, ranges[1], ranges[0]);

                    PreparedGeometry preparedGeometry = PreparedGeometryFactory.prepare(featureGeometry);

                    GridCellTraverser traverser = new GridCellTraverser(featureGridDataType);
                    traverser.traverse(new FeatureGridCellVisitor(preparedGeometry, categoricalCoverageMap));

                    categorySet.addAll(categoricalCoverageMap.keySet());
                }
            }
        } finally {
            featureCollection.close(featureIterator);
        }

        SimpleDelimitedWriter delimitedWriter = new SimpleDelimitedWriter(delimiter, writer);

        Collection headerRow = new ArrayList<Object>();
        for (Integer i : categorySet) {
            headerRow.add("Category");
        }
        delimitedWriter.writeRow(null, headerRow);
        
        headerRow.clear();
        headerRow.addAll(categorySet);
        headerRow.add("Sample Count");

        delimitedWriter.writeRow("Attribute", headerRow);

        for (Map.Entry<Object, Map<Integer, Integer>> entry : attributeToCategoricalCoverageMap.entrySet()) {
            Object attributeValue = entry.getKey();
            Map<Integer, Integer> categoricalCoverageMap = entry.getValue();

            List<Number> rowValues = new ArrayList<Number>();
            int total = 0;
            // gather total sample count for attribute
            for (Integer count : categoricalCoverageMap.values()) {
                if (count != null) {
                    total += count;
                }
            }
            // calculate and store fraction for each categorical type
            for (Integer category : categorySet) {
                Integer count = categoricalCoverageMap.get(category);
                float fraction = count == null ?  0 : (float) count / (float) total;
                rowValues.add(fraction);
            }
            rowValues.add(total);

            delimitedWriter.writeRow(attributeValue.toString(), rowValues);
        }

    }

    protected static class FeatureGridCellVisitor extends GridCellVisitor {

        GeometryFactory geometryFactory = new GeometryFactory();
        IndexToCoordinateBuilder coordinateBuilder;
        PreparedGeometry preparedGeometry;
        Map<Integer, Integer> categoryMap;

        protected FeatureGridCellVisitor(
                PreparedGeometry preparedGeometry,
                Map<Integer, Integer> categoryMap) {
            this.preparedGeometry = preparedGeometry;
            this.categoryMap = categoryMap;
        }

        @Override
        public void traverseStart(GridCoordSystem gridCoordSystem) {
            coordinateBuilder = GridUtility.generateIndexToCellCenterCoordinateBuilder(gridCoordSystem);
        }

        @Override
        public void processGridCell(int xCellIndex, int yCellIndex, double value) {
            Coordinate coordinate =
                    coordinateBuilder.getCoordinate(xCellIndex, yCellIndex);
            if (preparedGeometry.contains(geometryFactory.createPoint(coordinate))) {
                Integer key = (int) value;
                Integer count = categoryMap.get(key);
                count =  count == null ? 1 : count + 1;
                categoryMap.put(key, count);
            }
            
        }

    }

    protected static class SimpleDelimitedWriter {

        private String delimiter;
        private BufferedWriter writer;

        private StringBuilder lineSB = new StringBuilder();

        public SimpleDelimitedWriter(
                String delimiter,
                BufferedWriter writer) {

            this.delimiter = delimiter;
            this.writer = writer;

            lineSB = new StringBuilder();
        }

        public void writeRow(
                String rowLabel,
                Collection<? extends Object> rowValues)
                throws IOException
        {
            lineSB.setLength(0);
            if (rowLabel != null) {
                lineSB.append(rowLabel);
            }

            for (Object rowValue : rowValues) {
                lineSB.append(delimiter).append(rowValue);
            }
            writer.write(lineSB.toString());
            writer.newLine();
        }
    }

}
