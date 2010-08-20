package gov.usgs.cida.gdp.webapp.servlet;

import gov.usgs.cida.gdp.utilities.XmlUtils;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;

import gov.usgs.cida.gdp.utilities.bean.AckBean;
import gov.usgs.cida.gdp.utilities.bean.ErrorBean;
import gov.usgs.cida.gdp.utilities.bean.XmlReplyBean;
import java.util.Iterator;

/**
 * Servlet implementation class RouterServlet
 */
public class RouterServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static org.apache.log4j.Logger log = Logger.getLogger(RouterServlet.class);

    private static final Map<String, String> servletCommandMappings = new HashMap<String, String>();
    static {
    	servletCommandMappings.put("getoutputstats", "/FileProcessServlet");
    	servletCommandMappings.put("listfiles", "/FileSelectionServlet");
    	servletCommandMappings.put("listattributes", "/FileSelectionServlet");
    	servletCommandMappings.put("listfeatures", "/FileFeatureServlet");
    	servletCommandMappings.put("listservers", "/THREDDSCheckServlet");
    	servletCommandMappings.put("checkserver", "/THREDDSCheckServlet");
    	servletCommandMappings.put("createdatastore", "/GeoServerServlet");
    	servletCommandMappings.put("createuserdirectory", "/UploadFilesServlet");
    	servletCommandMappings.put("getdatafileselectables", "/GeoServerServlet");
    	servletCommandMappings.put("createcoloredmap", "/GeoServerServlet");
    	servletCommandMappings.put("getdatasetlist", "/THREDDSServlet");
    	servletCommandMappings.put("getgridlist", "/THREDDSServlet");
    	servletCommandMappings.put("getcatalog", "/THREDDSServlet");
    	servletCommandMappings.put("calculatewcscoverageinfo", "/WCSServlet");
    	servletCommandMappings.put("gettimerange", "/THREDDSServlet");
    	servletCommandMappings.put("submitforprocessing", "/FileProcessServlet");
    	servletCommandMappings.put("getoutputtypelist", "/OutputTypeServlet");
    	servletCommandMappings.put("getfile", "/FileProcessServlet");
    	servletCommandMappings.put("checkuploadfile", "/FileProcessServlet");
    	servletCommandMappings.put("commandlist", "/SummaryServlet");
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Long start = Long.valueOf(new Date().getTime());
        @SuppressWarnings("unchecked")
        Map<String, String> requestParameters = request.getParameterMap();

        // For debugging purposes
        Iterator<String> keys = requestParameters.keySet().iterator();
        log.debug("Submitted KVP: ( K : V ) ");
        while (keys.hasNext()) {
            String key = keys.next();
            String command = request.getParameter(key);
            if (command != null) log.debug(key + " : " + command);
        }

        // If the user is attempting to upload files, send them directly to the correct servlet
        if (ServletFileUpload.isMultipartContent(request)) {
            log.info("User is attempting to upload files.");
            RequestDispatcher rd = request.getRequestDispatcher("/UploadFilesServlet?command=upload");
            rd.forward(request, response);
            return;
        }

        // Must go after above isMultiPartContent block since that block doesn't contain a command parameter
        if (!requestParameters.containsKey("command")) {
            log.info("User did not send command");
            ErrorBean errorBean = new ErrorBean(ErrorBean.ERR_NO_COMMAND);
            XmlReplyBean xmlReply = new XmlReplyBean(AckBean.ACK_FAIL, errorBean);
            XmlUtils.sendXml(xmlReply, start, response);
            return;
        }

        String command = request.getParameter("command");

        String forwardToServlet = servletCommandMappings.get(command);
        if (forwardToServlet != null) {
        	log.info(command);
        	RequestDispatcher rd = request.getRequestDispatcher(forwardToServlet);
        	rd.forward(request, response);
        } else {
        	log.info("No such command");
        	ErrorBean errorBean = new ErrorBean(ErrorBean.ERR_NO_COMMAND);
            XmlReplyBean xmlReply = new XmlReplyBean(AckBean.ACK_FAIL, errorBean);

            XmlUtils.sendXml(xmlReply, start, response);
            
        	return;
        }
    }

}
