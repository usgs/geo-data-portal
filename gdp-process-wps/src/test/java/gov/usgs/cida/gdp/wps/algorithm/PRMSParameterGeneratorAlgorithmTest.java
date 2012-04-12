package gov.usgs.cida.gdp.wps.algorithm;

import gov.usgs.cida.gdp.wps.algorithm.PRMSParameterGeneratorAlgorithm.HRU;
import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author tkunicki
 */
public class PRMSParameterGeneratorAlgorithmTest {

    public PRMSParameterGeneratorAlgorithmTest() {
    }

    @Test
    @Ignore
    public void testCsv2param_File_File() throws Exception {
        PRMSParameterGeneratorAlgorithm.csv2param(
                Arrays.asList( new File[] {
                    new File ("/Users/tkunicki/Downloads/cidaPortalInfo/acfHrus.Prcp.csv"),
                    new File ("/Users/tkunicki/Downloads/cidaPortalInfo/acfHrus.Tmin.csv"),
                    new File ("/Users/tkunicki/Downloads/cidaPortalInfo/acfHrus.Tmax.csv"),
                }),
                new File ("/Users/tkunicki/Downloads/cidaPortalInfo/test/acfHrus.params"));
    }

    @Test
    @Ignore
    public void testCsv2data_File_File() throws Exception {
        PRMSParameterGeneratorAlgorithm.csv2data(
                Arrays.asList( new File[] {
                    new File ("/Users/tkunicki/Downloads/cidaPortalInfo/acfHrus.Prcp.csv"),
                    new File ("/Users/tkunicki/Downloads/cidaPortalInfo/acfHrus.Tmin.csv"),
                    new File ("/Users/tkunicki/Downloads/cidaPortalInfo/acfHrus.Tmax.csv"),
                }),
                new File ("/Users/tkunicki/Downloads/cidaPortalInfo/test/acfHrus.data"));
    }
    
    @Test
    public void testHRU() {
        
        List<HRU> hruList;
        int index;
        
        /* Sometimes data passed in with 'hru' bound as Class<Object> or Class<String>
         * we want to make sure we can covert to numeric ordering
         */
        hruList = Arrays.asList(new HRU[] {
           new HRU("1", 0),
           new HRU("10", 1),
           new HRU("100", 2),
           new HRU("2", 3),
           new HRU("20", 4),
           new HRU("200", 5),
        });
        
        Collections.sort(hruList);
        index = 0;
        assertEquals("1", hruList.get(index++).name);
        assertEquals("2", hruList.get(index++).name);
        assertEquals("10", hruList.get(index++).name);
        assertEquals("20", hruList.get(index++).name);
        assertEquals("100", hruList.get(index++).name);
        assertEquals("200", hruList.get(index++).name);
        
        /* Verify we can workaround 'hacks' people have used to impart numberic
         * order on Strings
         */
        hruList = Arrays.asList(new HRU[] {
           new HRU("001", 0),
           new HRU("010", 1),
           new HRU("100", 2),
           new HRU("002", 3),
           new HRU("020", 4),
           new HRU("200", 5),
           new HRU("009", 6), // next 3 to make sure these aren't parsed as octal with leading '0'
           new HRU("090", 7),
           new HRU("900", 8),
        });
        
        Collections.sort(hruList);
        index = 0;
        assertEquals("001", hruList.get(index++).name);
        assertEquals("002", hruList.get(index++).name);
        assertEquals("009", hruList.get(index++).name);
        assertEquals("010", hruList.get(index++).name);
        assertEquals("020", hruList.get(index++).name);
        assertEquals("090", hruList.get(index++).name);
        assertEquals("100", hruList.get(index++).name);
        assertEquals("200", hruList.get(index++).name);
        assertEquals("900", hruList.get(index++).name);
        
        hruList = Arrays.asList(new HRU[] {
           new HRU("001", 0),
           new HRU("002", 1),
           new HRU("009", 2),
           new HRU("010", 3),
           new HRU("020", 4),
           new HRU("090", 5),
           new HRU("100", 6), // next 3 to make sure these aren't parsed as octal with leading '0'
           new HRU("200", 7),
           new HRU("900", 8),
        });
        
        Collections.sort(hruList);
        index = 0;
        assertEquals("001", hruList.get(index++).name);
        assertEquals("002", hruList.get(index++).name);
        assertEquals("009", hruList.get(index++).name);
        assertEquals("010", hruList.get(index++).name);
        assertEquals("020", hruList.get(index++).name);
        assertEquals("090", hruList.get(index++).name);
        assertEquals("100", hruList.get(index++).name);
        assertEquals("200", hruList.get(index++).name);
        assertEquals("900", hruList.get(index++).name);
    }
}
