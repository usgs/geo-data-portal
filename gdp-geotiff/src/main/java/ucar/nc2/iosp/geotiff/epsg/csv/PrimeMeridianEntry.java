package ucar.nc2.iosp.geotiff.epsg.csv;

import ucar.nc2.iosp.geotiff.epsg.GTPrimeMeridian;
import ucar.nc2.iosp.geotiff.epsg.GTUnitOfMeasure;

/**
 *
 * @author tkunicki
 */
public class PrimeMeridianEntry implements CSVEntry, GTPrimeMeridian {

    private int code;
    private String name;
    private double longitude;
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
    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
        final PrimeMeridianEntry other = (PrimeMeridianEntry) obj;
        if (this.code != other.code) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + this.code;
        return hash;
    }

}
