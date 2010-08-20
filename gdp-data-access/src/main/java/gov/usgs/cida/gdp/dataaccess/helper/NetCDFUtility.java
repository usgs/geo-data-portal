package gov.usgs.cida.gdp.dataaccess.helper;

import com.google.common.base.Preconditions;
import gov.usgs.cida.gdp.utilities.bean.TimeBean;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Formatter;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import thredds.catalog.InvAccess;
import thredds.catalog.InvCatalog;
import thredds.catalog.InvDataset;
import thredds.catalog.ServiceType;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.VariableSimpleIF;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.dt.grid.GeoGrid;
import ucar.nc2.dt.grid.GridDataset;
import ucar.nc2.ft.FeatureCollection;
import ucar.nc2.ft.FeatureDataset;
import ucar.nc2.ft.FeatureDatasetFactoryManager;
import ucar.nc2.ft.FeatureDatasetPoint;
import ucar.nc2.ft.StationTimeSeriesFeature;
import ucar.nc2.ft.StationTimeSeriesFeatureCollection;
import ucar.nc2.units.DateRange;
import ucar.nc2.util.NamedObject;

public abstract class NetCDFUtility {
    // Private nullary ctor ensures non-instantiability.
    private NetCDFUtility() { }
    
    /**
     * For every dataset discovered in a depth-first traversal of {@code catalog}, this method returns a handle to it
     * of type {@code serviceType}, if available.
     *
     * @param catalog       an object representing a THREDDS catalog.
     * @param serviceType   the type of service that the returned handles will use to access data.
     * @return  a list of dataset handles. The list will be empty if {@code catalog} or {@code serviceType} is null.
     */
    public static List<InvAccess> getDatasetHandles(InvCatalog catalog, ServiceType serviceType) {
        if (catalog == null || serviceType == null) {
            return Collections.emptyList();     // Template parameter inferred from return type.
        }

        List<InvAccess> handles = new LinkedList<InvAccess>();
        for (InvDataset dataset : catalog.getDatasets()) {
            handles.addAll(getDatasetHandles(dataset, serviceType));
        }

        return handles;
    }

    /**
     * For every dataset discovered in a depth-first traversal of {@code dataset} and its nested datasets, this method
     * returns a handle to it of type {@code serviceType}, if available.
     *
     * @param dataset       a THREDDS dataset, which may have nested datasets.
     * @param serviceType   the type of service that the returned handles will use to access data.
     * @return  a list of dataset handles. The list will be empty if {@code dataset} or {@code serviceType} is null.
     */
    public static List<InvAccess> getDatasetHandles(InvDataset dataset, ServiceType serviceType) {
        if (dataset == null || serviceType == null) {
            return Collections.emptyList();     // Template parameter inferred from return type.
        }

        List<InvAccess> handles = new LinkedList<InvAccess>();
        for (InvAccess handle : dataset.getAccess()) {
            if (handle.getService().getServiceType() == serviceType) {
                handles.add(handle);
            }
        }

        for (InvDataset nestedDataset : dataset.getDatasets()) {
            handles.addAll(getDatasetHandles(nestedDataset, serviceType));
        }

        return handles;
    }

    public static List<VariableSimpleIF> getDataVariableNames(String location) throws IOException {
        if (location == null) {
            throw new IllegalArgumentException("location can't be null");
        }

        List<VariableSimpleIF> variableList = null;
        FeatureDataset dataset = null;
        try {
            dataset = FeatureDatasetFactoryManager.open(
                    null, location, null, new Formatter());
            switch (dataset.getFeatureType()) {
                case POINT:
                case PROFILE:
                case SECTION:
                case STATION:
                case STATION_PROFILE:
                case STATION_RADIAL:
                case TRAJECTORY:

                    variableList = new ArrayList<VariableSimpleIF>();

                    // Try Unidata Observation Dataset convention where observation
                    // dimension is declared as global attribute...
                    Attribute convAtt = dataset.findGlobalAttributeIgnoreCase("Conventions");
                    if (convAtt != null && convAtt.isString()) {
                        String convName = convAtt.getStringValue();

                        //// Unidata Observation Dataset Convention
                        //   http://www.unidata.ucar.edu/software/netcdf-java/formats/UnidataObsConvention.html
                        if (convName.contains("Unidata Observation Dataset")) {
                            Attribute obsDimAtt = dataset.findGlobalAttributeIgnoreCase("observationDimension");
                            String obsDimName = (obsDimAtt != null && obsDimAtt.isString()) ?
                                    obsDimAtt.getStringValue() : null;
                            if (obsDimName != null && obsDimName.length() > 0) {
                                String psuedoRecordPrefix = obsDimName + '.';
                                for (VariableSimpleIF var : dataset.getDataVariables()) {
                                    if (var.findAttributeIgnoreCase("_CoordinateAxisType") == null) {
                                        if (var.getName().startsWith(psuedoRecordPrefix)) {
                                            // doesn't appear to be documented, this
                                            // is observed behavior...
                                            variableList.add(var);
                                        } else {
                                            for (Dimension dim : var.getDimensions()) {
                                                if (obsDimName.equalsIgnoreCase(dim.getName())) {
                                                    variableList.add(var);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            if (variableList.size() == 0) {
                                // no explicit observation dimension found? look for
                                // variables with unlimited dimension
                                for (VariableSimpleIF var : dataset.getDataVariables()) {
                                    for (Dimension dim : var.getDimensions()) {
                                        if (dim.isUnlimited()) {
                                            variableList.add(var);
                                        }
                                    }
                                }
                            }
                        }
                    }

                    //// CF Conventions
                    //   https://cf-pcmdi.llnl.gov/trac/wiki/PointObservationConventions
                    // 
                    //  Don't try explicit :Conventions attribute check since this
                    //  doesnt seem to be coming through TDS with cdmremote when
                    //  CF conventions are used (?!)
                    if (variableList.size() == 0) {
                        // Try CF convention where range variable has coordinate attribute
                        for (VariableSimpleIF variable : dataset.getDataVariables()) {
                            if (variable.findAttributeIgnoreCase("coordinates") != null) {
                                variableList.add(variable);
                            }
                        }
                    }
                    break;
                default:
                    variableList = dataset.getDataVariables();
                    break;
            }
        } finally {
            if (dataset != null) {
                dataset.close();
            }
        }
        if (variableList == null) {
            variableList = Collections.emptyList();
        }
        return variableList;
    }

    public static String getDatasetType(String datasetUrl) throws IOException {
    	FeatureDataset featureDataset = null;
    	try {
    		featureDataset = FeatureDatasetFactoryManager.open(null, datasetUrl, null, new Formatter());
    		return featureDataset.getFeatureType().toString();
    	} finally {
			featureDataset.close();
		}
	}

	public static boolean hasTimeCoordinate(String location) throws IOException {
        FeatureDataset featureDataset = null;
        boolean result = false;
        try {
            featureDataset = FeatureDatasetFactoryManager.open(null, location, null, new Formatter());
            result = hasTimeCoordinate(featureDataset);
        } finally {
            featureDataset.close();
        }
        return result;
    }

    public static boolean hasTimeCoordinate(FeatureDataset featureDataset) throws IOException {
//        boolean hasTime = false;
//        if (featureDataset.getFeatureType() == FeatureType.STATION) {
//            Iterator<VariableSimpleIF> variableIterator = featureDataset.getDataVariables().iterator();
//            while (!hasTime && variableIterator.hasNext()) {
//                VariableSimpleIF vairable = variableIterator.next();
//                Iterator<Attribute> attIterator = vairable.getAttributes().iterator();
//                while (!hasTime && attIterator.hasNext()) {
//                    Attribute att = attIterator.next();
//                    hasTime = "_CoordinateAxisType".equalsIgnoreCase(att.getName()) && "Time".equals(att.getStringValue());
//                }
//            }
//        }
//        return hasTime;
    	return featureDataset.getFeatureType() == FeatureType.STATION;
    }


    /**
     * Retrieves a List of type String which has a date range from the beginning to the end of a FeatureDataSet
     * 
     * @param threddsURL URL for a THREDDS dataset
     * @param variableName name of a Grid or Station variable contained in that dataset
     * @return
     * @throws IOException
     * @throws IllegalArgumentException
     */
    public static List<String> getDateRange(String threddsURL, String variableName) throws IOException, IllegalArgumentException {
        Preconditions.checkNotNull(threddsURL, "location cannot be null");
        Preconditions.checkNotNull(variableName, "variable cannot be null");

        FeatureDataset dataset = FeatureDatasetFactoryManager.open(null, threddsURL, null, new Formatter());
        List<String> dateRange = new ArrayList<String>(2);
        try {
            if (dataset.getFeatureType() == FeatureType.GRID) {
                GeoGrid grid = ((GridDataset) dataset).findGridByName(variableName);
                if (grid == null) {
                    return dateRange;
                }
                List<NamedObject> times = grid.getTimes();
                if (times.isEmpty()) {
                    return dateRange;
                }

                NamedObject startTimeNamedObject = times.get(0);
                String startTime = startTimeNamedObject.getName();
                dateRange.add(0, startTime);

                NamedObject endTimeNamedObject = times.get(times.size() - 1);
                String endTime = endTimeNamedObject.getName();
                dateRange.add(1, endTime);
            } else if (dataset.getFeatureType() == FeatureType.STATION) {
                DateRange dr = dataset.getDateRange();
                if (dr == null) {
                    List<FeatureCollection> list =
                            ((FeatureDatasetPoint) dataset).getPointFeatureCollectionList();
                    for (FeatureCollection fc : list) {
                        if (fc instanceof StationTimeSeriesFeatureCollection) {
                            StationTimeSeriesFeatureCollection stsfc =
                                    (StationTimeSeriesFeatureCollection) fc;
                            while (dr == null && stsfc.hasNext()) {
                                StationTimeSeriesFeature stsf = stsfc.next();
                                dr = stsf.getDateRange();
                            }
                        }
                    }
                }
                if (dr != null) {
                    dateRange.set(0, dr.getStart().toString());
                    dateRange.set(1, dr.getEnd().toString());
                }
            }
        } finally {
            dataset.close();
        }
        return dateRange;
    }

    public static TimeBean getTimeBean(String location, String gridSelection) throws IOException, ParseException, IllegalArgumentException {
        List<String> dateRange = NetCDFUtility.getDateRange(location, gridSelection);
        if (dateRange.isEmpty()) {
            boolean hasTimeCoord = NetCDFUtility.hasTimeCoordinate(location);
            if (hasTimeCoord) { // This occurs when there is no date range in the file but has time coords
                // We want the user to pick dates but don't have a range to give
                dateRange.add("1800-01-01 00:00:00Z");
                dateRange.add("2100-12-31 00:00:00Z");
            }
        }
        TimeBean result = new TimeBean(dateRange);
        return result;
    }

    public static void main(String[] args) {
        try {
            //        URI catalogURI = URI.create("http://runoff.cr.usgs.gov:8086/thredds/hydrologic_catalog.xml");
            //        URI catalogURI = URI.create("http://runoff:8086/thredds/catalog.xml");
            //        URI catalogURI = URI.create("http://geoport.whoi.edu:8081/thredds/multi_catalog_all.xml");
            //        URI catalogURI = new File("C:/Documents and Settings/cwardgar/Desktop/multi_catalog_all.xml").toURI();
            //        InvCatalogFactory factory = new InvCatalogFactory("default", true);
            //        InvCatalog catalog = factory.readXML(catalogURI);
            //
            //        StringBuilder buff = new StringBuilder();
            //        if (!catalog.check(buff)) {
            //            System.err.println(buff.toString());
            //        }
            //
            //        List<InvAccess> handles = getDatasetHandles(catalog, ServiceType.OPENDAP);
            //        for (InvAccess handle : handles) {
            //            System.out.println(handle.getDataset().getCatalogUrl());
            //        }
            //
            for (VariableSimpleIF v : getDataVariableNames("/Users/tkunicki/Downloads/GSOD/netcdf/gsod.c.uod.nc")) {
                System.out.println(v.getShortName());
            }
            System.out.println("***");
            for (VariableSimpleIF v : getDataVariableNames("dods://localhost:18080/thredds/dodsC/gsod/gsod.c.uod.nc")) {
                System.out.println(v.getShortName());
            }
            System.out.println("***");
            for (VariableSimpleIF v : getDataVariableNames("cdmremote:http://localhost:18080/thredds/cdmremote/gsod/gsod.c.uod.nc")) {
                System.out.println(v.getShortName());
            }
            System.out.println("***");
            for (VariableSimpleIF v : getDataVariableNames("/Users/tkunicki/Downloads/GSOD/netcdf/gsod.c.cf.nc")) {
                System.out.println(v.getShortName());
            }
            System.out.println("***");
            for (VariableSimpleIF v : getDataVariableNames("dods://localhost:18080/thredds/dodsC/gsod/gsod.c.cf.nc")) {
                System.out.println(v.getShortName());
            }
            System.out.println("***");
            for (VariableSimpleIF v : getDataVariableNames("cdmremote:http://localhost:18080/thredds/cdmremote/gsod/gsod.c.cf.nc")) {
                System.out.println(v.getShortName());
            }
        } catch (IOException ex) {
            Logger.getLogger(NetCDFUtility.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(NetCDFUtility.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
