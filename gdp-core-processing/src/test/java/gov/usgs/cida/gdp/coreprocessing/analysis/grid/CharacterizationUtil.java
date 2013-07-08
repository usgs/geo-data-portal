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
    
    // This is used to test for changes in responses.
    // It doesn't mean the existing "expected" response was correct just that the output has changed
    static void characterize(File responseDirectory, Class<?> testClass, String test, String response, String suffix) throws URISyntaxException, IOException {

        File responseFile = new File(responseDirectory, testClass.getSimpleName() + "." + test + "." + suffix);

        if (responseFile.canRead()) {
            String expected = FileUtils.readFileToString(responseFile);
            try {
                assertThat("\nThe response has been modified.  If a change is expected:\n" +
                        "\t1) manually verify the change\n" +
                        "\t2) delete the existing reponse file: " + responseFile.getAbsolutePath() + "\n" +
                        "\t3) rerun the test to generate a new response file (this will cause a test failure as a warning)\n" +
                        "\t4) rerun rhe test to verify the new reponse file is valid (this should cause a test pass)\n" +
                        "\t5) commit the new test file",
                        response, is(equalTo(expected)));
            } catch (AssertionError e) {
                FileUtils.writeStringToFile(
                        new File(testClass.getSimpleName() + "." + test + "." + suffix),
                        response);
                throw e;
            }
        } else {
            FileUtils.writeStringToFile(responseFile, response);
            // to harsh?  Maybe not, somebody forgot to commit test data...
            fail("characterization test failed, unable to find previous result to compare in " + responseFile.getAbsolutePath());
        }
    } 
}
