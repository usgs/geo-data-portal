package gov.usgs.gdp.bean;

import static org.junit.Assert.*;

import java.io.Console;

import org.junit.Test;

public class CommandListBeanTest {

    @Test
    public void testGetCommandListBean() {
	CommandListBean result = CommandListBean.getCommandListBean();
	assertNotNull(result);
	assertFalse(result.getCommandList().isEmpty());
    }

    @Test
    public void testToXml() {
	CommandListBean instance = CommandListBean.getCommandListBean();
	String result = instance.toXml();
	assertNotNull(result);
	assertNotSame("", result);
	System.out.println(result);
    }

}
