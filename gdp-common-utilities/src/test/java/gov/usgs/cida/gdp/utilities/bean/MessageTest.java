package gov.usgs.cida.gdp.utilities.bean;

import gov.usgs.cida.gdp.utilities.bean.Message;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class MessageTest {

	@Test
	public void createMessageBean() {
		Message result = new Message();
		assertNotNull(result);
	}
	
	@Test
	public void testGetMessagesWithNullMessageParam() {
		Message messageBean = new Message();
                messageBean.setMessages(null);
		List<String> result = messageBean.getMessages();
		assertNotNull(result);
		assertTrue(result.isEmpty());
	}
        @Test
	public void testAddMessageWithNullMessageParam() {
		Message messageBean = new Message();
                messageBean.setMessages(null);
                messageBean.addMessage("test");
		List<String> result = messageBean.getMessages();
		assertNotNull(result);
		assertFalse(result.isEmpty());
	}


        @Test
	public void testGetMessagesWithMessagesInConstructor() {
		Message messageBean = new Message("test1", "test2");
		List<String> result = messageBean.getMessages();
		assertNotNull(result);
		assertFalse(result.isEmpty());
                assertTrue(result.size() == 2);
	}

	@Test
	public void testSetmessages() {
		Message messageBean = new Message();
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
		Message messageBean = new Message();
		messageBean.addMessage("Test message");
		List<String> result = messageBean.getMessages();
		assertNotNull(result);
		assertFalse(result.isEmpty());
		assertTrue("Test message".equals(result.get(0)));
	}

}
