package ucar.nc2.iosp.geotiff.epsg;

/**
 *
 * @author tkunicki
 */
public interface UnitOfMeasure {

    public int getCode();

    public String getName();

    public String getType();

    public UnitOfMeasure getTargetUnitOfMeasure();

    public double getFactorB();

    public double getFactorC();
    
}
