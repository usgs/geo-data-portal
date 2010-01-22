package gov.usgs.gdp.bean;

import static org.junit.Assert.*;

import java.util.Date;

import org.junit.Test;

public class ErrorBeanTest {


	@Test
	public void testToXmlWithBeanFilled() {
		ErrorBean errBean = new ErrorBean();
		errBean.setErrorClass("FakeName");
		errBean.setErrorCreated(new Date());
		errBean.setErrorMessage("Fake Message");
		errBean.setException(new Exception("Fake Exception"));
		
		String result = errBean.toXml();
		assertNotNull(result);
	}
	
	@Test
	public void testToXmlWithSparselyFilledBean() {
		ErrorBean errBean = new ErrorBean();
		errBean.setErrorClass("FakeName");
		errBean.setErrorCreated(new Date());
		
		String result = errBean.toXml();
		assertNotNull(result);
	}
	
	@Test
	public void testToXmlWithEmptyBean() {
		ErrorBean errBean = new ErrorBean();
		
		String result = errBean.toXml();
		assertNotNull(result);
	}

}
