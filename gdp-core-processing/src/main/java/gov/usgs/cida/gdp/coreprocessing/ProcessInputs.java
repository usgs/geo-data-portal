package gov.usgs.cida.gdp.coreprocessing;

import java.util.ArrayList;

public class ProcessInputs {
    // Geometry
    public String wfsURL;
    public String featureType;
    public String attribute;
    public ArrayList<String> features = new ArrayList<String>();;

    public String dataSetInterface;

    // THREDDS
    public String threddsURL;
    public String threddsDataset;
    public ArrayList<String> threddsDataTypes = new ArrayList<String>();
    public String threddsFromDate;
    public String threddsToDate;
    public String threddsGroupBy;

    // WCS
    public String wcsURL;
    public String wcsCoverage;
    public String wcsDataType;
    public String wcsGridCRS;
    public String wcsGridOffsets;
    public String wcsBoundingBox;
    public String wcsResampleFactor;
    public String wcsResampleFilter;

    // Output
    public ArrayList<String> outputStats = new ArrayList<String>();
    public String output;
    public String outputFile;
    public String delimId;
    public String email;
}
