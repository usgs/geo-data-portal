/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.usgs.gdp.analysis;

import org.geotools.geometry.jts.JTSFactoryFinder;

import ucar.nc2.dataset.CoordinateAxis1D;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDatatype;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

/**
 *
 * @author tkunicki
 */
public class GridCoverage {

    private Geometry geometry;
    private GridDatatype gridDatatype;

    private double[] cellCoverageFraction;
    private double[] featureCoverageFraction;
    private double[] featureCoverage;

    private GeometryFactory geometryFactory;

    private int xCount;
    private int yCount;

    public GridCoverage(Geometry geometry, GridDatatype gridDatatype)
    {
        this.geometry = geometry;
        this.gridDatatype = gridDatatype;

        this.geometryFactory = JTSFactoryFinder.getGeometryFactory(null);

        generateCoverage();
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public GridDatatype getGridDatatype() {
        return gridDatatype;
    }

    public double getCellCoverageFraction(int yxIndex) {
        return cellCoverageFraction[yxIndex];
    }

    public double getCellCoverageFraction(int xIndex, int yIndex) {
        return cellCoverageFraction[xIndex + yIndex * yCount];
    }

    public double getFeatureCoverage(int yxIndex) {
        return featureCoverage[yxIndex];
    }

    public double getFeatureCoverage(int xIndex, int yIndex) {
        return featureCoverage[xIndex + yIndex * yCount];
    }

    public double getFeatureCoverageFraction(int yxIndex) {
        return featureCoverageFraction[yxIndex];
    }

    public double getFeatureCoverageFraction(int xIndex, int yIndex) {
        return featureCoverageFraction[xIndex + yIndex * yCount];
    }

    private void generateCoverage() {

        GridCoordSystem gcs = gridDatatype.getCoordinateSystem();

        CoordinateAxis1D xAxis = (CoordinateAxis1D) gcs.getXHorizAxis();
        CoordinateAxis1D yAxis = (CoordinateAxis1D) gcs.getYHorizAxis();

        xCount = (int) xAxis.getSize();
        yCount = (int) yAxis.getSize();

        int yxCount = xCount * yCount;

        cellCoverageFraction = new double[yxCount];
        featureCoverage = new double[yxCount];
        featureCoverageFraction = new double[yxCount];

        double featureArea = geometry.getArea();

        for (int yIndex = 0; yIndex < yCount; ++yIndex) {
            int yOffset = yIndex * xCount;
            double[] yCellEdges = yAxis.getCoordEdges(yIndex);
            for (int xIndex = 0; xIndex < xCount; ++ xIndex) {
                double[] xCellEdges = xAxis.getCoordEdges(xIndex);
                Envelope cellEnvelope = new Envelope(
                        xCellEdges[0], xCellEdges[1], yCellEdges[0], yCellEdges[1]);
                Geometry cellGeometry = geometryFactory.toGeometry(cellEnvelope);
                Geometry intersectGeometry = cellGeometry.intersection(geometry);
                int xyIndex = yOffset + xIndex;
                cellCoverageFraction[xyIndex] = intersectGeometry.getArea() / cellEnvelope.getArea();
                featureCoverage[xyIndex] = intersectGeometry.getArea();
                featureCoverageFraction[xyIndex] = featureCoverage[xyIndex] / featureArea;
            }
        }
    }

}
