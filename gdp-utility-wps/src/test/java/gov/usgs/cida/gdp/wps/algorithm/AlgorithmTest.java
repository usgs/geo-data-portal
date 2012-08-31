package gov.usgs.cida.gdp.wps.algorithm;

import gov.usgs.cida.gdp.wps.algorithm.communication.EmailWhenFinishedAlgorithm;
import gov.usgs.cida.gdp.wps.algorithm.communication.GeoserverManagementAlgorithm;
import gov.usgs.cida.gdp.wps.algorithm.discovery.CalculateWCSCoverageInfo;
import gov.usgs.cida.gdp.wps.algorithm.discovery.GetGridTimeRange;
import gov.usgs.cida.gdp.wps.algorithm.discovery.GetWcsCoverages;
import gov.usgs.cida.gdp.wps.algorithm.discovery.ListOpendapGrids;
import gov.usgs.cida.gdp.wps.algorithm.filemanagement.CreateNewShapefileDataStore;
import gov.usgs.cida.gdp.wps.algorithm.filemanagement.GetWatersGeom;
import gov.usgs.cida.gdp.wps.algorithm.filemanagement.ReceiveFiles;
import gov.usgs.cida.n52.wps.test.AlgorithmUtil;
import gov.usgs.cida.n52.wps.test.MockUtil;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.xmlbeans.XmlException;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;
import org.n52.wps.server.IAlgorithm;

/**
 *
 * @author tkunicki
 */
public class AlgorithmTest {
 
    @BeforeClass
    public static void initializeWPSConfig() {
        try {
            MockUtil.getMockConfig();
        } catch (XmlException ex) {
            Logger.getLogger(AlgorithmTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(AlgorithmTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Test
    public void testAlgorithmDescriptions() {
        validateAlgorithmDescription(new ReceiveFiles());
        validateAlgorithmDescription(new GetWatersGeom());
        validateAlgorithmDescription(new CreateNewShapefileDataStore());
        validateAlgorithmDescription(new EmailWhenFinishedAlgorithm());
        validateAlgorithmDescription(new GeoserverManagementAlgorithm());
        validateAlgorithmDescription(new ListOpendapGrids());
        validateAlgorithmDescription(new CalculateWCSCoverageInfo());
        validateAlgorithmDescription(new GetWcsCoverages());
        validateAlgorithmDescription(new GetGridTimeRange());
    }
    
    private void validateAlgorithmDescription(IAlgorithm algorithm) {
        System.out.println();
        System.out.println(" ### DescribeProcess for " + algorithm.getClass().getName() + " ###");
        System.out.println(AlgorithmUtil.convertProcessDescriptionToXMLString(algorithm));
        System.out.println();
        assertTrue(AlgorithmUtil.processDescriptionIsValid(algorithm)); 
    }
}
