package gov.usgs.gdp.bean;

import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;

import org.junit.Test;

public class AttributeBeanTest {


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
