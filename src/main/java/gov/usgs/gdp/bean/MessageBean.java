package gov.usgs.gdp.bean;

import java.util.ArrayList;
import java.util.List;

public class MessageBean {
	private List<String> messages;

	public List<String> getMessages() {
		if (this.messages == null) this.messages = new ArrayList<String>();
		return this.messages;
	}

	public void setMessages(List<String> localMessages) {
		this.messages = localMessages;
	}
	
	public boolean addMessage(String message) {
		return this.messages.add(message);
	}
}
