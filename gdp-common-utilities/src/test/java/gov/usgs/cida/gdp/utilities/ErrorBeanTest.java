package gov.usgs.cida.gdp.utilities;

import gov.usgs.cida.gdp.utilities.bean.ErrorBean;
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

	@Test
	public void testInitializeWithInteger() {
		ErrorBean errBean = new ErrorBean(Integer.valueOf(0));
		assertNotNull(errBean.getErrorMessage());
		assertEquals(errBean.getErrorMessage(), "No Command Has Been Provided - To list all available commands, use command=commandlist");
	}
	
	@Test
	public void testInitializeWithIntegerXmlOutput() {
		ErrorBean errBean = new ErrorBean(Integer.valueOf(0));
		assertNotNull(errBean.getErrorMessage());
		assertEquals(errBean.getErrorMessage(), "No Command Has Been Provided - To list all available commands, use command=commandlist");
		String result = errBean.toXml();
		assertNotNull(result);
	}
}
