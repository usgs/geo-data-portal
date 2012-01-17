package ucar.nc2.iosp.geotiff.epsg.csv;

import javax.measure.unit.Unit;
import ucar.nc2.iosp.geotiff.epsg.GTUnitOfMeasure;

/**
 *
 * @author tkunicki
 */
public class UnitOfMeasureEntry implements CSVEntry, GTUnitOfMeasure {

    private int code;
    private String name;
    private String type;
    private int targetUnitOfMeasureCode;
    private double factorB = Double.NaN;
    private double factorC = Double.NaN;

    @Override
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public int getTargetUnitOfMeasureCode() {
        return targetUnitOfMeasureCode;
    }
    
    public void setTargetUnitOfMeasureCode(int targetUnitOfMeasureCode) {
        this.targetUnitOfMeasureCode = targetUnitOfMeasureCode;
    }

    @Override
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    
    public double getFactorB() {
        return factorB;
    }
    
    public void setFactorB(double factorB) {
        this.factorB = factorB;
    }
    
    public double getFactorC() {
        return factorC;
    }
    
    public void setFactorC(double factorC) {
        this.factorC = factorC;
    }

    @Override
    public Unit<?> getUnit() {
        return UnitOfMeasureUtil.convert(this);
    }

    @Override
    public GTUnitOfMeasure getTargetUnitOfMeasure() {
        return CSVEPSGFactory.getInstance().findUnitOfMeasureByCode(targetUnitOfMeasureCode);
    }
    
    public double convertToTargetUnitOfMeasure(double value) {
        return getUnit().getConverterTo(getTargetUnitOfMeasure().getUnit()).convert(value);
    }
}
