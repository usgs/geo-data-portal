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
public class ProjCSHandler implements CSHandler {

    private String gridMappingName;
    private Map<String, Double> parameterMap;
    private GeogCSHandler geogCSHandler;
    private Variable variable;

    public ProjCSHandler(
            GeogCSHandler geogCSHandler,
            String gridMappingName,
            Map<String, Double> paramterMap) {
        this.geogCSHandler = geogCSHandler;
        this.gridMappingName = gridMappingName;
        this.parameterMap = paramterMap;
    }


    public int getCode() {
        return 32767;
    }

    public String getName() {
        if (variable == null) {
            throw new IllegalStateException("Coordinate reference system not generated for this instance");
        }
        return variable.getName();
    }

    public synchronized Variable generateCRSVariable(NetcdfFile netCDFFile, int index) {
        if (variable == null) {

            variable = new Variable(netCDFFile, null, null, "crs" + index);
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

            geogCSHandler.augmentCRSVariable(variable);
        }
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
        final ProjCSHandler other = (ProjCSHandler) obj;
        if ((this.gridMappingName == null) ? (other.gridMappingName != null) : !this.gridMappingName.equals(other.gridMappingName)) {
            return false;
        }
        if (this.parameterMap != other.parameterMap && (this.parameterMap == null || !this.parameterMap.equals(other.parameterMap))) {
            return false;
        }
        if (this.geogCSHandler != other.geogCSHandler && (this.geogCSHandler == null || !this.geogCSHandler.equals(other.geogCSHandler))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 23 * hash + (this.gridMappingName != null ? this.gridMappingName.hashCode() : 0);
        hash = 23 * hash + (this.parameterMap != null ? this.parameterMap.hashCode() : 0);
        hash = 23 * hash + (this.geogCSHandler != null ? this.geogCSHandler.hashCode() : 0);
        return hash;
    }
}
