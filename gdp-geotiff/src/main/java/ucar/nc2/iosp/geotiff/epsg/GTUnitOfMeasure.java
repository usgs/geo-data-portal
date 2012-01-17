package ucar.nc2.iosp.geotiff.epsg;

import javax.measure.unit.Unit;

/**
 *
 * @author tkunicki
 */
public interface GTUnitOfMeasure {

    public int getCode();

    public String getName();

    public String getType();

    public GTUnitOfMeasure getTargetUnitOfMeasure();
    
    public Unit<?> getUnit();
        
}
