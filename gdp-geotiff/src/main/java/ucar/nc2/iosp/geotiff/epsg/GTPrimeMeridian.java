package ucar.nc2.iosp.geotiff.epsg;

/**
 *
 * @author tkunicki
 */
public interface GTPrimeMeridian {

    public int getCode();

    public String getName();

    public double getLongitude();

    public GTUnitOfMeasure getUnitOfMeasure();
}
