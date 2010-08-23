package ucar.nc2.iosp.geotiff.epsg;

/**
 *
 * @author tkunicki
 */
public interface Datum {

    enum Type {
        geodetic,
        vertical,
        engineering
    }
    public int getCode();

    public String getName();

    public Type getType();

    public Ellipsoid getEllipsoid();

    public PrimeMeridian getPrimeMeridian();
    
}
