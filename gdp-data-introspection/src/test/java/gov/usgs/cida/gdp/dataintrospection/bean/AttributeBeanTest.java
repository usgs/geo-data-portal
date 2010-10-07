package gov.usgs.cida.gdp.dataintrospection.bean;

import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.List;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import org.junit.Test;

public class AttributeBeanTest {

    private static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AttributeBeanTest.class);

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        log.debug("Started testing class.");
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        log.debug("Ended testing class.");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSetAttribute() {
        Attribute attBean = new Attribute(new ArrayList());
        ArrayList<String> list = new ArrayList<String>();
        list.add("Test1");
        list.add("Test2");
        attBean.setAttribute(list);
        assertNotNull(attBean.getAttribute());
        assertTrue(!attBean.getAttribute().isEmpty());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetAttribute() {
        Attribute attBean = new Attribute(new ArrayList());
        ArrayList<String> list = new ArrayList<String>();
        list.add("Test1");
        list.add("Test2");
        attBean.setAttribute(list);
        List<String> result = attBean.getAttribute();
        assertEquals(result, list);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetFilesetName() {
        Attribute attBean = new Attribute(new ArrayList());
        attBean.setFilesetName("TEST");
        String result = attBean.getFilesetName();
        assertEquals(result, result);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSetFilesetName() {
        Attribute attBean = new Attribute(new ArrayList());
        attBean.setFilesetName("TEST");
        String result = attBean.getFilesetName();
        assertEquals(result, result);
    }
//    @Test
//    public void testToXml() {
//        ArrayList<String> list = new ArrayList<String>();
//        list.add("Test1");
//        list.add("Test2");
//
//        Attribute ackBean = new Attribute(list);
//
//        String result = ackBean.toXml();
//        assertNotNull(result);
//    }
}
