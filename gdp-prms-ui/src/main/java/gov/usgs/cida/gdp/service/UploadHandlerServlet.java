package gov.usgs.cida.gdp.service;

import com.sun.jndi.toolkit.url.UrlUtil;
import gov.usgs.cida.config.DynamicReadOnlyProperties;
import java.io.*;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpUtils;
import org.apache.commons.codec.binary.Base64OutputStream;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;


public class UploadHandlerServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        DynamicReadOnlyProperties props = null;
        try {
            props = new DynamicReadOnlyProperties().addJNDIContexts((String[]) null);
        } catch (NamingException ex) {
            Logger.getLogger(UploadHandlerServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        int maxFileSize = Integer.parseInt(props.getProperty("gdp.prms.ui.file.maxsize"));
        int fileSize = Integer.parseInt(request.getHeader("Content-Length"));
        if (fileSize > maxFileSize) {
            sendErrorResponse(response, "Upload exceeds max file size of " + maxFileSize + " bytes");
            return;
        }

        String filename = request.getParameter("filename");
        String utilityWpsUrl = request.getParameter("utility-wps-url") + "/WebProcessingService?service=WPS&version=1.0.0&request=Execute&identifier=gov.usgs.cida.gdp.wps.algorithm.filemanagement.ReceiveFiles";
        String wfsEndpoint = request.getParameter("wfs-url");
        String tempDir = System.getProperty("java.io.tmpdir");
        File destinationFile = new File(tempDir + File.separator + filename);

        // Handle form-based upload (from IE)
        if (ServletFileUpload.isMultipartContent(request)) {
            FileItemFactory factory = new DiskFileItemFactory();
            ServletFileUpload upload = new ServletFileUpload(factory);

            // Parse the request
            FileItemIterator iter;
            try {
                iter = upload.getItemIterator(request);
                while (iter.hasNext()) {
                    FileItemStream item = iter.next();
                    String name = item.getFieldName();
                    if ("file-path".equals(name)) {
                        saveFileFromRequest(item.openStream(), destinationFile);
                        break;
                    }
                }
            } catch (Exception ex) {
                sendErrorResponse(response, "Unable to upload file");
                return;
            }
        } else {
            // Handle octet streams (from standards browsers)
            try {
                saveFileFromRequest(request.getInputStream(), destinationFile);
            } catch (IOException ex) {
                Logger.getLogger(UploadHandlerServlet.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        String responseText = null;
        try {
            String wpsResponse = postToWPS(utilityWpsUrl, wfsEndpoint, destinationFile);
            
            if (wpsResponse.toLowerCase().contains("exceptionreport")) {
                responseText = "{ 'success' : false, 'msg' : 'Unable to upload file'}";
            } else {
                responseText = "{ 'success' : true, 'msg' : 'Your upload has completed succesfully'}";
            }

        } catch (Exception ex) {
            Logger.getLogger(UploadHandlerServlet.class.getName()).log(Level.SEVERE, null, ex);
            sendErrorResponse(response, "Unable to upload file");
            return;
        } finally {
            FileUtils.deleteQuietly(destinationFile);
        }
        
        sendResponse(response, responseText);
    }

    public static void sendErrorResponse(HttpServletResponse response, String text) {
        
        sendResponse(response, "{ 'success' : false, 'msg' : '" + text + "'}");
    }

    public static void sendResponse(HttpServletResponse response, String text) {
        response.setCharacterEncoding("utf-8");
        response.setContentType("text/html");  
        try {
            Writer writer = response.getWriter();
            writer.write(text);
            writer.close();
        } catch (IOException ex) {
            Logger.getLogger(UploadHandlerServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void saveFileFromRequest(InputStream is, File destinationFile) throws IOException {
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(destinationFile);
            IOUtils.copy(is, os);
        } finally {
            IOUtils.closeQuietly(os);
        }
    }

    private String postToWPS(String url, String wfsEndpoint, File uploadedFile) throws IOException{
        HttpPost post = null;
        HttpClient httpClient = new DefaultHttpClient();
        
        post = new HttpPost(url);

        File wpsRequestFile = createWPSReceiveFilesXML(uploadedFile, wfsEndpoint);
        FileInputStream wpsRequestInputStream = null;
        try {
            wpsRequestInputStream = new FileInputStream(wpsRequestFile);

            AbstractHttpEntity entity = new InputStreamEntity(wpsRequestInputStream, wpsRequestFile.length());
        
            post.setEntity(entity);

            HttpResponse response = httpClient.execute(post);

            return EntityUtils.toString(response.getEntity());

        } finally {
            IOUtils.closeQuietly(wpsRequestInputStream);
            FileUtils.deleteQuietly(wpsRequestFile);
        }
    }

    private static File createWPSReceiveFilesXML(final File uploadedFile, final String wfsEndpoint) throws IOException {

        File wpsRequestFile = null;
        FileOutputStream wpsRequestOutputStream = null;
        FileInputStream uploadedInputStream  = null;

        try {
            wpsRequestFile = File.createTempFile("wps.upload.", ".xml");
            wpsRequestOutputStream = new FileOutputStream(wpsRequestFile);
            uploadedInputStream  = new FileInputStream(uploadedFile);
        
            wpsRequestOutputStream.write(new String(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<wps:Execute service=\"WPS\" version=\"1.0.0\" " +
                        "xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" " +
                        "xmlns:ows=\"http://www.opengis.net/ows/1.1\" " +
                        "xmlns:xlink=\"http://www.w3.org/1999/xlink\" " +
                        "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                        "xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0 " +
                        "http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd\">" +
                    "<ows:Identifier>gov.usgs.cida.gdp.wps.algorithm.filemanagement.ReceiveFiles</ows:Identifier>" +
                    "<wps:DataInputs>" +
                        "<wps:Input>" +
                            "<ows:Identifier>filename</ows:Identifier>" +
                            "<wps:Data>" +
                                "<wps:LiteralData>" +
                                    StringEscapeUtils.escapeXml(uploadedFile.getName().replace(".zip", "")) +
                                "</wps:LiteralData>" +
                            "</wps:Data>" +
                        "</wps:Input>" +
                        "<wps:Input>" +
                            "<ows:Identifier>wfs-url</ows:Identifier>" +
                            "<wps:Data>" +
                                "<wps:LiteralData>" +
                                    StringEscapeUtils.escapeXml(wfsEndpoint) +
                                "</wps:LiteralData>" +
                            "</wps:Data>" +
                        "</wps:Input>" +
                        "<wps:Input>" +
                            "<ows:Identifier>file</ows:Identifier>" +
                            "<wps:Data>" +
                                "<wps:ComplexData mimeType=\"application/x-zipped-shp\" encoding=\"Base64\">").getBytes());
            IOUtils.copy(uploadedInputStream, new Base64OutputStream(wpsRequestOutputStream, true, 0, null));
            wpsRequestOutputStream.write(new String(
                                 "</wps:ComplexData>" +
                            "</wps:Data>" +
                        "</wps:Input>" +
                    "</wps:DataInputs>" +
                    "<wps:ResponseForm>" +
                        "<wps:ResponseDocument>" +
                            "<wps:Output>" +
                                "<ows:Identifier>result</ows:Identifier>" +
                            "</wps:Output>" +
                            "<wps:Output>" +
                                "<ows:Identifier>wfs-url</ows:Identifier>" +
                            "</wps:Output>" +
                            "<wps:Output>" +
                                "<ows:Identifier>featuretype</ows:Identifier>" +
                            "</wps:Output>" +
                        "</wps:ResponseDocument>" +
                    "</wps:ResponseForm>" +
                "</wps:Execute>").getBytes());
        } finally {
            IOUtils.closeQuietly(wpsRequestOutputStream);
            IOUtils.closeQuietly(uploadedInputStream);
        }
        return wpsRequestFile;
    }
}
