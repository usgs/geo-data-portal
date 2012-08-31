package gov.usgs.cida.gdp.wps.algorithm;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.opengis.wps.x100.ProcessDescriptionType;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlOptions;
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
        printAlgorithmProcessDescription(new FeatureCategoricalGridCoverageAlgorithm());
        printAlgorithmProcessDescription(new FeatureCoverageIntersectionAlgorithm());
        printAlgorithmProcessDescription(new FeatureCoverageOPeNDAPIntersectionAlgorithm());
        printAlgorithmProcessDescription(new FeatureGridStatisticsAlgorithm());
        printAlgorithmProcessDescription(new FeatureWeightedGridStatisticsAlgorithm());
        printAlgorithmProcessDescription(new PRMSParameterGeneratorAlgorithm());
    }
    
    private void printAlgorithmProcessDescription(IAlgorithm algorithm) {
        assertTrue(algorithm.processDescriptionIsValid()); 
        System.out.println();
        System.out.println(" ### DescribeProcess for " + algorithm.getClass().getName() + " ###");
        System.out.println(getXMLAsStringFromDescription(algorithm.getDescription()));
        System.out.println();
    }

    private String getXMLAsStringFromDescription(ProcessDescriptionType decription) {
        XmlOptions options = new XmlOptions();
        options.setSavePrettyPrint();
        options.setSaveOuter();
        HashMap ns = new HashMap();
        ns.put("http://www.opengis.net/wps/1.0.0", "wps");
        ns.put("http://www.opengis.net/ows/1.1", "ows");
        options.setSaveNamespacesFirst().
                setSaveSuggestedPrefixes(ns).
                setSaveAggressiveNamespaces();
        return decription.xmlText(options);
    }
}
