package ucar.nc2.iosp.geotiff.epsg;

/**
 *
 * @author tkunicki
 */
public interface GTEllipsoid {

    public int getCode();

    public String getName();

    public double getSemiMajorAxis();

    public double getSemiMinorAxis();

    public double getInverseFlattening();

    public GTUnitOfMeasure getUnitOfMeasure();
}
