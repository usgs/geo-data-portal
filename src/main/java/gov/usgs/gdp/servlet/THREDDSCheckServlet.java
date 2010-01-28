package gov.usgs.gdp.servlet;



import gov.usgs.gdp.bean.AckBean;
import gov.usgs.gdp.bean.AttributeBean;
import gov.usgs.gdp.bean.ErrorBean;
import gov.usgs.gdp.bean.THREDDSInfoBean;
import gov.usgs.gdp.bean.THREDDSServerBean;
import gov.usgs.gdp.bean.THREDDSServerBeanList;
import gov.usgs.gdp.bean.XmlReplyBean;
import gov.usgs.gdp.helper.THREDDSServerHelper;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Servlet implementation class THREDDSCheckServlet
 */
public class THREDDSCheckServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	static org.apache.log4j.Logger log	= Logger.getLogger(THREDDSCheckServlet.class);
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

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException{
		doPost(request, response);
	}
	
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException{
		String command = request.getParameter("command");
		XmlReplyBean xmlReply = null;
		
		if ("checkserver".equals(command)) {
			String hostname = request.getParameter("hostname");
			String portString = request.getParameter("port");
			int port = 80;
			try {
				if (portString != null && !"".equals(portString) && !"null".equals(portString)) {
					port = Integer.parseInt(portString);
				}
			} catch (NumberFormatException e) {
				xmlReply = new XmlReplyBean(AckBean.ACK_FAIL, new ErrorBean("Port provided is not a number"));
				RouterServlet.sendXml(xmlReply, response);
				return;
			}
			log.debug("User is attempting to get server status: " + hostname + ":" + port);
			try {
				boolean isServerUp = THREDDSServerHelper.isServerReachable(hostname, port, 5000);
				THREDDSServerBean tsb = new THREDDSServerBean();
				tsb.setActive(isServerUp);
				tsb.setHostname(hostname);
				tsb.setPort(port);
				tsb.setLastCheck(new Date());
				xmlReply = new XmlReplyBean(AckBean.ACK_OK, tsb);
				RouterServlet.sendXml(xmlReply, response);
			} catch (IOException e) {
				xmlReply = new XmlReplyBean(AckBean.ACK_FAIL, new ErrorBean(ErrorBean.ERR_ERROR_WHILE_CONNECTING));
				RouterServlet.sendXml(xmlReply, response);
				return;
			}
		}
		
		if ("listthredds".equals(command)) {
			log.debug("User is attempting to retrieve a list of THREDDS servers");
			
			Map<String, THREDDSServerBean> threddsServerBeanMap = (Map<String, THREDDSServerBean>) this.getServletContext().getAttribute("threddsServerBeanMap");
			
			if (threddsServerBeanMap != null) {
				try {
					Collection<THREDDSServerBean> threddsServerBeanCollection = threddsServerBeanMap.values();
					List<THREDDSServerBean> threddsServerBeanList = new ArrayList<THREDDSServerBean>();
					threddsServerBeanList.addAll(threddsServerBeanCollection);
					
					// Best naming scheme ever.
					THREDDSServerBeanList threddsServerBeanListBean = new THREDDSServerBeanList(threddsServerBeanList);
					xmlReply = new XmlReplyBean(AckBean.ACK_OK, threddsServerBeanListBean);
					RouterServlet.sendXml(xmlReply, response);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return;
			}
		}
	}
	
	class testTHREDDSServers extends TimerTask {
		private ServletConfig paramConfig;
		
		public testTHREDDSServers(ServletConfig paramConfig) {
			super();
			setParamConfig(paramConfig);
		}
		
		@Override
		public void run() {
			Map<String, THREDDSServerBean> threddsServerBeanMap = (Map<String, THREDDSServerBean>) paramConfig.getServletContext().getAttribute("threddsServerBeanMap");
			if (threddsServerBeanMap == null) {
				threddsServerBeanMap = getTHREDDSServerBeanMap();
			}
			
			threddsServerBeanMap = checkServers(threddsServerBeanMap);
			this.paramConfig.getServletContext().setAttribute("threddsServerBeanMap", threddsServerBeanMap);
		}

		/**
		 * Check the Map of servers to see if they're up or down
		 * 
		 * @param threddsServerBeanMap
		 * @return
		 */
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
					log.debug("Host " + host + ":" + port + " could not be reached. Reason: " + e.getMessage() + "\n\tBeing labeled as down. Will re-check in 5 minutes.");
				} catch (IOException e) {
					log.debug("Host " + host + ":" + port + " could not be reached. Reason: " + e.getMessage() + "\n\tBeing labeled as down. Will re-check in 5 minutes.");
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
