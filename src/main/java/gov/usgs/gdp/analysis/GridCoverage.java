/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.usgs.gdp.analysis;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.opengis.feature.simple.SimpleFeature;
import ucar.nc2.dataset.CoordinateAxis1D;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDataset;
import ucar.nc2.dt.GridDatatype;
import ucar.unidata.geoloc.LatLonPointImpl;
import ucar.unidata.geoloc.LatLonRect;

/**
 *
 * @author tkunicki
 */
public class GridCoverage {

    private SimpleFeature simpleFeature;
    private GridDataset gridDataset;
    private String variableName;

    private double[][] coverage;

    private GeometryFactory geometryFactory;

    public GridCoverage(
            SimpleFeature simpleFeature,
            GridDataset gridDataset,
            String variableName) {
        this.simpleFeature = simpleFeature;
        this.gridDataset = gridDataset;
        this.variableName = variableName;

        this.geometryFactory = JTSFactoryFinder.getGeometryFactory(null);

        generateCoverage();
    }

    public double[][] getCoverage() {
        return coverage;
    }

    public double getCoverage(int xIndex, int yIndex) {
        return coverage[xIndex][yIndex];
    }

    private void generateCoverage() {

        double[][] coverageMask = null;

        Geometry sfg = (Geometry)simpleFeature.getDefaultGeometry();
        sfg = sfg.getEnvelope();
        Envelope sfge = sfg.getEnvelopeInternal();
        
        // llr is never read...
        LatLonRect llr = new LatLonRect(
                new LatLonPointImpl(sfge.getMinY(), sfge.getMinX()),
                new LatLonPointImpl(sfge.getMaxY(), sfge.getMaxX()));


        GridDatatype gdt = gridDataset.findGridDatatype(variableName);
        GridCoordSystem gcs = gdt.getCoordinateSystem();

        CoordinateAxis1D xAxis = (CoordinateAxis1D) gcs.getXHorizAxis();
        CoordinateAxis1D yAxis = (CoordinateAxis1D) gcs.getYHorizAxis();

        long xCount = xAxis.getSize();
        long yCount = yAxis.getSize();
        
        coverageMask = new double[(int) xCount][(int) yCount];

        for (int yIndex = 0; yIndex < yCount; ++yIndex) {
            double[] yCellEdges = yAxis.getCoordEdges(yIndex);
            for (int xIndex = 0; xIndex < xCount; ++ xIndex) {
                double[] xCellEdges = xAxis.getCoordEdges(xIndex);
                Envelope cellEnvelope = new Envelope(
                        xCellEdges[0], xCellEdges[1], yCellEdges[0], yCellEdges[1]);
                Geometry cellGeometry = geometryFactory.toGeometry(cellEnvelope);
                Geometry intersectGeometry = cellGeometry.intersection(sfg);
                coverageMask[xIndex][yIndex] =
                        intersectGeometry.getArea() / cellEnvelope.getArea();
            }
        }

        this.coverage = coverageMask;
    }

}
