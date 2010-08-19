package gov.usgs.cida.gdp.webapp.bean;

import gov.usgs.cida.gdp.utilities.bean.XmlBean;
import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("messages")
public class MessageBean implements XmlBean {
	@XStreamAlias("message")
	@XStreamImplicit
	private List<String> message;

    // A JavaBean must have a public, nullary constructor. We must explicitly provide it because the generation
    // of the default constructor has been suppressed by the presence of other constructors in this class.
    public MessageBean() {
        this(new String[0]);
    }

	public MessageBean(String... messages) {
		this.message = new ArrayList<String>();
		for (String singleMessage : messages) {
			message.add(singleMessage);
		}
	}

	@Override
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
		if (this.message == null) this.message = new ArrayList<String>();
		return this.message;
	}

	public void setMessages(List<String> localMessages) {
		this.message = localMessages;
	}

	public boolean addMessage(String message) {
		if (this.message == null) this.message = new ArrayList<String>();
		return this.message.add(message);
	}
}
