package ucar.nc2.iosp.geotiff.epsg;

/**
 *
 * @author tkunicki
 */
public interface GTDatum {

    enum Type {
        geodetic,
        vertical,
        engineering
    }
    public int getCode();

    public String getName();

    public Type getType();

    public GTEllipsoid getEllipsoid();

    public GTPrimeMeridian getPrimeMeridian();
    
}
