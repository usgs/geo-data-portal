package gov.usgs.gdp.bean;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
@XStreamAlias("response")
public class MessageBean {
	
	@XStreamAlias("messages")
	@XStreamImplicit
	private List<String> messages;

	public String toXml() {
		XStream xstream = new XStream();
		xstream.processAnnotations(MessageBean.class);
		StringBuffer sb = new StringBuffer();
		String result = "";
		sb.append(xstream.toXML(this));
		result = sb.toString();
		return result;
	}
	
	public List<String> getMessages() {
		if (this.messages == null) this.messages = new ArrayList<String>();
		return this.messages;
	}

	public void setMessages(List<String> localMessages) {
		this.messages = localMessages;
	}
	
	public boolean addMessage(String message) {
		if (this.messages == null) this.messages = new ArrayList<String>();
		return this.messages.add(message);
	}
}
