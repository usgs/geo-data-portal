package gov.usgs.cida.gdp.coreprocessing.analysis.grid;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.prep.PreparedGeometry;
import com.vividsolutions.jts.geom.prep.PreparedGeometryFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.geotools.feature.FeatureCollection;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import ucar.nc2.dt.GridCoordSystem;

/**
 *
 * @author tkunicki
 */
public abstract class GridCellCoverageFactory {

	public static GridCellCoverageByIndex generateFeatureAttributeCoverageByIndex(
            FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection,
            String attributeName,
            GridCoordSystem gridCoordinateSystem)
            throws FactoryException, TransformException, InvalidRangeException
    {

        AttributeDescriptor attributeDescriptor =
                featureCollection.getSchema().getDescriptor(attributeName);
        if (attributeDescriptor == null) {
            throw new IllegalArgumentException(
                    "Attribute " + attributeName + " not found in FeatureCollection.");
        }

		boolean attributeComparable = Comparable.class.isAssignableFrom(
                attributeDescriptor.getType().getBinding());

        Set<Object> attributeSet = attributeComparable ?
                // rely on Comparable to sort
                new TreeSet<Object>() :
                // use order from FeatureCollection.iterator();
                new LinkedHashSet<Object>();

		GridCellGeometry gridCellGeometry = new GridCellGeometry(gridCoordinateSystem);

		GridCellCoverageByIndex coverageByIndex = new GridCellCoverageByIndex(
				gridCellGeometry.getCellCountX(),
				gridCellGeometry.getCellCountY());

        Iterator<SimpleFeature> featureIterator = featureCollection.iterator();
        try {
            while (featureIterator.hasNext()) {

                SimpleFeature feature = featureIterator.next();
                Object attribute = feature.getAttribute(attributeName);

                if (attribute != null) {

					boolean exists = !attributeSet.add(attribute);

					List<GridCellAttributeCoverage> attributeCoverage = calculateCoverage(gridCellGeometry, feature);
                    
					if (attributeCoverage.size() > 0) {
						for(GridCellAttributeCoverage c : attributeCoverage) {
							int yxCellIndex = gridCellGeometry.calculateYXIndex(
									c.xCellIndex, c.yCellIndex);
							GridCellIndexCoverage[] currentCoverage = coverageByIndex.getCoverage(yxCellIndex);
							if (currentCoverage == null) {
								coverageByIndex.putCoverage(
										yxCellIndex,
										new GridCellIndexCoverage[] { new GridCellIndexCoverage(attribute, c.coverage) } );
							} else if (exists) {
								final int count = currentCoverage.length;
								int found = -1;
								// O(n), ouch...
								for (int index = 0; index < count && found < 0; ++index ) {
									if (currentCoverage[index].attribute.equals(attribute)) {
										found = index;
									}
								}
								if (found < 0) {
									GridCellIndexCoverage[] newCoverage = new GridCellIndexCoverage[count + 1];
									System.arraycopy(currentCoverage, 0, newCoverage, 0, count);
									newCoverage[count] = new GridCellIndexCoverage(attribute, c.coverage);
									coverageByIndex.putCoverage(yxCellIndex, newCoverage);
								} else {
									double coverage = c.coverage + currentCoverage[found].coverage;
									if (coverage > 1) {
										coverage = 1;
									}
									currentCoverage[found] = new GridCellIndexCoverage(attribute, coverage);
								}
							} else {
								final int length = currentCoverage.length;
								GridCellIndexCoverage[] newCoverage = new GridCellIndexCoverage[length + 1];
								System.arraycopy(currentCoverage, 0, newCoverage, 0, length);
								newCoverage[length] = new GridCellIndexCoverage(attribute, c.coverage);
								coverageByIndex.putCoverage(yxCellIndex, newCoverage);
							}
						}
						attributeCoverage.clear();
                    }
                }
            }
			coverageByIndex.attributeValueList = Collections.unmodifiableList(new ArrayList<Object>(attributeSet));
        } finally {
            featureCollection.close(featureIterator);
        }
        return coverageByIndex;
    }

	// meant to be keyed by index
	public static class GridCellIndexCoverage {
		public final Object attribute;
		public final double coverage;
		public GridCellIndexCoverage(Object attribute, double coverage) {
			this.attribute = attribute;
			this.coverage = coverage;
		}
	}

	// meant to be keyed by attribute
	public static class GridCellAttributeCoverage {
		public final int xCellIndex;
		public final int yCellIndex;
		public final double coverage;
		public GridCellAttributeCoverage(int xCellIndex, int yCellIndex, double coverage) {
			this.xCellIndex = xCellIndex;
			this.yCellIndex = yCellIndex;
			this.coverage = coverage;
		}
	}

	public static List<GridCellAttributeCoverage> calculateCoverage(GridCellGeometry gridCellGeometry, SimpleFeature feature)
            throws FactoryException, TransformException, InvalidRangeException {

		Geometry geometry = (Geometry)feature.getDefaultGeometry();
		CoordinateReferenceSystem geometryCRS = feature.getFeatureType().getCoordinateReferenceSystem();

		final int xCellCount = gridCellGeometry.getCellCountX();

		final Range[] ranges = GridUtility.getXYRangesFromBoundingBox(
			feature.getBounds(),
			gridCellGeometry.getGridCoordSystem(),
            false);
		final int xCellMin = ranges[0].first();
		final int xCellMax = ranges[0].last() + 1; // last() returns inclusive, we want exclulsive
		final int yCellMin = ranges[1].first();
		final int yCellMax = ranges[1].last() + 1; // last() returns inclusive, we want exclulsive

        MathTransform transform = CRS.findMathTransform(gridCellGeometry.getGridCRS(), geometryCRS, true);
        PreparedGeometry preparedGeometry = PreparedGeometryFactory.prepare(geometry);

		List<GridCellAttributeCoverage> coverageList = new ArrayList<GridCellAttributeCoverage>();
        for (int yIndex = yCellMin; yIndex < yCellMax; ++yIndex) {
            int yOffset = yIndex * xCellCount;
            for (int xIndex = xCellMin; xIndex < xCellMax; ++xIndex) {
                int yxIndex = yOffset + xIndex;
                Geometry cellGeometry = JTS.transform(
						gridCellGeometry.getCellGeometryQuick(yxIndex),
						transform);
                if (preparedGeometry.intersects(cellGeometry)) {

                    if (preparedGeometry.containsProperly(cellGeometry)) {
                        coverageList.add(
								new GridCellAttributeCoverage(xIndex, yIndex, 1d));
                    } else {
                        Geometry intersectGeometry = geometry.intersection(cellGeometry);
                        coverageList.add(
								new GridCellAttributeCoverage(
									xIndex,
									yIndex,
									intersectGeometry.getArea() / cellGeometry.getArea()));
                    }
                }
            }
        }
		return coverageList;
    }

	public static class GridCellCoverageByIndex {

		final GridCellIndexCoverage[][] coverages;
		final int xCellCount;
		final int yCellCount;

		List<Object> attributeValueList;

		GridCellCoverageByIndex(int xCellCount, int yCellCount) {
			this.xCellCount = xCellCount;
			this.yCellCount = yCellCount;
			int cellCount = xCellCount * yCellCount;
			coverages = new GridCellIndexCoverage[cellCount][] ;
		}

		public List<GridCellIndexCoverage> getCoverageList(int xIndex, int yIndex) {
			return getCoverageList(xIndex + yIndex * xCellCount);
		}

		public List<GridCellIndexCoverage> getCoverageList(int yxIndex) {
			return coverages[yxIndex] == null ? null :  Arrays.asList(coverages[yxIndex]);
		}

		public List<Object> getAttributeValueList() {
			return attributeValueList;
		}

		private GridCellIndexCoverage[] getCoverage(int yxIndex) {
			return coverages[yxIndex];
		}

		private void putCoverage(int yxIndex,  GridCellIndexCoverage[] coverageList) {
			coverages[yxIndex] = coverageList;
		}
	}

}
