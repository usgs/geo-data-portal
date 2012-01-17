package ucar.nc2.iosp.geotiff.epsg.csv;

import ucar.nc2.iosp.geotiff.epsg.GTDatum;
import ucar.nc2.iosp.geotiff.epsg.GTEllipsoid;
import ucar.nc2.iosp.geotiff.epsg.GTPrimeMeridian;

public class DatumEntry implements CSVEntry, GTDatum {

    private int code;
    private String name;
    private String typeAsString;
    private int ellipsoidCode;
    private int primeMeridianCode;

    private GTEllipsoid ellipsoid;
    private GTPrimeMeridian primeMeridian;

    @Override
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setEllipsoidCode(int ellipsoidCode) {
        this.ellipsoidCode = ellipsoidCode;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPrimeMeridianCode(int primeMeridianCode) {
        this.primeMeridianCode = primeMeridianCode;
    }

    @Override
    public Type getType() {
        return Type.valueOf(typeAsString);
    }

    public void setTypeAsString(String typeAsString) {
        this.typeAsString = typeAsString;
    }

    @Override
    public synchronized GTEllipsoid getEllipsoid() {
        if (ellipsoid == null) {
            if (getType() == Type.geodetic) {
                ellipsoid = CSVEPSGFactory.getInstance().findEllipsoidByCode(ellipsoidCode);
            } else {
                throw new IllegalStateException("attempt to access ellipsoid for non-geodetic datum");
            }
        }
        return ellipsoid;
    }

    @Override
    public synchronized GTPrimeMeridian getPrimeMeridian() {
        if (primeMeridian == null) {
            if (getType() == Type.geodetic) {
                primeMeridian = CSVEPSGFactory.getInstance().findPrimeMeridianByCode(primeMeridianCode);
            } else {
                throw new IllegalStateException("attempt to access prime meridian for non-geodetic datum");
            }
        }
        return primeMeridian;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DatumEntry other = (DatumEntry) obj;
        if (this.code != other.code) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 83 * hash + this.code;
        return hash;
    }
}
