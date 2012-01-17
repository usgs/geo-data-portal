/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ucar.nc2.iosp.geotiff.epsg;

/**
 *
 * @author tkunicki
 */
public interface GTGeogCS {

    public int getCode();

    public String getName();

    public GTDatum getDatum();

    public GTUnitOfMeasure getUnitOfMeasure();

    public GTEllipsoid getEllipsoid();

    public GTPrimeMeridian getPrimeMeridian();

}
