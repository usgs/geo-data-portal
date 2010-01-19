package gov.usgs.gdp.bean;

import java.util.Date;

import org.apache.log4j.Logger;

public class THREDDSServerBean {

	private static org.apache.log4j.Logger log = Logger.getLogger(THREDDSServerBean.class);
	
	private String hostname;
	private int port;
	private boolean active;
	private Date lastCheck;
	
	public static org.apache.log4j.Logger getLog() {
		return log;
	}
	public static void setLog(org.apache.log4j.Logger log) {
		THREDDSServerBean.log = log;
	}
	public String getHostname() {
		return hostname;
	}
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}
	public Date getLastCheck() {
		return lastCheck;
	}
	public void setLastCheck(Date lastCheck) {
		this.lastCheck = lastCheck;
	}
	
}
