package gov.usgs.gdp.bean;

import gov.usgs.cida.gdp.webapp.bean.MessageBean;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class MessageBeanTest {

	@Test
	public void createMessageBean() {
		MessageBean result = new MessageBean();
		assertNotNull(result);
	}
	
	@Test
	public void testGetMessages() {
		MessageBean messageBean = new MessageBean();
		List<String> result = messageBean.getMessages();
		assertNotNull(result);
		assertTrue(result.isEmpty());
		
	}

	@Test
	public void testSetmessages() {
		MessageBean messageBean = new MessageBean();
		List<String> messageList = new ArrayList<String>(1);
		messageList.add("Test message");
		messageBean.setMessages(messageList);
		List<String> result = messageBean.getMessages();
		assertNotNull(result);
		assertFalse(result.isEmpty());
		assertTrue("Test message".equals(result.get(0)));
	}

	@Test
	public void testAddMessage() {
		MessageBean messageBean = new MessageBean();
		messageBean.addMessage("Test message");
		List<String> result = messageBean.getMessages();
		assertNotNull(result);
		assertFalse(result.isEmpty());
		assertTrue("Test message".equals(result.get(0)));
	}

}
