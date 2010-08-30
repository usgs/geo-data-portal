/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usgs.cida.gdp.coreprocessing.analysis;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.geometry.BoundingBox;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.operation.TransformException;
import ucar.unidata.geoloc.LatLonPointImpl;
import ucar.unidata.geoloc.LatLonRect;

/**
 *
 * @author tkunicki
 */
public class GeoToolsNetCDFUtility {

    public static LatLonRect getLatLonRectFromEnvelope(ReferencedEnvelope envelope) throws TransformException {
        return getLatLonRectFromEnvelope(envelope, DefaultGeographicCRS.WGS84);
    }

    public static LatLonRect getLatLonRectFromEnvelope(ReferencedEnvelope envelope, GeographicCRS crs) throws TransformException {
        BoundingBox latLonBoundingBox = envelope.toBounds(crs);
        LatLonRect llr = new LatLonRect(
                new LatLonPointImpl(latLonBoundingBox.getMinY(), latLonBoundingBox.getMinX()),
                new LatLonPointImpl(latLonBoundingBox.getMaxY(), latLonBoundingBox.getMaxX()));
        return llr;
    }
}
