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
        servletCommandMappings.put("getoutputstats", "http://localhost:8080/gdp-webapp/OutputInfoServlet");
        servletCommandMappings.put("listfiles", "http://localhost:8080/gdp-file-management/FileSelectionServlet");
        servletCommandMappings.put("listattributes", "http://localhost:8080/gdp-file-management/FileSelectionServlet");
        servletCommandMappings.put("listfeatures", "http://localhost:8080/gdp-file-management/FileFeatureServlet");
        servletCommandMappings.put("listservers", "http://localhost:8080/gdp-data-access/THREDDSCheckServlet");
        servletCommandMappings.put("checkserver", "http://localhost:8080/gdp-data-access/THREDDSCheckServlet");
        servletCommandMappings.put("createdatastore", "http://localhost:8080/gdp-file-management/GeoServerServlet");
        servletCommandMappings.put("createuserdirectory", "http://localhost:8080/gdp-file-management/ReceiveFileServlet");
        servletCommandMappings.put("getdatafileselectables", "http://localhost:8080/gdp-file-management/GeoServerServlet");
        servletCommandMappings.put("createcoloredmap", "http://localhost:8080/gdp-file-management/GeoServerServlet");
        servletCommandMappings.put("getdatasetlist", "http://localhost:8080/gdp-data-access/THREDDSServlet");
        servletCommandMappings.put("getgridlist", "http://localhost:8080/gdp-data-access/THREDDSServlet");
        servletCommandMappings.put("getcatalog", "http://localhost:8080/gdp-data-access/THREDDSServlet");
        servletCommandMappings.put("calculatewcscoverageinfo", "http://localhost:8080/gdp-data-access/WCSServlet");
        servletCommandMappings.put("gettimerange", "http://localhost:8080/gdp-data-access/THREDDSServlet");
        servletCommandMappings.put("submitforprocessing", "http://localhost:8080/gdp-core-processing/ProcessServlet");
        servletCommandMappings.put("getoutputtypelist", "http://localhost:8080/gdp-webapp/OutputInfoServlet");
        servletCommandMappings.put("getfile", "http://localhost:8080/gdp-file-management/SendFileServlet");
        servletCommandMappings.put("checkuploadfile", "http://localhost:8080/gdp-file-management/SendFileServlet");
        servletCommandMappings.put("commandlist", "http://localhost:8080/gdp-file-management/SummaryServlet");
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
        

        // If the user is attempting to upload files, send them directly to the correct servlet
        if (ServletFileUpload.isMultipartContent(request)) {
            log.info("User is attempting to upload files.");
            RequestDispatcher rd = request.getRequestDispatcher("/UploadFilesServlet?command=upload");
            rd.forward(request, response);
            return;
        }

        


        Map<String, String> requestParameters = request.getParameterMap();

        // For debugging purposes
        Iterator<String> keys = requestParameters.keySet().iterator();
        String command = request.getParameter("command");

        // Must go after above isMultiPartContent block since that block doesn't contain a commandKey parameter
        if (!requestParameters.containsKey("command")) {
            log.info("User did not send command");
            ErrorBean errorBean = new ErrorBean(ErrorBean.ERR_NO_COMMAND);
            XmlReplyBean xmlReply = new XmlReplyBean(AckBean.ACK_FAIL, errorBean);
            XmlUtils.sendXml(xmlReply, start, response);
            return;
        }

        String commandList = "&command=" + command;
        log.debug("Submitted KVP: ( K : V ) ");
        while (keys.hasNext()) {
            String key = keys.next();
            String commandKey = request.getParameter(key);
            if (commandKey != null) log.debug(key + " : " + commandKey);
            commandList += "&" + key + "=" + commandKey;
        }

        String forwardToServlet = servletCommandMappings.get(command);
        if (forwardToServlet != null) {
        	log.info(command);
            response.sendRedirect(forwardToServlet + commandList);
        } else {
        	log.info("No such command");
        	ErrorBean errorBean = new ErrorBean(ErrorBean.ERR_NO_COMMAND);
            XmlReplyBean xmlReply = new XmlReplyBean(AckBean.ACK_FAIL, errorBean);

            XmlUtils.sendXml(xmlReply, start, response);
            
        	return;
        }
    }

}
