/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usgs.cida.gdp.wps.algorithm;

import java.io.File;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author tkunicki
 */
@Ignore
public class PRMSFeatureWeightedGridStatisticsAlgorithmTest {

    public PRMSFeatureWeightedGridStatisticsAlgorithmTest() {
    }

    @Test
    public void testCsv2prms_File_File() throws Exception {
        PRMSFeatureWeightedGridStatisticsAlgorithm.csv2prms(
                new File ("/Users/tkunicki/Downloads/acfHrus.in.csv"),
                new File ("/Users/tkunicki/Downloads/acfHrus.prms.data"));
    }
}
