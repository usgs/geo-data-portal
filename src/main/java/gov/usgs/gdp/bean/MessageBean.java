package gov.usgs.gdp.bean;

import java.util.ArrayList;
import java.util.List;

public class MessageBean {
	private List<String> messages;

	public List<String> getMessages() {
		if (this.messages == null) this.messages = new ArrayList<String>();
		return messages;
	}

	public void setmessages(List<String> messages) {
		this.messages = messages;
	}
	
	public boolean addMessage(String message) {
		List<String> messages = getMessages();
		return messages.add(message);
	}
}
