package ucar.nc2.iosp.geotiff.cs;

import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

public interface CSHandler {

    public int getCode();

    public String getName();

    public Variable generateCRSVariable(NetcdfFile netCDFFile, int index);
}
