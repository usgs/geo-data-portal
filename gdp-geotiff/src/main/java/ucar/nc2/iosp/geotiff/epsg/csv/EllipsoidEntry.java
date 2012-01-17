package ucar.nc2.iosp.geotiff.epsg.csv;

import ucar.nc2.iosp.geotiff.epsg.GTEllipsoid;
import ucar.nc2.iosp.geotiff.epsg.GTUnitOfMeasure;

public class EllipsoidEntry implements CSVEntry, GTEllipsoid {

    private int code;
    private String name;
    private double semiMajorAxis;
    private double semiMinorAxis;
    private double inverseFlattening;
    private int unitOfMeasureCode;

    private GTUnitOfMeasure unitOfMeasure;

    @Override
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    @Override
    public double getInverseFlattening() {
        return inverseFlattening;
    }

    public void setInverseFlattening(double inverseFlattening) {
        this.inverseFlattening = inverseFlattening;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public double getSemiMajorAxis() {
        return semiMajorAxis;
    }

    public void setSemiMajorAxis(double semiMajorAxis) {
        this.semiMajorAxis = semiMajorAxis;
    }

    @Override
    public double getSemiMinorAxis() {
        return semiMinorAxis;
    }

    public void setSemiMinorAxis(double semiMinorAxis) {
        this.semiMinorAxis = semiMinorAxis;
    }

    public void setUnitOfMeasureCode(int unitOfMeasureCode) {
        this.unitOfMeasureCode = unitOfMeasureCode;
    }

    @Override
    public synchronized GTUnitOfMeasure getUnitOfMeasure() {
        if (unitOfMeasure == null) {
            unitOfMeasure = CSVEPSGFactory.getInstance().findUnitOfMeasureByCode(unitOfMeasureCode);
        }
        return unitOfMeasure;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final EllipsoidEntry other = (EllipsoidEntry) obj;
        if (this.code != other.code) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + this.code;
        return hash;
    }
}
