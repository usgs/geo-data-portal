package gov.usgs.cida.gdp.utilities;

import static org.junit.Assert.*;

import org.junit.Test;

import com.vividsolutions.jts.util.Assert;
import gov.usgs.cida.gdp.utilities.bean.AckBean;

public class AckBeanTest {

        @Test
        public void testSetStatus() {
            AckBean ackBean = new AckBean(1);
            assertEquals("OK", ackBean.getStatus());
            ackBean.setStatus("FAIL");
            assertEquals("FAIL", ackBean.getStatus());

        }

	@Test
	public void testInvalidStatus() {
		try {
			@SuppressWarnings("unused")
			AckBean ackBean = new AckBean(3);
		} catch (IllegalArgumentException e) {
			return;
		}
		
		Assert.shouldNeverReachHere();
	}
}
