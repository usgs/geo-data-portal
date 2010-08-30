package ucar.nc2.iosp.geotiff.cs;

import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.iosp.geotiff.epsg.Ellipsoid;
import ucar.nc2.iosp.geotiff.epsg.GeogCS;

public class GeogCSHandler implements CSHandler {

    private GeogCS geogCS;

    private Variable variable;

    public GeogCSHandler(GeogCS geogCS) {
        this.geogCS = geogCS;
    }

    @Override
    public int getCode() {
        return geogCS.getCode();
    }

    @Override
    public synchronized String getName() {
        if (variable == null) {
            throw new IllegalStateException("Coordinate reference system not generated for this instance");
        }
        return variable.getName();
    }

    @Override
    public synchronized Variable generateCRSVariable(NetcdfFile netCDFFile, int index) {
        if (variable == null) {
          
            variable = new Variable(netCDFFile, null, null, "crs" + index);
            variable.setDataType(DataType.INT);
            variable.setIsScalar();
            variable.addAttribute(new Attribute("grid_mapping_name", "latitude_longitude"));

            augmentCRSVariable(variable);

            return variable;
        }
        throw new IllegalStateException("Coordinate reference system already generated for this instance");
    }

    public void augmentCRSVariable(Variable variable) {
        
        variable.addAttribute(new Attribute("longitude_of_prime_meridian", geogCS.getPrimeMeridian().getLongitude()));

        Ellipsoid ellipsoid = geogCS.getEllipsoid();
        double semiMajor = ellipsoid.getSemiMajorAxis();
        double semiMinor = ellipsoid.getSemiMinorAxis();
        double inverse = ellipsoid.getInverseFlattening();
        if (!Double.isNaN(semiMajor)) {
            variable.addAttribute(new Attribute("semi_major_axis", semiMajor));
        }
        if (!Double.isNaN(semiMinor)) {
            variable.addAttribute(new Attribute("semi_minor_axis", semiMinor));
        }
        if (!Double.isNaN(inverse)) {
            variable.addAttribute(new Attribute("inverse_flattening", inverse));
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final GeogCSHandler other = (GeogCSHandler) obj;
        if (this.geogCS != other.geogCS && (this.geogCS == null || !this.geogCS.equals(other.geogCS))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (this.geogCS != null ? this.geogCS.hashCode() : 0);
        return hash;
    }
}
