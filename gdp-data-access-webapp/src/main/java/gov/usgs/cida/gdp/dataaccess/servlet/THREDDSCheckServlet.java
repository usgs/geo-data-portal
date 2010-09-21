package gov.usgs.cida.gdp.dataaccess.servlet;


import gov.usgs.cida.gdp.dataaccess.bean.Server;
import gov.usgs.cida.gdp.dataaccess.bean.ServerList;
import gov.usgs.cida.gdp.dataaccess.helper.THREDDSServerHelper;
import gov.usgs.cida.gdp.dataaccess.helper.TestTHREDDSServers;
import gov.usgs.cida.gdp.utilities.XmlUtils;
import gov.usgs.cida.gdp.utilities.bean.Acknowledgement;
import gov.usgs.cida.gdp.utilities.bean.XmlReply;
import gov.usgs.cida.gdp.utilities.bean.Error;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.slf4j.LoggerFactory;

/**
 * Servlet implementation class THREDDSCheckServlet
 */
public class THREDDSCheckServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    static org.slf4j.Logger log = LoggerFactory.getLogger(THREDDSCheckServlet.class);
    private Timer timer;
    private static final long FIVE_MINUTES = 1000 * 60 * 5; 		// Run every 5 minutes

    @Override
    public void init(ServletConfig paramConfig) throws ServletException {
        super.init(paramConfig);
        this.timer = new Timer(true);
        this.timer.scheduleAtFixedRate(new TestTHREDDSServers(paramConfig), 0, FIVE_MINUTES);

    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doPost(request, response);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Long start = Long.valueOf(new Date().getTime());
        String command = request.getParameter("command");
        XmlReply xmlReply = null;

        if ("checkserver".equals(command)) {
            String url = request.getParameter("url");
            
            URL urlObject = null;
            try {
        	 urlObject = new URL(URLDecoder.decode(url, "UTF-8"));
            } catch (MalformedURLException e) {
                xmlReply = new XmlReply(Acknowledgement.ACK_FAIL, new Error(Error.ERR_INVALID_URL));
                XmlUtils.sendXml(xmlReply, start, response);
                return;
            }
            
            String hostname = urlObject.getHost();
            int port = urlObject.getPort();
            if (port == -1) port = 80;
            
            try {
                boolean isServerUp = THREDDSServerHelper.isServerReachable(hostname, port, 5000);
                Server tsb = new Server();
                tsb.setHostname(hostname);
                tsb.setPort(port);
                tsb.setActive(isServerUp);
                tsb.setLastCheck(new Date());
                xmlReply = new XmlReply(Acknowledgement.ACK_OK, tsb);
                XmlUtils.sendXml(xmlReply, start, response);
            } catch (IOException e) {
                xmlReply = new XmlReply(Acknowledgement.ACK_FAIL, new Error(Error.ERR_ERROR_WHILE_CONNECTING));
                XmlUtils.sendXml(xmlReply, start, response);
                return;
            }


        }
        if ("listservers".equals(command)) {
            log.debug("User is attempting to retrieve a list of THREDDS servers");

            Map<String, Server> threddsServerBeanMap = (Map<String, Server>) this.getServletContext().getAttribute("threddsServerBeanMap");

            if (threddsServerBeanMap != null) {
                try {
                    Collection<Server> threddsServerBeanCollection = threddsServerBeanMap.values();
                    List<Server> threddsServerBeanList = new ArrayList<Server>();
                    threddsServerBeanList.addAll(threddsServerBeanCollection);

                    // Best naming scheme ever.
                    ServerList threddsServerBeanListBean = new ServerList(threddsServerBeanList);
                    xmlReply = new XmlReply(Acknowledgement.ACK_OK, threddsServerBeanListBean);
                    XmlUtils.sendXml(xmlReply, start, response);
                } catch (IOException e) {
                    log.error(e.getMessage());
                }
                return;
            }
        }
    }


}
