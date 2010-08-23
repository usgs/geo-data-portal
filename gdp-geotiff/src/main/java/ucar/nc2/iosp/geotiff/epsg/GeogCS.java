/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ucar.nc2.iosp.geotiff.epsg;

/**
 *
 * @author tkunicki
 */
public interface GeogCS {

    public int getCode();

    public String getName();

    public Datum getDatum();

    public Datum getGreenwichDatum();

    public UnitOfMeasure getUnitOfMeasure();

    public Ellipsoid getEllipsoid();

    public PrimeMeridian getPrimeMeridian();

}
