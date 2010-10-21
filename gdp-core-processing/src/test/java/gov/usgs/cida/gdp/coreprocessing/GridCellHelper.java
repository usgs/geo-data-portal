package gov.usgs.cida.gdp.coreprocessing;

import gov.usgs.cida.gdp.utilities.FileHelper;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public class GridCellHelper {

    public static final int T_SIZE = 2;
    public static final int Z_SIZE = 5;
    public static final int Y_SIZE = 3;
    public static final int X_SIZE = 4;
    private static String RESOURCE_PATH;
    private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GridCellHelper.class);

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        log.debug("Started testing class.");
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        log.debug("Ended testing class.");
    }

    public synchronized static String getResourceDir() {
        if (RESOURCE_PATH == null) {
            ClassLoader cl = GridCellHelper.class.getClassLoader();
            URL sampleFileLocation = cl.getResource("Sample_files" + File.separator);
            try {
                RESOURCE_PATH = new File(sampleFileLocation.toURI()).getPath();
            } catch (URISyntaxException e) {
                RESOURCE_PATH = new File(sampleFileLocation.getPath()).getPath();
            }
        }
        return RESOURCE_PATH;
    }
}
