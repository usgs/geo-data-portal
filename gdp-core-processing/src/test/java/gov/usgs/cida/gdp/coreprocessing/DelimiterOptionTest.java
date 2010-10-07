package gov.usgs.cida.gdp.coreprocessing;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author isuftin
 */
public class DelimiterOptionTest {

    private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DelimiterOptionTest.class);

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        log.debug("Started testing class.");
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        log.debug("Ended testing class.");
    }

    /**
     * Test of values method, of class DelimiterOption.
     */
    @Test
    public void testValues() {
        assertEquals(DelimiterOption.c.toString(), "[comma]");
        assertEquals(DelimiterOption.s.toString(), "[space]");
        assertEquals(DelimiterOption.t.toString(), "[tab]");
        assertEquals(DelimiterOption.getDefault(), DelimiterOption.c);
    }
}
