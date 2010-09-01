package gov.usgs.cida.gdp.utilities.bean;

import gov.usgs.cida.gdp.utilities.bean.Error;
import static org.junit.Assert.*;

import java.util.Date;

import org.junit.Test;

public class ErrorTest {


//	@Test
//	public void testToXmlWithBeanFilled() {
//		Error errBean = new Error();
//		errBean.setErrorClass("FakeName");
//		errBean.setErrorCreated(new Date());
//		errBean.setErrorMessage("Fake Message");
//		errBean.setException(new Exception("Fake Exception"));
//		
//		String result = errBean.toXml();
//		assertNotNull(result);
//	}
	
//	@Test
//	public void testToXmlWithSparselyFilledBean() {
//		Error errBean = new Error();
//		errBean.setErrorClass("FakeName");
//		errBean.setErrorCreated(new Date());
//		
//		String result = errBean.toXml();
//		assertNotNull(result);
//	}
	
//	@Test
//	public void testToXmlWithEmptyBean() {
//		Error errBean = new Error();
//		
//		String result = errBean.toXml();
//		assertNotNull(result);
//	}

	@Test
	public void testInitializeWithInteger() {
		Error errBean = new Error(Integer.valueOf(0));
		assertNotNull(errBean.getErrorMessage());
		assertEquals(errBean.getErrorMessage(), "No Command Has Been Provided - To list all available commands, use command=commandlist");
	}
	
//	@Test
//	public void testInitializeWithIntegerXmlOutput() {
//		Error errBean = new Error(Integer.valueOf(0));
//		assertNotNull(errBean.getErrorMessage());
//		assertEquals(errBean.getErrorMessage(), "No Command Has Been Provided - To list all available commands, use command=commandlist");
//		String result = errBean.toXml();
//		assertNotNull(result);
//	}
}
