/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usgs.cida.gdp.coreprocessing.analysis.grid;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import org.apache.commons.io.FileUtils;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 *
 * @author tkunicki
 */
public class CharacterizationUtil {
    
    static void characterize(File responseDirectory, Class<?> testClass, String test, String response, String suffix) throws URISyntaxException, IOException {

        File responseFile = new File(responseDirectory, testClass.getSimpleName() + "." + test + "." + suffix);

        if (responseFile.canRead()) {
            String expected = FileUtils.readFileToString(responseFile);
            assertThat(response, is(equalTo(expected)));
        } else {
            FileUtils.writeStringToFile(responseFile, response);
            // to harsh?  Maybe not, somebody forgot to commit test data...
            fail("characterization test failed, unable to find previous result to compare in " + responseFile.getAbsolutePath());
        }
    } 
}
