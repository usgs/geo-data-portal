package ucar.nc2.iosp.geotiff.cs;

import java.util.LinkedHashMap;
import java.util.Map;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

/**
 *
 * @author tkunicki
 */
public class ProjCSGridMappingAdapter implements GridMappingAdapter {

    private String gridMappingName;
    private Map<String, Double> parameterMap;
    private GeogCSGridMappingAdapter geogCSGridMappingAdapter;

    public ProjCSGridMappingAdapter(
            GeogCSGridMappingAdapter geogCSGridMapping,
            String gridMappingName,
            Map<String, Double> paramterMap) {
        this.geogCSGridMappingAdapter = geogCSGridMapping;
        this.gridMappingName = gridMappingName;
        this.parameterMap = paramterMap;
    }


    @Override
    public int getCode() {
        // TCK - 2012.01.02 - I *think* there are ProjCS that can be referenced from
        // their codes, then using that code you can recontruct the projcs method, parameters
        // from the EPSG database.  I *think* this ties into the never-implemented method
        // GeoTiffCoordSys.generateProjCSHandlerFromProjection(...) referenced in
        // GeoTiffCoordSys.generateProjCSHandler(...) ? The use of 32767 is from the GeoTIFF spec.
        return 32767;
    }

    @Override
    public synchronized Variable generateGridMappingVariable(NetcdfFile netCDFFile, int index) {
        
        Variable variable = new Variable(netCDFFile, null, null, "crs" + index);
        variable.setDataType(DataType.INT);
        variable.setIsScalar();

        Map<String, Double> clonedMap = new LinkedHashMap<String, Double>(parameterMap);
        String standardParallel = null;
        Double standardParallel1 = clonedMap.remove("standard_parallel1");
        Double standardParallel2 = clonedMap.remove("standard_parallel2");
        if (standardParallel1 != null && !standardParallel1.isNaN()) {
            if (standardParallel2 != null && !standardParallel2.isNaN()) {
               standardParallel = standardParallel1 + " " + standardParallel2;
            } else {
                standardParallel = standardParallel1.toString();
            }
        }

        variable.addAttribute(new Attribute("grid_mapping_name", gridMappingName));
        for (Map.Entry<String, Double> parameterEntry : clonedMap.entrySet()) {
            if (!parameterEntry.getValue().isNaN()) {
                variable.addAttribute(new Attribute(
                        parameterEntry.getKey(),
                        parameterEntry.getValue()));
            }
        }
        if (standardParallel != null) {
            variable.addAttribute(new Attribute("standard_parallel", standardParallel));
        }
        
        geogCSGridMappingAdapter.augmentCRSVariable(variable);

        return variable;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ProjCSGridMappingAdapter other = (ProjCSGridMappingAdapter) obj;
        if ((this.gridMappingName == null) ? (other.gridMappingName != null) : !this.gridMappingName.equals(other.gridMappingName)) {
            return false;
        }
        if (this.parameterMap != other.parameterMap && (this.parameterMap == null || !this.parameterMap.equals(other.parameterMap))) {
            return false;
        }
        if (this.geogCSGridMappingAdapter != other.geogCSGridMappingAdapter && (this.geogCSGridMappingAdapter == null || !this.geogCSGridMappingAdapter.equals(other.geogCSGridMappingAdapter))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 23 * hash + (this.gridMappingName != null ? this.gridMappingName.hashCode() : 0);
        hash = 23 * hash + (this.parameterMap != null ? this.parameterMap.hashCode() : 0);
        hash = 23 * hash + (this.geogCSGridMappingAdapter != null ? this.geogCSGridMappingAdapter.hashCode() : 0);
        return hash;
    }
}
