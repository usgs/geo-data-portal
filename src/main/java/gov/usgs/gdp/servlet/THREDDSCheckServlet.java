package gov.usgs.gdp.servlet;



import gov.usgs.gdp.bean.THREDDSInfoBean;
import gov.usgs.gdp.bean.THREDDSServerBean;
import gov.usgs.gdp.helper.THREDDSServerHelper;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.log4j.Logger;

/**
 * Servlet implementation class THREDDSCheckServlet
 */
public class THREDDSCheckServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static org.apache.log4j.Logger log	= Logger.getLogger(THREDDSCheckServlet.class);
	private Timer	timer;
	private static final long FIVE_MINUTES 			= 1000 * 60 * 5; 		// Run every 5 minutes
    /**
     * @see HttpServlet#HttpServlet()
     */
    public THREDDSCheckServlet() {
        super();        
    }
    
	@Override
	public void init(ServletConfig paramConfig) throws ServletException {		
		super.init(paramConfig);
		this.timer = new Timer(true);

		this.timer.scheduleAtFixedRate(new testTHREDDSServers(paramConfig), 0, FIVE_MINUTES);
		log.info("Email Users Task: Initialized");
		
	}	

	class testTHREDDSServers extends TimerTask {
		private ServletConfig paramConfig;
		
		public testTHREDDSServers(ServletConfig paramConfig) {
			super();
			setParamConfig(paramConfig);
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public void run() {
			Map<String, THREDDSServerBean> threddsServerBeanMap = (Map<String, THREDDSServerBean>) paramConfig.getServletContext().getAttribute("threddsServerBeanMap");
			if (threddsServerBeanMap == null) {
				threddsServerBeanMap = getTHREDDSServerBeanMap();
			}
			
			threddsServerBeanMap = checkServers(threddsServerBeanMap);
			paramConfig.getServletContext().setAttribute("threddsServerBeanMap", threddsServerBeanMap);
		}

		private Map<String, THREDDSServerBean> checkServers(Map<String, THREDDSServerBean> threddsServerBeanMap) {
			Map<String, THREDDSServerBean> result = new TreeMap<String, THREDDSServerBean>();
			
			Set<String> threddsServerBeanMapKeySet = threddsServerBeanMap.keySet();
			Iterator<String> threddsServerBeanMapKeySetIterator = threddsServerBeanMapKeySet.iterator();
			
			while (threddsServerBeanMapKeySetIterator.hasNext()) {
				String key = threddsServerBeanMapKeySetIterator.next();
				THREDDSServerBean threddsServerBean = threddsServerBeanMap.get(key);
				threddsServerBean.setLastCheck(new Date());
				String host = threddsServerBean.getHostname();
				int port = threddsServerBean.getPort();
				int timeout = 5000;
				
				boolean serverIsUp = false;
				try {
					serverIsUp = THREDDSServerHelper.isServerReachable(host, port, timeout);
				} catch (UnknownHostException e) {
					log.debug(e.getMessage());
				} catch (IOException e) {
					log.debug(e.getMessage());
				}
				threddsServerBean.setActive(serverIsUp);
				result.put(key, threddsServerBean);
			}
			
			return result;
		}

		private Map<String, THREDDSServerBean> getTHREDDSServerBeanMap() {
			Map<String, THREDDSServerBean> result = new TreeMap<String, THREDDSServerBean>();
			
			Map <String, String> threddsUrlMap = THREDDSInfoBean.getTHREDDSUrlMap();
			Set<String> threddsUrlMapKeySet = threddsUrlMap.keySet();
			Iterator<String> threddsUrlMapKeySetIterator = threddsUrlMapKeySet.iterator();
			while (threddsUrlMapKeySetIterator.hasNext()) {
				String key = threddsUrlMapKeySetIterator.next();
				String serverUrl = threddsUrlMap.get(key);
				THREDDSServerBean threddsServerBean = new THREDDSServerBean();
				
				int startAt = 0;
				if (serverUrl.contains("http:")) startAt = 7;
				else startAt = 8;
				
				String hostname = "";
				boolean hasPort = true;
				try {
					hostname = serverUrl.substring(startAt, serverUrl.indexOf(':', startAt));
				} catch (StringIndexOutOfBoundsException e) {
					// Has no port 
					hostname = serverUrl.substring(startAt, serverUrl.indexOf('/', startAt));
					hasPort = false;
				}
				
				String port = "80";
				if (hasPort) {
					startAt = hostname.length() + startAt;
					port = serverUrl.substring(startAt + 1, serverUrl.indexOf("/", startAt));
				}
				
				threddsServerBean.setHostname(hostname);
				threddsServerBean.setPort(Integer.parseInt(port));
				result.put(key, threddsServerBean);
			}
			
			return result;
		}

		public void setThreddsServerBeanMap(
				Map<String, THREDDSServerBean> threddsServerBeanMap) {
		}

		public ServletConfig getParamConfig() {
			return paramConfig;
		}

		public void setParamConfig(ServletConfig paramConfig) {
			this.paramConfig = paramConfig;
		} 
		
		
	}
}
