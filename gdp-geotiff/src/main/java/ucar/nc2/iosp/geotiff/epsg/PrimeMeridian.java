package ucar.nc2.iosp.geotiff.epsg;

/**
 *
 * @author tkunicki
 */
public interface PrimeMeridian {

    public int getCode();

    public String getName();

    public double getLongitude();

    public UnitOfMeasure getUnitOfMeasure();
}
