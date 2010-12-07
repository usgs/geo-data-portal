package gov.usgs.cida.gdp.coreprocessing;

public class ProcessInputs {
    // Geometry
    public String wfsURL;
    public String featureType;
    public String attribute;
    public String[] features;

    public String dataSetInterface;

    // THREDDS
    public String threddsDataset;
    public String[] threddsDataTypes;
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
    public String[] outputStats;

    // URL at which the client accessed GDP. Used to send user a getfile link in
    // the processing finished email.
    public String gdpURL;
    public String delimId;
    public String email;
}
