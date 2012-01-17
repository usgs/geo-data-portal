package ucar.nc2.iosp.geotiff.epsg;

/**
 *
 * @author tkunicki
 */
public interface EPSGFactory {
    
    public GTGeogCS findGeogCSByCode(int code);

    public GTGeogCS findGeogCSByDatum(GTDatum datum);

    public GTProjCS findProjCSByCode(int code);
    
    public GTDatum findDatumByCode(int code);
    
    public GTEllipsoid findEllipsoidByCode(int code);

    public GTPrimeMeridian findPrimeMeridianByCode(int code);

    public GTUnitOfMeasure findUnitOfMeasureByCode(int code);
    
}
