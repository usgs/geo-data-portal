package gov.usgs.cida.gdp.wps.algorithm;

import gov.usgs.cida.gdp.coreprocessing.analysis.grid.GridUtility;
import java.net.URISyntaxException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.dt.GridCoordSystem;
import ucar.ma2.Range;
import ucar.ma2.InvalidRangeException;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.geotools.feature.FeatureCollection;
import ucar.nc2.Attribute;
import ucar.nc2.FileWriter;
import ucar.nc2.Variable;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.CoordinateAxis1DTime;
import ucar.nc2.dataset.CoordinateTransform;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dt.GridDataset;

public class NetCDFGridSubSetWriter {

    static private org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(NetCDFGridSubSetWriter.class);

    public static void makeFile(
            String location,
            GridDataset gridDataset,
            List<String> gridVariableList,
            FeatureCollection featureCollection,
            Date dateTimeStart,
            Date dateTimeEnd,
            String metaDataString)
            throws IOException, InvalidRangeException, TransformException, FactoryException {

        FileWriter writer = new FileWriter(location, false);
        NetcdfDataset ncd = (NetcdfDataset) gridDataset.getNetcdfFile();

        // global attributes
        Attribute historyAttribute = null;
        for (Attribute att : gridDataset.getGlobalAttributes()) {
            String attributeName = att.getName();
            if ("history".equalsIgnoreCase(attributeName)) {
                historyAttribute = att;  // we want to concatenate to existing attribute.
            } else {
                writer.writeGlobalAttribute(att);
            }
        }

        if (historyAttribute == null) {
            writer.writeGlobalAttribute(new Attribute(
                    "history",
                    metaDataString));  
        } else {
            writer.writeGlobalAttribute(new Attribute(
                    historyAttribute.getName(),
                    historyAttribute.getStringValue() + "; " + metaDataString));
        }

        List<Variable> varList = new ArrayList<Variable>();
        List<String> varNameList = new ArrayList<String>();
        List<CoordinateAxis> axisList = new ArrayList<CoordinateAxis>();

        // add each desired Grid to the new file
        long total_size = 0;
        for (String gridName : gridVariableList) {
            if (varNameList.contains(gridName)) {
                continue;
            }

            varNameList.add(gridName);

            GridDatatype gridCurrent = gridDataset.findGridDatatype(gridName);
            GridCoordSystem gcsCurrent = gridCurrent.getCoordinateSystem();
            CoordinateAxis1DTime timeAxis = gcsCurrent.getTimeAxis1D();

            // make subset if needed
            Range tRange = null;
            if ((dateTimeStart != null || dateTimeEnd != null ) && (timeAxis != null)) {
                int timeStartIndex = dateTimeStart != null ?
                    timeAxis.findTimeIndexFromDate(dateTimeStart) :
                    0;
                int timeEndIndex = dateTimeEnd != null ?
                    timeAxis.findTimeIndexFromDate(dateTimeEnd) :
                    timeAxis.getShape(0) - 1;
                try {
                    tRange = new Range(timeStartIndex, timeEndIndex);
                } catch (InvalidRangeException e) {
                    throw new RuntimeException("Unable to generate time range.", e);
                }
            }
            
            Range[] ranges = GridUtility.getRangesFromBoundingBox(featureCollection.getBounds(), gcsCurrent); 

            gridCurrent = gridCurrent.makeSubset(null, null, tRange, null, ranges[1], ranges[0]);

            Variable gridV = (Variable) gridCurrent.getVariable();
            varList.add(gridV);
            total_size += gridV.getSize() * gridV.getElementSize();

            // add coordinate axes
            GridCoordSystem gcs = gridCurrent.getCoordinateSystem();
            for (CoordinateAxis axis : gcs.getCoordinateAxes()) {
                if (!varNameList.contains(axis.getName())) {
                    varNameList.add(axis.getName());
                    varList.add(axis);
                    axisList.add(axis);
                }
            }

            // add coordinate transform variables
            for (CoordinateTransform ct : gcs.getCoordinateTransforms()) {
                Variable v = ncd.findVariable(ct.getName());
                if (!varNameList.contains(ct.getName()) && (v != null)) {
                    varNameList.add(ct.getName());
                    varList.add(v);
                }
            }
        }

        writer.writeVariables(varList);

        writer.finish();
        
        writer.getNetcdf().writeCDL(System.out, true);
    }
    
}
