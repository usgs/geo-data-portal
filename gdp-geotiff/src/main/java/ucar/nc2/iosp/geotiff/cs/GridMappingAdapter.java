package ucar.nc2.iosp.geotiff.cs;

import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

public interface GridMappingAdapter {

    public int getCode();

    public Variable generateGridMappingVariable(NetcdfFile netCDFFile, int index);
}
