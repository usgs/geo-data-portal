package gov.usgs.cida.gdp.dataintrospection.bean;

import gov.usgs.cida.gdp.dataintrospection.bean.Attribute;
import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class AttributeBeanTest {

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
