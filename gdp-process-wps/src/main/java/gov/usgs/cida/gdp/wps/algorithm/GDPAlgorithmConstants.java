/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usgs.cida.gdp.wps.algorithm;

/**
 *
 * @author tkunicki
 */
public class GDPAlgorithmConstants {
    
    private GDPAlgorithmConstants() { }
    
    public final static String FEATURE_COLLECTION_IDENTIFIER = "FEATURE_COLLECTION";
    public final static String FEATURE_COLLECTION_TITLE = "Feature Collection";
    public final static String FEATURE_COLLECTION_ABSTRACT = "A feature collection encoded as a WFS request or one of the supported GML profiles.";
    
    public final static String FEATURE_ATTRIBUTE_NAME_IDENTIFIER = "FEATURE_ATTRIBUTE_NAME";
    public final static String FEATURE_ATTRIBUTE_NAME_TITLE = "Feature Attribute Name";
    public final static String FEATURE_ATTRIBUTE_NAME_ABSTRACT = "The attribute that will be used to label column headers in processing output.";
    
    public final static String DATASET_URI_IDENTIFIER = "DATASET_URI";
    public final static String DATASET_URI_TITLE = "Dataset URI";
    public final static String DATASET_URI_ABSTRACT = "The base data web service URI for the dataset of interest.";
    
    public final static String DATASET_ID_IDENTIFIER = "DATASET_ID";
    public final static String DATASET_ID_TITLE = "Dataset Identifier";
    public final static String DATASET_ID_ABSTRACT = "The unique identifier for the data type or variable of interest.";
    
    
    public final static String REQUIRE_FULL_COVERAGE_IDENTIFIER = "REQUIRE_FULL_COVERAGE";
    public final static String REQUIRE_FULL_COVERAGE_TITLE = "Require Full Coverage";
    public final static String REQUIRE_FULL_COVERAGE_ABSTRACT = "If turned on, the service will require that the dataset of interest fully cover the polygon analysis zone data.";
    
    public final static String TIME_START_IDENTIFIER = "TIME_START";
    public final static String TIME_START_TITLE = "Time Start";
    public final static String TIME_START_ABSTRACT = "The date to begin analysis.";
    
    
    public final static String TIME_END_IDENTIFIER = "TIME_END";
    public final static String TIME_END_TITLE = "Time End";
    public final static String TIME_END_ABSTRACT = "The date to end analysis.";
    
    
    public final static String DELIMITER_IDENTIFIER = "DELIMITER";
    public final static String DELIMITER_TITLE = "Delimiter";
    public final static String DELIMITER_ABSTRACT = "The delimiter that will be used to separate columns in the processing output.";
}
