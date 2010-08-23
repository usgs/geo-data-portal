package gov.usgs.cida.gdp.filemanagement.bean;

import gov.usgs.cida.gdp.filemanagement.bean.AttributeBean;
import static org.junit.Assert.*;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class AttributeBeanTest {

    @SuppressWarnings("unchecked")
    @Test
    public void testSetAttribute() {
        AttributeBean attBean = new AttributeBean(new ArrayList());
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
        AttributeBean attBean = new AttributeBean(new ArrayList());
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
        AttributeBean attBean = new AttributeBean(new ArrayList());
        attBean.setFilesetName("TEST");
        String result = attBean.getFilesetName();
        assertEquals(result, result);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSetFilesetName() {
        AttributeBean attBean = new AttributeBean(new ArrayList());
        attBean.setFilesetName("TEST");
        String result = attBean.getFilesetName();
        assertEquals(result, result);
    }

    @Test
    public void testToXml() {
        ArrayList<String> list = new ArrayList<String>();
        list.add("Test1");
        list.add("Test2");

        AttributeBean ackBean = new AttributeBean(list);

        String result = ackBean.toXml();
        assertNotNull(result);
    }
}
