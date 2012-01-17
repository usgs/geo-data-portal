package ucar.nc2.iosp.geotiff.epsg.csv;

import ucar.nc2.iosp.geotiff.epsg.GTGeogCS;
import ucar.nc2.iosp.geotiff.epsg.GTProjCS;
import ucar.nc2.iosp.geotiff.epsg.GTUnitOfMeasure;

/**
 *
 * @author tkunicki
 */
public class ProjCSEntry implements CSVEntry, GTProjCS {

    private int code;
    private String name;
    private int unitsOfMeasureCode;
    private int sourceGeogCSCode;
    private int coordOpCode;
    private int coordOpMethodCode;
    
    private int parameter1Code = MISSING_CODE;
    private int parameter1UnitOfMeasureCode = MISSING_CODE;;
    private double parameter1Value = MISSING_VALUE;
    
    private int parameter2Code = MISSING_CODE;
    private int parameter2UnitOfMeasureCode = MISSING_CODE;
    private double parameter2Value = MISSING_VALUE;
    
    private int parameter3Code = MISSING_CODE;
    private int parameter3UnitOfMeasureCode = MISSING_CODE;
    private double parameter3Value = MISSING_VALUE;
    
    private int parameter4Code = MISSING_CODE;
    private int parameter4UnitOfMeasureCode = MISSING_CODE;
    private double parameter4Value = MISSING_VALUE;
    
    private int parameter5Code = MISSING_CODE;
    private int parameter5UnitOfMeasureCode = MISSING_CODE;
    private double parameter5Value = MISSING_VALUE;
    
    private int parameter6Code = MISSING_CODE;
    private int parameter6UnitOfMeasureCode = MISSING_CODE;
    private double parameter6Value = MISSING_VALUE;
    
    private int parameter7Code = MISSING_CODE;
    private int parameter7UnitOfMeasureCode = MISSING_CODE;
    private double parameter7Value = MISSING_VALUE;

    private GeogCSEntry sourceGeogCS;
    private UnitOfMeasureEntry unitOfMeasure;

    private UnitOfMeasureEntry parameter1UnitOfMeasure;
    private UnitOfMeasureEntry parameter2UnitOfMeasure;
    private UnitOfMeasureEntry parameter3UnitOfMeasure;
    private UnitOfMeasureEntry parameter4UnitOfMeasure;
    private UnitOfMeasureEntry parameter5UnitOfMeasure;
    private UnitOfMeasureEntry parameter6UnitOfMeasure;
    private UnitOfMeasureEntry parameter7UnitOfMeasure;

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
    public synchronized GTUnitOfMeasure getUnitOfMeasure() {
        if (unitOfMeasure == null) {
            unitOfMeasure = CSVEPSGFactory.getInstance().findUnitOfMeasureByCode(unitsOfMeasureCode);
        }
        return unitOfMeasure;
    }

    @Override
    public GTGeogCS getSourceGeogCS() {
        if (sourceGeogCS == null) {
            sourceGeogCS = CSVEPSGFactory.getInstance().findGeogCSByCode(sourceGeogCSCode);
        }
        return sourceGeogCS;
    }

    @Override
    public synchronized UnitOfMeasureEntry getParameter1UnitOfMeasure() {
        if (parameter1UnitOfMeasure == null && parameter1UnitOfMeasureCode != MISSING_CODE) {
            parameter1UnitOfMeasure = CSVEPSGFactory.getInstance().findUnitOfMeasureByCode(parameter1UnitOfMeasureCode);
        }
        return parameter1UnitOfMeasure;
    }

    @Override
    public synchronized UnitOfMeasureEntry getParameter2UnitOfMeasure() {
        if (parameter2UnitOfMeasure == null && parameter4UnitOfMeasureCode != MISSING_CODE) {
            parameter2UnitOfMeasure = CSVEPSGFactory.getInstance().findUnitOfMeasureByCode(parameter2UnitOfMeasureCode);
        }
        return parameter2UnitOfMeasure;
    }

    @Override
    public synchronized UnitOfMeasureEntry getParameter3UnitOfMeasure() {
        if (parameter3UnitOfMeasure == null && parameter3UnitOfMeasureCode != MISSING_CODE) {
            parameter3UnitOfMeasure = CSVEPSGFactory.getInstance().findUnitOfMeasureByCode(parameter3UnitOfMeasureCode);
        }
        return parameter3UnitOfMeasure;
    }

    @Override
    public synchronized UnitOfMeasureEntry getParameter4UnitOfMeasure() {
        if (parameter4UnitOfMeasure == null && parameter4UnitOfMeasureCode != MISSING_CODE) {
            parameter4UnitOfMeasure = CSVEPSGFactory.getInstance().findUnitOfMeasureByCode(parameter4UnitOfMeasureCode);
        }
        return parameter4UnitOfMeasure;
    }

    @Override
    public synchronized UnitOfMeasureEntry getParameter5UnitOfMeasure() {
        if (parameter5UnitOfMeasure == null && parameter5UnitOfMeasureCode != MISSING_CODE) {
            parameter5UnitOfMeasure = CSVEPSGFactory.getInstance().findUnitOfMeasureByCode(parameter5UnitOfMeasureCode);
        }
        return parameter5UnitOfMeasure;
    }

    @Override
    public synchronized UnitOfMeasureEntry getParameter6UnitOfMeasure() {
        if (parameter6UnitOfMeasure == null && parameter6UnitOfMeasureCode != MISSING_CODE) {
            parameter6UnitOfMeasure = CSVEPSGFactory.getInstance().findUnitOfMeasureByCode(parameter6UnitOfMeasureCode);
        }
        return parameter6UnitOfMeasure;
    }

    @Override
    public synchronized UnitOfMeasureEntry getParameter7UnitOfMeasure() {
        if (parameter7UnitOfMeasure == null && parameter7UnitOfMeasureCode != MISSING_CODE) {
            parameter7UnitOfMeasure = CSVEPSGFactory.getInstance().findUnitOfMeasureByCode(parameter7UnitOfMeasureCode);
        }
        return parameter7UnitOfMeasure;
    }

    @Override
    public double getParameterValueByCode(int parameterCode) {
        // so lame
        if (parameter1Code == parameterCode && parameter1Code != MISSING_CODE) {
            return getParameter1UnitOfMeasure().convertToTargetUnitOfMeasure(parameter1Value);
        }
        if (parameter2Code == parameterCode && parameter2Code != MISSING_CODE) {
            return getParameter2UnitOfMeasure().convertToTargetUnitOfMeasure(parameter2Value);
        }
        if (parameter3Code == parameterCode && parameter3Code != MISSING_CODE) {
            return getParameter3UnitOfMeasure().convertToTargetUnitOfMeasure(parameter3Value);
        }
        if (parameter4Code == parameterCode && parameter4Code != MISSING_CODE) {
            return getParameter4UnitOfMeasure().convertToTargetUnitOfMeasure(parameter4Value);
        }
        if (parameter5Code == parameterCode && parameter5Code != MISSING_CODE) {
            return getParameter5UnitOfMeasure().convertToTargetUnitOfMeasure(parameter5Value);
        }
        if (parameter6Code == parameterCode && parameter6Code != MISSING_CODE) {
            return getParameter6UnitOfMeasure().convertToTargetUnitOfMeasure(parameter6Value);
        }
        if (parameter7Code == parameterCode && parameter7Code != MISSING_CODE) {
            return getParameter7UnitOfMeasure().convertToTargetUnitOfMeasure(parameter7Value);
        }
        return MISSING_VALUE;
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
