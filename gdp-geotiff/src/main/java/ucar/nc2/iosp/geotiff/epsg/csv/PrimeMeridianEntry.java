package ucar.nc2.iosp.geotiff.epsg.csv;

import ucar.nc2.iosp.geotiff.epsg.PrimeMeridian;
import ucar.nc2.iosp.geotiff.epsg.UnitOfMeasure;

/**
 *
 * @author tkunicki
 */
public class PrimeMeridianEntry implements CSVEntry, PrimeMeridian {

    private int code;
    private String name;
    private double longitude;
    private int unitOfMeasureCode;

    private UnitOfMeasure unitOfMeasure;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUnitOfMeasureCode(int unitOfMeasureCode) {
        this.unitOfMeasureCode = unitOfMeasureCode;
    }

    public synchronized UnitOfMeasure getUnitOfMeasure() {
        if (unitOfMeasure == null) {
            unitOfMeasure = EPSG.findUnitOfMeasureByCode(unitOfMeasureCode);
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
