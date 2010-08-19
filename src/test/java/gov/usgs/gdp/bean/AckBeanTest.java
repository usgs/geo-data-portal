package gov.usgs.gdp.bean;

import gov.usgs.cida.gdp.webapp.bean.AckBean;
import static org.junit.Assert.*;


import org.junit.Test;

import com.vividsolutions.jts.util.Assert;

public class AckBeanTest {


	@Test
	public void testAckOk() {
		AckBean ackBean = new AckBean(AckBean.ACK_OK);
		
		String result = ackBean.toXml();
		assertNotNull(result);
	}
	
	@Test
	public void testAckFail() {
		AckBean ackBean = new AckBean(AckBean.ACK_FAIL);
		
		String result = ackBean.toXml();
		assertNotNull(result);
	}

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
