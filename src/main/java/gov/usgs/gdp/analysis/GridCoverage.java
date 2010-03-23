package gov.usgs.gdp.analysis;

import org.geotools.geometry.jts.JTSFactoryFinder;

import ucar.nc2.dataset.CoordinateAxis1D;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDatatype;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import ucar.unidata.geoloc.LatLonPointImpl;
import ucar.unidata.geoloc.Projection;
import ucar.unidata.geoloc.ProjectionPointImpl;

/**
 *
 * @author tkunicki
 */
public class GridCoverage {
    private Geometry geometry;
    private CoordinateReferenceSystem geometryCRS;
    private GridDatatype gridDatatype;

    private double[][] cellCoverageFraction;
    private double[][] featureCoverageFraction;
    private double[][] featureCoverage;

    public GridCoverage(Geometry geometry, CoordinateReferenceSystem geometryCRS, GridDatatype gridDatatype)
            throws FactoryException, TransformException {
        this.geometry = geometry;
        this.geometryCRS = geometryCRS;
        this.gridDatatype = gridDatatype;

        generateCoverage();
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public GridDatatype getGridDatatype() {
        return gridDatatype;
    }

    public double getCellCoverageFraction(int latIndex, int lonIndex) {
        return cellCoverageFraction[latIndex][lonIndex];
    }

    public double getFeatureCoverage(int latIndex, int lonIndex) {
        return featureCoverage[latIndex][lonIndex];
    }

    public double getFeatureCoverageFraction(int latIndex, int lonIndex) {
        return featureCoverageFraction[latIndex][lonIndex];
    }

    private void generateCoverage() throws FactoryException, TransformException {
        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);
        MathTransform latLonTransform = CRS.findMathTransform(geometryCRS, DefaultGeographicCRS.WGS84, true);
        Geometry latLonGeom = JTS.transform(geometry, latLonTransform);

        double[][] latLonCellEdges = getLatLonCellEdges(gridDatatype.getCoordinateSystem());
        double[] latCellEdges = latLonCellEdges[0];
        double[] lonCellEdges = latLonCellEdges[1];

        cellCoverageFraction    = new double[latCellEdges.length - 1][lonCellEdges.length - 1];
        featureCoverageFraction = new double[latCellEdges.length - 1][lonCellEdges.length - 1];
        featureCoverage         = new double[latCellEdges.length - 1][lonCellEdges.length - 1];

        double featureArea = latLonGeom.getArea();

        for (int yIndex = 0; yIndex < latCellEdges.length - 1; ++yIndex) {
            for (int xIndex = 0; xIndex < lonCellEdges.length - 1; ++xIndex) {
                Envelope cellEnvelope = new Envelope(
                        lonCellEdges[xIndex], lonCellEdges[xIndex + 1], latCellEdges[yIndex], latCellEdges[yIndex + 1]);
                Geometry cellGeometry = geometryFactory.toGeometry(cellEnvelope);
                Geometry intersectGeometry = cellGeometry.intersection(latLonGeom);

                cellCoverageFraction[yIndex][xIndex] = intersectGeometry.getArea() / cellEnvelope.getArea();
                featureCoverage[yIndex][xIndex] = intersectGeometry.getArea();
                featureCoverageFraction[yIndex][xIndex] = featureCoverage[yIndex][xIndex] / featureArea;
            }
        }
    }

    public static double[][] getLatLonCellEdges(GridCoordSystem gcs) {
        CoordinateAxis1D yAxis = (CoordinateAxis1D) gcs.getYHorizAxis();
        CoordinateAxis1D xAxis = (CoordinateAxis1D) gcs.getXHorizAxis();
        double[] yCellEdges = yAxis.getCoordEdges();
        double[] xCellEdges = xAxis.getCoordEdges();

        if (!gcs.isLatLon()) {
            // Project to lat/lon.
            Projection proj = gcs.getProjection();
            ProjectionPointImpl projPoint   = new ProjectionPointImpl();
            LatLonPointImpl     latLonPoint = new LatLonPointImpl();

            for (int iY = 0; iY < yCellEdges.length; ++iY) {
                for (int iX = 0; iX < xCellEdges.length; ++iX) {
                    projPoint.setLocation(xCellEdges[iX], yCellEdges[iY]);
                    proj.projToLatLon(projPoint, latLonPoint);
                    yCellEdges[iY] = latLonPoint.getLatitude();
                    xCellEdges[iX] = latLonPoint.getLongitude();
                }
            }
        }

        return new double[][] { yCellEdges, xCellEdges };
    }
}
