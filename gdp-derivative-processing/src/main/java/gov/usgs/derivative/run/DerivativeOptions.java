package gov.usgs.derivative.run;

import java.io.File;
import org.kohsuke.args4j.Option;


/**
 *
 * @author Jordan Walker <jiwalker@usgs.gov>
 */
public class DerivativeOptions {
    
    public enum Process {
        P1D,
        P1Y,
        P1Y30D,
        P30Y,
        P1M,
        SPATIAL;
    }
    
    public enum VariableType {
        PRECIP,
        T_MIN,
        T_MAX
    }

    @Option(name = "-d", usage = "input dataset to process derivatives on")
    public String datasetLocation = null;
    
    @Option(name = "-o", metaVar = "DIR", usage = "directory to write output files to")
    public String outputDir = ".";
    
    @Option(name = "-s", usage = "shapefile to do spatial analysis with")
    public File shapefile = null;
    
    @Option(name = "-p", required = true, usage = "which derivative process to run at this step")
    public Process process = null;
    
    @Option(name = "-pr", usage = "name of the precipitation variable")
    public String precipVar = "pr";
    @Option(name = "-tmin", usage = "name of the minimum temperature variable")
    public String tminVar = "tmin";
    @Option(name = "-tmax", usage = "name of the maximum temperature variable")
    public String tmaxVar = "tmax";
    
    @Option(name = "-l", usage = "lower memory footprint")
    public boolean lowMemory = false;
    
    @Option(name = "-h", aliases = "--help", usage = "this help menu")
    public boolean help = false;
}
