package gov.usgs.gdp.bean;

import static org.junit.Assert.*;

import java.util.Date;

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
	public void testInvalidStatus() {
		try {
			AckBean ackBean = new AckBean(3);
		} catch (IllegalArgumentException e) {
			return;
		}
		
		Assert.shouldNeverReachHere();
	}
}
