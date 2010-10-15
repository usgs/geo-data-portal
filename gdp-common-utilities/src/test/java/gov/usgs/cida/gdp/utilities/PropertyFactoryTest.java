package gov.usgs.cida.gdp.utilities;

import static org.junit.Assert.*;
import java.util.Enumeration;
import java.util.List;
import org.junit.AfterClass;
import org.junit.BeforeClass; 

import org.junit.Test;
import org.slf4j.LoggerFactory;

public class PropertyFactoryTest {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(PropertyFactoryTest.class);

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        log.debug("Started testing class.");
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        log.debug("Ended testing class.");
    }

    @Test
    public final void testGetKeys() {
        Enumeration<Object> result = PropertyFactory.getKeys();
        assertNotNull(result);
        assertTrue(result.hasMoreElements());
    }

    @Test
    public final void testGetPropertyWithFakeKey() {
        String result = PropertyFactory.getProperty("does.not.exist");
        assertEquals("", result);
    }

    @Test
    public final void testGetPropertyWithRealKey() {
        String result = PropertyFactory.getProperty("server.url.0");
        assertEquals("RUNOFF;http://runoff.cr.usgs.gov:8086/thredds/hydrologic_catalog.xml", result);
    }

        @Test
    public final void testGetPropertyWithNullKey() {
        String result = PropertyFactory.getProperty(null);
        assertNull(result);
    }

    @Test
    public final void testSetProperty() {
        String result = PropertyFactory.getProperty("server.url.0");
        assertEquals("RUNOFF;http://runoff.cr.usgs.gov:8086/thredds/hydrologic_catalog.xml", result);
        PropertyFactory.setProperty("test...test", "...");
        result = PropertyFactory.getProperty("test...test");
        assertEquals("...", result);
    }

    @Test
    public final void testGetValueList() {
        List<String> result = null;
        result = PropertyFactory.getValueList("server.url");
        assertNotNull(result);
        assertTrue(result.size() > 1);
    }
}
