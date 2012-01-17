package ucar.nc2.iosp.geotiff.epsg.csv;

import ucar.nc2.iosp.geotiff.epsg.GTDatum;
import ucar.nc2.iosp.geotiff.epsg.GTEllipsoid;
import ucar.nc2.iosp.geotiff.epsg.GTGeogCS;
import ucar.nc2.iosp.geotiff.epsg.GTPrimeMeridian;
import ucar.nc2.iosp.geotiff.epsg.GTUnitOfMeasure;

/**
 *
 * @author tkunicki
 */
public class GeogCSEntry implements CSVEntry, GTGeogCS {

    private int code;
    private String name;
    private int datumCode;
//    private int greenwichDatumCode;
    private int unitOfMeasureCode;
    private int ellipsoidCode;
    private int primeMeridianCode;

    private GTDatum datum;
    private GTEllipsoid ellipsoid;
//    private GTDatum greenwichDatum;
    private GTPrimeMeridian primeMeridian;
    private GTUnitOfMeasure unitOfMeasure;

    @Override
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setDatumCode(int datumCode) {
        this.datumCode = datumCode;
    }

    public void setEllipsoidCode(int ellipsoidCode) {
        this.ellipsoidCode = ellipsoidCode;
    }


//    public void setGreenwichDatumCode(int greenwichDatumCode) {
//        this.greenwichDatumCode = greenwichDatumCode;
//    }

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

    public void setUnitOfMeasureCode(int unitOfMeasureCode) {
        this.unitOfMeasureCode = unitOfMeasureCode;
    }

    @Override
    public synchronized GTDatum getDatum() {
        if (datum == null) {
            datum = CSVEPSGFactory.getInstance().findDatumByCode(datumCode);
        }
        return datum;
    }

    @Override
    public synchronized GTEllipsoid getEllipsoid() {
        if (ellipsoid == null) {
            ellipsoid = CSVEPSGFactory.getInstance().findEllipsoidByCode(ellipsoidCode);
        }
        return ellipsoid;
    }

    @Override
    public synchronized GTPrimeMeridian getPrimeMeridian() {
        if (primeMeridian == null) {
            primeMeridian = CSVEPSGFactory.getInstance().findPrimeMeridianByCode(primeMeridianCode);
        }
        return primeMeridian;
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
        final GeogCSEntry other = (GeogCSEntry) obj;
        if (this.code != other.code) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 73 * hash + this.code;
        return hash;
    }
}
