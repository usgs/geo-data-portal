package gov.usgs.gdp.bean;

import java.util.Date;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class ErrorBean {
	private final static XStream XSTREAM;
	public final static int ERR_NO_COMMAND = 1;
	
	private int errorNumber;
	private String errorMessage;
	private Exception exception;
	private Date errorCreated;
	private String errorClass;
	
	public ErrorBean() {};
	public ErrorBean(String errorMessage) { 
		this(errorMessage, -1); 
	}
	
	public ErrorBean(String errorMessage, int errorNumber) { 
		this(errorMessage, errorNumber, null); 
	}
	
	public ErrorBean(String errorMessage, int errorNumber, Exception stackTrace) { 
		this(errorMessage, errorNumber, stackTrace, null); 
	}
	public ErrorBean(String errorMessage,int errorNumber, Exception stackTrace, String errorClass) {
		setErrorMessage(errorMessage);
		setErrorNumber(errorNumber);
		setException(stackTrace);
		setErrorClass(errorClass);
		setErrorCreated(new Date());
	}
	
	static {
		// Setup XStream, we want to configure for readability since editing
		// of this file is the only way for users to modify color ramps...
		XSTREAM = new XStream(new DomDriver());
		XSTREAM.alias("error", ErrorBean.class);
		/*XSTREAM.useAttributeFor(ErrorBean.class, "errorMessage");
		XSTREAM.aliasField("errorNumber", ErrorBean.class, "code");*/

	}
	
	public String toXml() {
		StringBuffer sb = new StringBuffer();
		String result = "";
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		sb.append(XSTREAM.toXML(this));
		result = sb.toString();
		return result;
	}
	
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setException(Exception stackTrace) {
		this.exception = stackTrace;
	}

	public Exception getException() {
		return exception;
	}

	public void setErrorCreated(Date errorCreated) {
		this.errorCreated = errorCreated;
	}

	public Date getErrorCreated() {
		return errorCreated;
	}

	public void setErrorClass(String errorClass) {
		this.errorClass = errorClass;
	}

	public String getErrorClass() {
		return errorClass;
	}
	public void setErrorNumber(int errorNumber) {
		this.errorNumber = errorNumber;
	}
	public int getErrorNumber() {
		return errorNumber;
	}
	
	
	
}
