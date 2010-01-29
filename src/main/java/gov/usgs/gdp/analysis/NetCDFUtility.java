package gov.usgs.gdp.analysis;

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

public class NetCDFUtility {
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

    public static List<String> getDataVariableNames(String location) throws IOException {

        if (location == null) {
            throw new IllegalArgumentException("location can't be null");
        }

        List<String> variableNames = new ArrayList<String>();
        FeatureDataset dataset = null;
        try {
            dataset = FeatureDatasetFactoryManager.open(
                    null, location, null, new Formatter());
            if (dataset.getFeatureType() == FeatureType.STATION) {
                for (VariableSimpleIF variable : dataset.getDataVariables()) {
                    String variableName = variable.getName();
                    if (!variableName.startsWith("station.")) {
                        variableNames.add(variable.getName());
                    }
                }
            } else {
                for (VariableSimpleIF variable : dataset.getDataVariables()) {
                    variableNames.add(variable.getName());
                }
            }
        } finally {
            if (dataset != null) {
                dataset.close();
            }
        }
        return variableNames;
    }

    public static List<String> getDateRange(String location, String variableName) throws IOException {
        if (location == null) {
            throw new IllegalArgumentException("location can't be null");
        }

        List<String> dateRange = new ArrayList<String>(2);
        FeatureDataset dataset = null;
        try {
            dataset = FeatureDatasetFactoryManager.open(
                    null, location, null, new Formatter());
            if (dataset.getFeatureType() == FeatureType.GRID) {
                GeoGrid grid = ((GridDataset) dataset).findGridByName(variableName);
                List<NamedObject> times = grid.getTimes();
                dateRange.set(0, times.get(0).getName());
                dateRange.set(0, times.get(times.size() - 1).getName());
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
        } finally {
            if (dataset != null) {
                dataset.close();
            }
        }
        return dateRange;
//
//            Formatter errorLog = new Formatter();
//            FeatureDataset featureDataset = FeatureDatasetFactoryManager.open(
//                    null, location, null, errorLog);
//
//            if (featureDataset != null) {
//
//                List<String> timeSelectItemList = new ArrayList<String>();
//                if (featureDataset instanceof GridDataset) {
//                    GeoGrid grid = ((GridDataset)featureDataset).findGridByName(gridSelection);
//
//                    for (NamedObject time : grid.getTimes()) {
//                        timeSelectItemList.add(time.getName());
//                    }
//                } else {
//                    // TODO:
//                }
//            }

//                    FeatureDatasetPoint fdp = (FeatureDatasetPoint) dataset;
//            for (FeatureCollection fc : fdp.getPointFeatureCollectionList()) {
//                StationTimeSeriesFeatureCollection sc = (StationTimeSeriesFeatureCollection) fc;
//                for (Station s : sc.getStations()) {
//                    System.out.println("  " + s.getName());
//                }
//            }
    }

    public static void main(String[] args) {
        URI catalogURI = URI.create("http://runoff.cr.usgs.gov:8086/thredds/hydrologic_catalog.xml");
        InvCatalogFactory factory = new InvCatalogFactory("default", true);
        InvCatalog catalog = factory.readXML(catalogURI);

        StringBuilder buff = new StringBuilder();
        if (!catalog.check(buff)) {
            System.err.println(buff.toString());
        }

        List<InvAccess> opendapDatasets = getDatasetHandles(catalog, ServiceType.OPENDAP);
    }
}
