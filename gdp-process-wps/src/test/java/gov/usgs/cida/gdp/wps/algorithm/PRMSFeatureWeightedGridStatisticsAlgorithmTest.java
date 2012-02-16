/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usgs.cida.gdp.wps.algorithm;

import java.io.File;
import java.util.Arrays;
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
    public void testCsv2param_File_File() throws Exception {
        PRMSFeatureWeightedGridStatisticsAlgorithm.csv2param(
                Arrays.asList( new File[] {
                    new File ("/Users/tkunicki/Downloads/cidaPortalInfo/acfHrus.Prcp.csv"),
                    new File ("/Users/tkunicki/Downloads/cidaPortalInfo/acfHrus.Tmin.csv"),
                    new File ("/Users/tkunicki/Downloads/cidaPortalInfo/acfHrus.Tmax.csv"),
                }),
                new File ("/Users/tkunicki/Downloads/cidaPortalInfo/test/acfHrus.params"));
    }

    @Test
    public void testCsv2data_File_File() throws Exception {
        PRMSFeatureWeightedGridStatisticsAlgorithm.csv2data(
                Arrays.asList( new File[] {
                    new File ("/Users/tkunicki/Downloads/cidaPortalInfo/acfHrus.Prcp.csv"),
                    new File ("/Users/tkunicki/Downloads/cidaPortalInfo/acfHrus.Tmin.csv"),
                    new File ("/Users/tkunicki/Downloads/cidaPortalInfo/acfHrus.Tmax.csv"),
                }),
                new File ("/Users/tkunicki/Downloads/cidaPortalInfo/test/acfHrus.data"));
    }
}
