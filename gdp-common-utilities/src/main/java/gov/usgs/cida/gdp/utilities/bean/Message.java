package gov.usgs.cida.gdp.utilities.bean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("messages")
public class Message implements XmlResponse {

    @XStreamAlias("message")
    @XStreamImplicit
    private List<String> message;

    // A JavaBean must have a public, nullary constructor. We must explicitly provide it because the generation
    // of the default constructor has been suppressed by the presence of other constructors in this class.
    public Message() {
        this(new String[0]);
    }

    public Message(String... messages) {
        this.message = new ArrayList<String>();
        message.addAll(Arrays.asList(messages));
    }

    public List<String> getMessages() {
        if (this.message == null) {
            this.message = new ArrayList<String>();
        }
        return this.message;
    }

    public void setMessages(List<String> localMessages) {
        this.message = localMessages;
    }

    public boolean addMessage(String message) {
        if (this.message == null) {
            this.message = new ArrayList<String>();
        }
        return this.message.add(message);
    }
}
