package gov.usgs.gdp.bean;

import java.util.*;

/**
 * Represents and E-Mail message
 * 
 * @author isuftin
 *
 */
public class EmailMessageBean {
private String from;
private String to;
private List<String> cc;
private List<String> bcc;
private String subject;
private String content;

public EmailMessageBean() {
	this.from = "";
	this.to = "";
	this.cc = new Vector<String>();
	this.subject = "";
	this.content = "";
}

public EmailMessageBean(String from, String to, List<String> cc, String subject, String content) {
	this.from = from;
	this.to = to;
	this.cc = cc;
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
	if (this.bcc == null) setBcc(new ArrayList<String>());
	return this.bcc;
}

public String getBccToString() {
	StringBuffer result = new StringBuffer();
	for (String emailAddress : getBcc()) {
		result.append(emailAddress + ",");
	}
	// Return the string without the trailing comma
	return result.toString().substring(0, result.toString().length() - 1);
}

public void setBcc(List<String> bcc) {
	this.bcc = bcc;
}
}