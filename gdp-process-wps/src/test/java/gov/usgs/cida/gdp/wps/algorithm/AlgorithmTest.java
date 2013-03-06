package gov.usgs.cida.gdp.wps.algorithm;

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
        validateAlgorithmDescription(new FeatureCategoricalGridCoverageAlgorithm());
        validateAlgorithmDescription(new FeatureCoverageIntersectionAlgorithm());
        validateAlgorithmDescription(new FeatureCoverageOPeNDAPIntersectionAlgorithm());
        validateAlgorithmDescription(new FeatureGridStatisticsAlgorithm());
        validateAlgorithmDescription(new FeatureWeightedGridStatisticsAlgorithm());
        validateAlgorithmDescription(new PRMSParameterGeneratorAlgorithm());
    }
    
    private void validateAlgorithmDescription(IAlgorithm algorithm) {
        System.out.println();
        System.out.println(" ### DescribeProcess for " + algorithm.getClass().getName() + " ###");
        System.out.println(AlgorithmUtil.convertProcessDescriptionToXMLString(algorithm));
        System.out.println();
        assertTrue(AlgorithmUtil.processDescriptionIsValid(algorithm)); 
    }
}
