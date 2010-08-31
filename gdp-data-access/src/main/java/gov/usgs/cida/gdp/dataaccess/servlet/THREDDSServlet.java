package gov.usgs.cida.gdp.dataaccess.servlet;

import gov.usgs.cida.gdp.dataaccess.helper.THREDDSServerHelper;
import gov.usgs.cida.gdp.utilities.XmlUtils;
import gov.usgs.cida.gdp.utilities.bean.Acknowledgement;
import gov.usgs.cida.gdp.utilities.bean.Error;
import gov.usgs.cida.gdp.utilities.bean.Time;
import gov.usgs.cida.gdp.utilities.bean.XmlReply;
import gov.usgs.cida.gdp.utilities.bean.XmlResponse;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.slf4j.LoggerFactory;

/**
 * Servlet implementation class THREDDSServlet
 */
public class THREDDSServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static org.slf4j.Logger log = LoggerFactory.getLogger(THREDDSServlet.class);

    /**
     * @see HttpServlet#HttpServlet()
     */
    public THREDDSServlet() {
        super();
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
        
        String command = request.getParameter("command");
        XmlReply xmlReply = null;

        if ("getcatalog".equals(command)) {
        	URL urlObject = null;
        	try {
        		urlObject = new URL(URLDecoder.decode(request.getParameter("url"), "UTF-8"));
        	}catch (MalformedURLException e) {
                xmlReply = new XmlReply(Acknowledgement.ACK_FAIL, new Error(Error.ERR_INVALID_URL));
                XmlUtils.sendXml(xmlReply, start, response);
                return;
            }

            HttpClient client = new HttpClient();
            HttpMethod method = new GetMethod(urlObject.toString());
            
            try {
                int statusCode = client.executeMethod(method);

                if (statusCode != HttpStatus.SC_OK) {
                    System.err.println("Method failed: " + method.getStatusLine());
                }
                // Read the response body.
                byte[] responseBody = method.getResponseBody();

                // Deal with the response.
                // Use caution: ensure correct character encoding and is not binary data
                String xmlResponse = new String(responseBody);
                method.releaseConnection();
                XmlUtils.sendXml(xmlResponse, start, response);
                return;
            } catch (HttpException e) {
                xmlReply = new XmlReply(Acknowledgement.ACK_FAIL, new Error(Error.ERR_PROTOCOL_VIOLATION));
                XmlUtils.sendXml(xmlReply, start, response);
                return;
            } catch (IOException e) {
                xmlReply = new XmlReply(Acknowledgement.ACK_FAIL, new Error(Error.ERR_TRANSPORT_ERROR));
                XmlUtils.sendXml(xmlReply, start, response);
                return;
            } finally {
                // Release the connection.
                method.releaseConnection();
            }
        }

        if ("getgridlist".equals(command)) {
            log.debug("User has chosen to list shapefile attributes");

            // Grab what we need to work with for this request
            String datasetUrl = request.getParameter("dataseturl");
            if (datasetUrl == null || "".equals(datasetUrl)) {
                xmlReply = new XmlReply(Acknowledgement.ACK_FAIL, new Error(Error.ERR_MISSING_PARAM));
                XmlUtils.sendXml(xmlReply, start, response);
                return;
            }

            List<XmlResponse> gridBeanList = null;
            try {
                gridBeanList = THREDDSServerHelper.getGridBeanListFromServer(datasetUrl);
            } catch (IOException e) {
                xmlReply = new XmlReply(Acknowledgement.ACK_FAIL, new Error(Error.ERR_INVALID_URL));
                XmlUtils.sendXml(xmlReply, start, response);
                return;
            }
            XmlReply xrb = new XmlReply(Acknowledgement.ACK_OK, gridBeanList);
            XmlUtils.sendXml(xrb, start, response);
            return;
        }

        if ("gettimerange".equals(command)) {
            String datasetUrl = request.getParameter("dataseturl");
            
            // FIXME: properly handle multiple datatypes
            String[] gridSelections = request.getParameterValues("grid[]");
            String gridSelection = gridSelections[0];
            /////////////////////////////////////////////
            
            Time timeBean = null;
            try {
                timeBean = THREDDSServerHelper.getTimeBean(datasetUrl, gridSelection);
            } catch (ParseException e) {
                xmlReply = new XmlReply(Acknowledgement.ACK_FAIL, new Error(Error.ERR_MISSING_TIMERANGE));
                XmlUtils.sendXml(xmlReply, start, response);
                return;
            }

            if (timeBean == null) {
                xmlReply = new XmlReply(Acknowledgement.ACK_FAIL, new Error(Error.ERR_MISSING_TIMERANGE));
                XmlUtils.sendXml(xmlReply, start, response);
                return;
            }



            XmlReply xrb = new XmlReply(Acknowledgement.ACK_OK, timeBean);
            XmlUtils.sendXml(xrb, start, response);
            return;
        }

    }
}
