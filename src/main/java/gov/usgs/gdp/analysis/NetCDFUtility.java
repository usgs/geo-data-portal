package gov.usgs.gdp.analysis;

import com.google.common.base.Preconditions;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Formatter;
import java.util.LinkedList;
import java.util.List;
import thredds.catalog.InvAccess;
import thredds.catalog.InvCatalog;
import thredds.catalog.InvCatalogFactory;
import thredds.catalog.InvDataset;
import thredds.catalog.ServiceType;
import ucar.nc2.VariableSimpleIF;
import ucar.nc2.constants.FeatureType;
import ucar.nc2.dt.grid.GeoGrid;
import ucar.nc2.dt.grid.GridDataset;
import ucar.nc2.ft.FeatureCollection;
import ucar.nc2.ft.FeatureDataset;
import ucar.nc2.ft.FeatureDatasetFactoryManager;
import ucar.nc2.ft.FeatureDatasetPoint;
import ucar.nc2.ft.PointFeatureIterator;
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

        List<VariableSimpleIF> variableList = new ArrayList<VariableSimpleIF>();
        FeatureDataset dataset = null;
        try {
            dataset = FeatureDatasetFactoryManager.open(
                    null, location, null, new Formatter());
            for (VariableSimpleIF variable : dataset.getDataVariables()) {
                if (variable.findAttributeIgnoreCase("_CoordinateAxisType") == null) {
                	variableList.add(variable);
                }
            }
        } finally {
            if (dataset != null) {
                dataset.close();
            }
        }
        return variableList;
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
        try {
            List<String> dateRange = new ArrayList<String>(2);

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
                DateRange dr = null;
                List<FeatureCollection> list =
                        ((FeatureDatasetPoint) dataset).getPointFeatureCollectionList();
                for (FeatureCollection fc : list) {
                    if (fc instanceof StationTimeSeriesFeatureCollection) {
                        StationTimeSeriesFeatureCollection stsfc =
                                (StationTimeSeriesFeatureCollection) fc;
                        //stsfc = stsfc.subset(boundingBox);
                        while (dr == null && stsfc.hasNext()) {
                            StationTimeSeriesFeature stsf = stsfc.next();
                            System.out.println(stsf.getName() + "" + stsf.size());
                            PointFeatureIterator pfi = stsf.getPointFeatureIterator(1 << 20);
                            while (pfi.hasNext()) {
                                pfi.next();
                            }
                        }
                        while (stsfc.hasNext()) {
                            dr.extend(stsfc.next().getDateRange());
                        }
                    }
                }
                if (dr == null) {
                    dr = dataset.getDateRange();
                }
                if (dr != null) {
                    dateRange.set(0, dr.getEnd().toString());
                    dateRange.set(0, dr.getEnd().toString());
                }
            }

            return dateRange;
        } finally {
            dataset.close();
        }
    }

    public static void main(String[] args) {
//        URI catalogURI = URI.create("http://runoff.cr.usgs.gov:8086/thredds/hydrologic_catalog.xml");
//        URI catalogURI = URI.create("http://runoff:8086/thredds/catalog.xml");
//        URI catalogURI = URI.create("http://geoport.whoi.edu:8081/thredds/multi_catalog_all.xml");
        URI catalogURI = new File("C:/Documents and Settings/cwardgar/Desktop/multi_catalog_all.xml").toURI();
        InvCatalogFactory factory = new InvCatalogFactory("default", true);
        InvCatalog catalog = factory.readXML(catalogURI);

        StringBuilder buff = new StringBuilder();
        if (!catalog.check(buff)) {
            System.err.println(buff.toString());
        }

        List<InvAccess> handles = getDatasetHandles(catalog, ServiceType.OPENDAP);
        for (InvAccess handle : handles) {
            System.out.println(handle.getDataset().getCatalogUrl());
        }
    }
}
