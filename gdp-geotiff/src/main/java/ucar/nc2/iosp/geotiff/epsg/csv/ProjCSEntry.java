package ucar.nc2.iosp.geotiff.epsg.csv;

import ucar.nc2.iosp.geotiff.epsg.GeogCS;
import ucar.nc2.iosp.geotiff.epsg.ProjCS;
import ucar.nc2.iosp.geotiff.epsg.UnitOfMeasure;

/**
 *
 * @author tkunicki
 */
public class ProjCSEntry implements CSVEntry, ProjCS {

    private int code;
    private String name;
    private int unitsOfMeasureCode;
    private int sourceGeogCSCode;
    private int coordOpCode;
    private int coordOpMethodCode;
    private int parameter1Code;
    private int parameter1UnitOfMeasureCode;
    private double parameter1Value;
    private int parameter2Code;
    private int parameter2UnitOfMeasureCode;
    private double parameter2Value;
    private int parameter3Code;
    private int parameter3UnitOfMeasureCode;
    private double parameter3Value;
    private int parameter4Code;
    private int parameter4UnitOfMeasureCode;
    private double parameter4Value;
    private int parameter5Code;
    private int parameter5UnitOfMeasureCode;
    private double parameter5Value;
    private int parameter6Code;
    private int parameter6UnitOfMeasureCode;
    private double parameter6Value;
    private int parameter7Code;
    private int parameter7UnitOfMeasureCode;
    private double parameter7Value;
    private GeogCS sourceGeogCS;
    private UnitOfMeasure unitOfMeasure;
    private UnitOfMeasure parameter1UnitOfMeasure;
    private UnitOfMeasure parameter2UnitOfMeasure;
    private UnitOfMeasure parameter3UnitOfMeasure;
    private UnitOfMeasure parameter4UnitOfMeasure;
    private UnitOfMeasure parameter5UnitOfMeasure;
    private UnitOfMeasure parameter6UnitOfMeasure;
    private UnitOfMeasure parameter7UnitOfMeasure;

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

    @Override
    public int getCoordOpCode() {
        return coordOpCode;
    }

    public void setCoordOpCode(int coordOpCode) {
        this.coordOpCode = coordOpCode;
    }

    @Override
    public int getCoordOpMethodCode() {
        return coordOpMethodCode;
    }

    public void setCoordOpMethodCode(int coordOpMethodCode) {
        this.coordOpMethodCode = coordOpMethodCode;
    }

    @Override
    public int getParameter1Code() {
        return parameter1Code;
    }

    public void setParameter1Code(int parameter1Code) {
        this.parameter1Code = parameter1Code;
    }

    public int getParameter1UnitOfMeasureCode() {
        return parameter1UnitOfMeasureCode;
    }

    public void setParameter1UnitOfMeasureCode(int parameter1UnitOfMeasureCode) {
        this.parameter1UnitOfMeasureCode = parameter1UnitOfMeasureCode;
    }

    @Override
    public double getParameter1Value() {
        return parameter1Value;
    }

    public void setParameter1Value(double parameter1Value) {
        this.parameter1Value = parameter1Value;
    }

    @Override
    public int getParameter2Code() {
        return parameter2Code;
    }

    public void setParameter2Code(int parameter2Code) {
        this.parameter2Code = parameter2Code;
    }

    public int getParameter2UnitOfMeasureCode() {
        return parameter2UnitOfMeasureCode;
    }

    public void setParameter2UnitOfMeasureCode(int parameter2UnitOfMeasureCode) {
        this.parameter2UnitOfMeasureCode = parameter2UnitOfMeasureCode;
    }

    @Override
    public double getParameter2Value() {
        return parameter2Value;
    }

    public void setParameter2Value(double parameter2Value) {
        this.parameter2Value = parameter2Value;
    }

    @Override
    public int getParameter3Code() {
        return parameter3Code;
    }

    public void setParameter3Code(int parameter3Code) {
        this.parameter3Code = parameter3Code;
    }

    public int getParameter3UnitOfMeasureCode() {
        return parameter3UnitOfMeasureCode;
    }

    public void setParameter3UnitOfMeasureCode(int parameter3UnitOfMeasureCode) {
        this.parameter3UnitOfMeasureCode = parameter3UnitOfMeasureCode;
    }

    @Override
    public double getParameter3Value() {
        return parameter3Value;
    }

    public void setParameter3Value(double parameter3Value) {
        this.parameter3Value = parameter3Value;
    }

    @Override
    public int getParameter4Code() {
        return parameter4Code;
    }

    public void setParameter4Code(int parameter4Code) {
        this.parameter4Code = parameter4Code;
    }

    public int getParameter4UnitOfMeasureCode() {
        return parameter4UnitOfMeasureCode;
    }

    public void setParameter4UnitOfMeasureCode(int parameter4UnitOfMeasureCode) {
        this.parameter4UnitOfMeasureCode = parameter4UnitOfMeasureCode;
    }

    @Override
    public double getParameter4Value() {
        return parameter4Value;
    }

    public void setParameter4Value(double parameter4Value) {
        this.parameter4Value = parameter4Value;
    }

    @Override
    public int getParameter5Code() {
        return parameter5Code;
    }

    public void setParameter5Code(int parameter5Code) {
        this.parameter5Code = parameter5Code;
    }

    public int getParameter5UnitOfMeasureCode() {
        return parameter5UnitOfMeasureCode;
    }

    public void setParameter5UnitOfMeasureCode(int parameter5UnitOfMeasureCode) {
        this.parameter5UnitOfMeasureCode = parameter5UnitOfMeasureCode;
    }

    @Override
    public double getParameter5Value() {
        return parameter5Value;
    }

    public void setParameter5Value(double parameter5Value) {
        this.parameter5Value = parameter5Value;
    }

    @Override
    public int getParameter6Code() {
        return parameter6Code;
    }

    public void setParameter6Code(int parameter6Code) {
        this.parameter6Code = parameter6Code;
    }

    public int getParameter6UnitOfMeasureCode() {
        return parameter6UnitOfMeasureCode;
    }

    public void setParameter6UnitOfMeasureCode(int parameter6UnitOfMeasureCode) {
        this.parameter6UnitOfMeasureCode = parameter6UnitOfMeasureCode;
    }

    @Override
    public double getParameter6Value() {
        return parameter6Value;
    }

    public void setParameter6Value(double parameter6Value) {
        this.parameter6Value = parameter6Value;
    }

    @Override
    public int getParameter7Code() {
        return parameter7Code;
    }

    public void setParameter7Code(int parameter7Code) {
        this.parameter7Code = parameter7Code;
    }

    public int getParameter7UnitOfMeasureCode() {
        return parameter7UnitOfMeasureCode;
    }

    public void setParameter7UnitOfMeasureCode(int parameter7UnitOfMeasureCode) {
        this.parameter7UnitOfMeasureCode = parameter7UnitOfMeasureCode;
    }

    @Override
    public double getParameter7Value() {
        return parameter7Value;
    }

    public void setParameter7Value(double parameter7Value) {
        this.parameter7Value = parameter7Value;
    }

    public int getSourceGeogCSCode() {
        return sourceGeogCSCode;
    }

    public void setSourceGeogCSCode(int sourceGeogCRSCode) {
        this.sourceGeogCSCode = sourceGeogCRSCode;
    }

    public int getUnitsOfMeasureCode() {
        return unitsOfMeasureCode;
    }

    public void setUnitsOfMeasureCode(int unitsOfMeasureCode) {
        this.unitsOfMeasureCode = unitsOfMeasureCode;
    }

    @Override
    public synchronized UnitOfMeasure getUnitOfMeasure() {
        if (unitOfMeasure == null) {
            unitOfMeasure = EPSG.findUnitOfMeasureByCode(unitsOfMeasureCode);
        }
        return unitOfMeasure;
    }

    @Override
    public GeogCS getSourceGeogCS() {
        if (sourceGeogCS == null) {
            sourceGeogCS = EPSG.findGeogCSByCode(sourceGeogCSCode);
        }
        return sourceGeogCS;
    }

    @Override
    public synchronized UnitOfMeasure getParameter1UnitOfMeasure() {
        if (parameter1UnitOfMeasure == null) {
            parameter1UnitOfMeasure = EPSG.findUnitOfMeasureByCode(parameter1UnitOfMeasureCode);
        }
        return parameter1UnitOfMeasure;
    }

    @Override
    public synchronized UnitOfMeasure getParameter2UnitOfMeasure() {
        if (parameter2UnitOfMeasure == null) {
            parameter2UnitOfMeasure = EPSG.findUnitOfMeasureByCode(parameter2UnitOfMeasureCode);
        }
        return parameter2UnitOfMeasure;
    }

    @Override
    public synchronized UnitOfMeasure getParameter3UnitOfMeasure() {
        if (parameter3UnitOfMeasure == null) {
            parameter3UnitOfMeasure = EPSG.findUnitOfMeasureByCode(parameter3UnitOfMeasureCode);
        }
        return parameter3UnitOfMeasure;
    }

    @Override
    public synchronized UnitOfMeasure getParameter4UnitOfMeasure() {
        if (parameter4UnitOfMeasure == null) {
            parameter4UnitOfMeasure = EPSG.findUnitOfMeasureByCode(parameter4UnitOfMeasureCode);
        }
        return parameter4UnitOfMeasure;
    }

    @Override
    public synchronized UnitOfMeasure getParameter5UnitOfMeasure() {
        if (parameter5UnitOfMeasure == null) {
            parameter5UnitOfMeasure = EPSG.findUnitOfMeasureByCode(parameter5UnitOfMeasureCode);
        }
        return parameter5UnitOfMeasure;
    }

    @Override
    public synchronized UnitOfMeasure getParameter6UnitOfMeasure() {
        if (parameter6UnitOfMeasure == null) {
            parameter6UnitOfMeasure = EPSG.findUnitOfMeasureByCode(parameter6UnitOfMeasureCode);
        }
        return parameter6UnitOfMeasure;
    }

    @Override
    public synchronized UnitOfMeasure getParameter7UnitOfMeasure() {
        if (parameter7UnitOfMeasure == null) {
            parameter7UnitOfMeasure = EPSG.findUnitOfMeasureByCode(parameter7UnitOfMeasureCode);
        }
        return parameter7UnitOfMeasure;
    }

    @Override
    public double getParameterValueByCode(int parameterCode) {
        // so lame
        if (parameter1Code == parameterCode) {
            return parameter1Value;
        }
        if (parameter2Code == parameterCode) {
            return parameter2Value;
        }
        if (parameter3Code == parameterCode) {
            return parameter3Value;
        }
        if (parameter4Code == parameterCode) {
            return parameter4Value;
        }
        if (parameter5Code == parameterCode) {
            return parameter5Value;
        }
        if (parameter6Code == parameterCode) {
            return parameter6Value;
        }
        if (parameter7Code == parameterCode) {
            return parameter7Value;
        }
        return Double.NaN;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ProjCSEntry other = (ProjCSEntry) obj;
        if (this.code != other.code) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 37 * hash + this.code;
        return hash;
    }
}
