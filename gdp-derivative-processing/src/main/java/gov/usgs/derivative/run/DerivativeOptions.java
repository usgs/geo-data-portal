package gov.usgs.derivative.run;

import java.io.File;

/**
 *
 * @author Jordan Walker <jiwalker@usgs.gov>
 */
public class DerivativeOptions {
    
    enum Process {
        P1D,
        P1Y,
        P1Y30D,
        P30Y,
        P1M,
        SPATIAL;
    }    

    private File dataset;
    
    private File outputDir;
    
    private String precipVar = "pr";
    private String tminVar = "tmin";
    private String tmaxVar = "tmax";
    
}
