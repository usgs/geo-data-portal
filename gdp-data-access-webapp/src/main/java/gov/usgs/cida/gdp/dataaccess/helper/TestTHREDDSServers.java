/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usgs.cida.gdp.dataaccess.helper;

import gov.usgs.cida.gdp.dataaccess.bean.Server;
import gov.usgs.cida.gdp.dataaccess.bean.THREDDSInfo;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TimerTask;
import java.util.TreeMap;
import javax.servlet.ServletConfig;
import org.slf4j.LoggerFactory;
 
public final class TestTHREDDSServers extends TimerTask {

    static org.slf4j.Logger log = LoggerFactory.getLogger(THREDDSServerHelper.class);
    private ServletConfig paramConfig;

    public TestTHREDDSServers(ServletConfig paramConfig) {
        super();
        setParamConfig(paramConfig);
    }

    @Override
    public void run() {
        Map<String, Server> threddsServerBeanMap = (Map<String, Server>) this.paramConfig.getServletContext().getAttribute("threddsServerBeanMap");
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
    private Map<String, Server> checkServers(Map<String, Server> threddsServerBeanMap) {
        Map<String, Server> result = new TreeMap<String, Server>();

        Set<String> threddsServerBeanMapKeySet = threddsServerBeanMap.keySet();
        Iterator<String> threddsServerBeanMapKeySetIterator = threddsServerBeanMapKeySet.iterator();

        while (threddsServerBeanMapKeySetIterator.hasNext()) {
            String key = threddsServerBeanMapKeySetIterator.next();
            Server threddsServerBean = threddsServerBeanMap.get(key);
            threddsServerBean.setLastCheck(new Date());

            String serverURL = threddsServerBean.getFullUrl();

            boolean serverIsUp = THREDDSServerHelper.isServerReachable(serverURL);
            if (!serverIsUp) log.debug("Server " + serverURL + " could not be reached.\n"
                    + "\tBeing labeled as down. Will re-check in 5 minutes.");
            
            threddsServerBean.setActive(serverIsUp);
            result.put(key, threddsServerBean);
        }
        return result;
    }

    private Map<String, Server> getTHREDDSServerBeanMap() {
        Map<String, Server> result = new TreeMap<String, Server>();

        Map<String, String> threddsUrlMap = THREDDSInfo.getTHREDDSUrlMap();
        Set<String> threddsUrlMapKeySet = threddsUrlMap.keySet();
        Iterator<String> threddsUrlMapKeySetIterator = threddsUrlMapKeySet.iterator();
        while (threddsUrlMapKeySetIterator.hasNext()) {
            String key = threddsUrlMapKeySetIterator.next();
            String name = key;
            String serverUrl = threddsUrlMap.get(key);
            String protocol;
            Server threddsServerBean = new Server();

            int startAt = 0;
            if (serverUrl.contains("http:")) {
                startAt = 7;
                protocol = "http://";
            } else {
                startAt = 8;
                protocol = "https://";
            }

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
            startAt = hostname.length() + startAt;
            if (hasPort) {
                port = serverUrl.substring(startAt + 1, serverUrl.indexOf("/", startAt));
                startAt = startAt + port.length() + 1;
            }

            String uri = "";

            uri = serverUrl.substring(startAt);
            threddsServerBean.setName(name);
            threddsServerBean.setUri(uri);
            threddsServerBean.setProtocol(protocol);
            threddsServerBean.setHostname(hostname);
            threddsServerBean.setPort(Integer.parseInt(port));
            threddsServerBean.setFullUrl(serverUrl);
            result.put(key, threddsServerBean);
        }

        return result;
    }

    public ServletConfig getParamConfig() {
        return this.paramConfig;
    }

    public void setParamConfig(@SuppressWarnings("hiding") ServletConfig paramConfig) {
        this.paramConfig = paramConfig;
    }
}
