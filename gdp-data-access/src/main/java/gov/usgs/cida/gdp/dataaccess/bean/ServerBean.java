package gov.usgs.cida.gdp.dataaccess.bean;

import java.util.Date;

import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import gov.usgs.cida.gdp.utilities.bean.XmlResponse;

@XStreamAlias("server")
public class ServerBean implements XmlResponse {

	private static org.slf4j.Logger log = LoggerFactory.getLogger(ServerBean.class);
	
        private String name;
	private String protocol;
	private String hostname;
	private int port;
	private String uri;
	private boolean active;
	private Date lastCheck;
        private String fullUrl;
	
	public static org.slf4j.Logger getLog() {
		return log;
	}
	public static void setLog(org.slf4j.Logger log) {
		ServerBean.log = log;
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
	public void setUri(String uri) {
		this.uri = uri;
	}
	public String getUri() {
		return uri;
	}
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}
	public String getProtocol() {
		return protocol;
	}

    /**
     * @return the fullUrl
     */
    public String getFullUrl() {
        return fullUrl;
    }

    /**
     * @param fullUrl the fullUrl to set
     */
    public void setFullUrl(String fullUrl) {
        this.fullUrl = fullUrl;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }
	
}
