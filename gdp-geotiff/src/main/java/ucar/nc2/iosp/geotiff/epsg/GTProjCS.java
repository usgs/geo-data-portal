package ucar.nc2.iosp.geotiff.epsg;

/**
 *
 * @author tkunicki
 */
public interface GTProjCS {

    public final static int MISSING_CODE = Integer.MIN_VALUE;
    public final static double MISSING_VALUE = Double.NaN;
    
    public int getCode();

    public String getName();

    public GTUnitOfMeasure getUnitOfMeasure();

    public GTGeogCS getSourceGeogCS();

    public int getCoordOpCode();

    public int getCoordOpMethodCode();

    public int getParameter1Code();
    public GTUnitOfMeasure getParameter1UnitOfMeasure();
    public double getParameter1Value();

    public int getParameter2Code();
    public GTUnitOfMeasure getParameter2UnitOfMeasure();
    public double getParameter2Value();

    public int getParameter3Code();
    public GTUnitOfMeasure getParameter3UnitOfMeasure();
    public double getParameter3Value();

    public int getParameter4Code();
    public GTUnitOfMeasure getParameter4UnitOfMeasure();
    public double getParameter4Value();

    public int getParameter5Code();
    public GTUnitOfMeasure getParameter5UnitOfMeasure();
    public double getParameter5Value();

    public int getParameter6Code();
    public GTUnitOfMeasure getParameter6UnitOfMeasure();
    public double getParameter6Value();

    public int getParameter7Code();
    public GTUnitOfMeasure getParameter7UnitOfMeasure();
    public double getParameter7Value();

    public double getParameterValueByCode(int code);

}
