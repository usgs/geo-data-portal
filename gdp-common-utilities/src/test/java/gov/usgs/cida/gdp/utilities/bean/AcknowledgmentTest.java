package gov.usgs.cida.gdp.utilities.bean;

import static org.junit.Assert.*;

import org.junit.Test;

import com.vividsolutions.jts.util.Assert;

public class AcknowledgmentTest {

        @Test
        public void testSetStatusToOne() {
            Acknowledgement ackBean = new Acknowledgement(1);
            assertEquals("OK", ackBean.getStatus());
            ackBean.setStatus("FAIL");
            assertEquals("FAIL", ackBean.getStatus());

        }


        @Test
        public void testSetStatusToTwo() {
            Acknowledgement ackBean = new Acknowledgement(2);
            assertEquals("FAIL", ackBean.getStatus());

        }


	@Test
	public void testInvalidStatus() {
		try {
			@SuppressWarnings("unused")
			Acknowledgement ackBean = new Acknowledgement(3);
		} catch (IllegalArgumentException e) {
			return;
		}
		
		Assert.shouldNeverReachHere();
	}
}
