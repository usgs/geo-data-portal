package ucar.nc2.iosp.geotiff.epsg.csv;

import ucar.nc2.iosp.geotiff.epsg.Datum;
import ucar.nc2.iosp.geotiff.epsg.Ellipsoid;
import ucar.nc2.iosp.geotiff.epsg.GeogCS;
import ucar.nc2.iosp.geotiff.epsg.PrimeMeridian;
import ucar.nc2.iosp.geotiff.epsg.UnitOfMeasure;

/**
 *
 * @author tkunicki
 */
public class GeogCSEntry implements CSVEntry, GeogCS {

    private int code;
    private String name;
    private int datumCode;
    private int greenwichDatumCode;
    private int unitOfMeasureCode;
    private int ellipsoidCode;
    private int primeMeridianCode;

    private Datum datum;
    private Ellipsoid ellipsoid;
    private Datum greenwichDatum;
    private PrimeMeridian primeMeridian;
    private UnitOfMeasure unitOfMeasure;

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


    public void setGreenwichDatumCode(int greenwichDatumCode) {
        this.greenwichDatumCode = greenwichDatumCode;
    }

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

    public synchronized Datum getDatum() {
        if (datum == null) {
            datum = EPSG.findDatumByCode(datumCode);
        }
        return datum;
    }

    public synchronized Ellipsoid getEllipsoid() {
        if (ellipsoid == null) {
            ellipsoid = EPSG.findEllipsoidByCode(ellipsoidCode);
        }
        return ellipsoid;
    }

    public synchronized Datum getGreenwichDatum() {
        if (greenwichDatum == null) {
            greenwichDatum = EPSG.findDatumByCode(greenwichDatumCode);
        }
        return greenwichDatum;
    }

    public synchronized PrimeMeridian getPrimeMeridian() {
        if (primeMeridian == null) {
            primeMeridian = EPSG.findPrimeMeridianByCode(primeMeridianCode);
        }
        return primeMeridian;
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
