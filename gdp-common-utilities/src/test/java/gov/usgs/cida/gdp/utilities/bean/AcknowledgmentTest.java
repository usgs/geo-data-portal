package gov.usgs.cida.gdp.utilities.bean;

import static org.junit.Assert.*;

import org.junit.Test;

import com.vividsolutions.jts.util.Assert;
import gov.usgs.cida.gdp.utilities.bean.Acknowledgement;

public class AcknowledgmentTest {

        @Test
        public void testSetStatus() {
            Acknowledgement ackBean = new Acknowledgement(1);
            assertEquals("OK", ackBean.getStatus());
            ackBean.setStatus("FAIL");
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
