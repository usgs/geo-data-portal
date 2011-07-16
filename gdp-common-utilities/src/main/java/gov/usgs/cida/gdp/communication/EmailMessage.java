package gov.usgs.cida.gdp.communication;

import java.util.*;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

/**
 * Represents an E-Mail message
 *
 * @author isuftin
 *
 */
public class EmailMessage {

    private String from;
    private String to;
    private List<String> cc;
    private List<String> bcc;
    private InternetAddress[] replyTo;
    private String subject;
    private String content;

    public EmailMessage() {
        this.from = "";
        this.to = "";
        this.cc = new Vector<String>();
        this.bcc = new Vector<String>();
        this.subject = "";
        this.content = "";
    }

    public EmailMessage(String from, String to, List<String> cc, String subject, String content) {
        this.from = from;
        this.to = to;
        this.cc = cc;
        this.subject = subject;
        this.content = content;
    }

    public EmailMessage(String from, String to, List<String> cc, List<String> bcc, String subject, String content) {
        this.from = from;
        this.to = to;
        this.cc = cc;
        this.bcc = bcc;
        this.subject = subject;
        this.content = content;
    }

    public String getFrom() {
        return this.from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return this.to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public List<String> getCc() {
        if (this.cc == null) {
            setCc(new ArrayList<String>());
        }
        return this.cc;
    }

    public void setCc(List<String> cc) {
        this.cc = cc;
    }

    public String getSubject() {
        return this.subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<String> getBcc() {
        if (this.bcc == null) {
            setBcc(new ArrayList<String>());
        }
        return this.bcc;
    }

    public String getBccToString() {
        if (getBcc().isEmpty()) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        for (String emailAddress : getBcc()) {
            result.append(emailAddress).append(",");
        }
        // Return the string without the trailing comma
        return result.toString().substring(0, result.toString().length() - 1);
    }

	public String getCcToString() {
        if (getCc().isEmpty()) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        for (String emailAddress : getCc()) {
            result.append(emailAddress).append(",");
        }
        // Return the string without the trailing comma
        return result.toString().substring(0, result.toString().length() - 1);
    }

    public void setBcc(List<String> bcc) {
        this.bcc = bcc;
    }
    
    public void send() throws AddressException, MessagingException {
        EmailHandler.sendMessage(this);
    }

    public InternetAddress[] getReplyTo() {
        return this.replyTo;
    }
    
    public void setReplyTo(InternetAddress[] replyTo) {
        this.replyTo = replyTo;
    }
}
