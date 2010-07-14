package ucar.nc2.iosp.geotiff.epsg;

/**
 *
 * @author tkunicki
 */
public interface ProjCS {

    public int getCode();

    public String getName();

    public UnitOfMeasure getUnitOfMeasure();

    public GeogCS getSourceGeogCS();

    public int getCoordOpCode();

    public int getCoordOpMethodCode();

    public int getParameter1Code();
    public UnitOfMeasure getParameter1UnitOfMeasure();
    public double getParameter1Value();

    public int getParameter2Code();
    public UnitOfMeasure getParameter2UnitOfMeasure();
    public double getParameter2Value();

    public int getParameter3Code();
    public UnitOfMeasure getParameter3UnitOfMeasure();
    public double getParameter3Value();

    public int getParameter4Code();
    public UnitOfMeasure getParameter4UnitOfMeasure();
    public double getParameter4Value();

    public int getParameter5Code();
    public UnitOfMeasure getParameter5UnitOfMeasure();
    public double getParameter5Value();

    public int getParameter6Code();
    public UnitOfMeasure getParameter6UnitOfMeasure();
    public double getParameter6Value();

    public int getParameter7Code();
    public UnitOfMeasure getParameter7UnitOfMeasure();
    public double getParameter7Value();

    public double getParameterValueByCode(int code);

}
