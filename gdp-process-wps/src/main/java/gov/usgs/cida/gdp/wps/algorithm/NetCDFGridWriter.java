package gov.usgs.cida.gdp.wps.algorithm;

import gov.usgs.cida.gdp.coreprocessing.analysis.grid.GridUtility;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.geotools.feature.FeatureCollection;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;
import ucar.nc2.Attribute;
import ucar.nc2.FileWriter;
import ucar.nc2.Variable;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.CoordinateTransform;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDataset;
import ucar.nc2.dt.GridDatatype;

public class NetCDFGridWriter {

    static private org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(NetCDFGridWriter.class);

    public static void makeFile(
            String location,
            GridDataset gridDataset,
            List<String> gridVariableList,
            FeatureCollection featureCollection,
            Date dateTimeStart,
            Date dateTimeEnd,
            boolean requireFullCoverage,
            String metaDataString)
            throws IOException, InvalidRangeException, TransformException, FactoryException {

        FileWriter writer = new FileWriter(location, false);
        NetcdfDataset netcdfDataset = (NetcdfDataset) gridDataset.getNetcdfFile();

        // global attributes
        Attribute historyAttribute = null;
        for (Attribute att : gridDataset.getGlobalAttributes()) {
            String attributeName = att.getName();
            if ("history".equalsIgnoreCase(attributeName)) {
                // defer write as we want to concatenate to existing attribute.
                historyAttribute = att;  
            } else {
                writer.writeGlobalAttribute(att);
            }
        }

        historyAttribute = historyAttribute == null ?  
            new Attribute("history", metaDataString) :  
            new Attribute(historyAttribute.getName(),
                    historyAttribute.getStringValue() + "/n" + metaDataString);
        writer.writeGlobalAttribute(historyAttribute);

        List<Variable> variableList = new ArrayList<Variable>();
        Set<String> variableNameSet = new HashSet<String>();
        List<CoordinateAxis> coordinateAxisList = new ArrayList<CoordinateAxis>();

        // add each desired Grid to the new file
        long total_size = 0;
        for (String gridVariable : gridVariableList) {
            if (variableNameSet.add(gridVariable)) {

                GridDatatype gridDataType = gridDataset.findGridDatatype(gridVariable);
                GridCoordSystem gridCoordSystem = gridDataType.getCoordinateSystem();

                // generate sub-set
                Range tRange = GDPAlgorithmUtil.generateTimeRange(gridDataType, dateTimeStart, dateTimeEnd);
                Range[] xyRanges = GridUtility.getRangesFromBoundingBox(featureCollection.getBounds(), gridCoordSystem, requireFullCoverage); 
                gridDataType = gridDataType.makeSubset(null, null, tRange, null, xyRanges[1], xyRanges[0]);

                Variable gridV = (Variable) gridDataType.getVariable();
                variableList.add(gridV);
                total_size += gridV.getSize() * gridV.getElementSize();

                // add coordinate axes
                gridCoordSystem = gridDataType.getCoordinateSystem();
                for (CoordinateAxis axis : gridCoordSystem.getCoordinateAxes()) {
                    if (variableNameSet.add(axis.getName())) {
                        variableList.add(axis);
                        coordinateAxisList.add(axis);
                    }
                }

                // add coordinate transform variables
                for (CoordinateTransform ct : gridCoordSystem.getCoordinateTransforms()) {
                    Variable v = netcdfDataset.findVariable(ct.getName());
                    if (v != null && variableNameSet.add(ct.getName())) {
                        variableList.add(v);
                    }
                }
            }
        }

        writer.writeVariables(variableList);

        writer.finish();
    }
    
}
