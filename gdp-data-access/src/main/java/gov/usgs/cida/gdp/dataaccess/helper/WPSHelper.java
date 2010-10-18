package gov.usgs.cida.gdp.dataaccess.helper;

import gov.usgs.cida.gdp.utilities.FileHelper;
import java.io.File;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author isuftin
 */
public class WPSHelper {
    Logger log = LoggerFactory.getLogger(WPSHelper.class);

    public static String createWPSReceiveFilesXML(final File inputFile, final String WFSUrl, final String filename) throws IOException {
        StringBuilder sb = new StringBuilder();

        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        sb.append("<wps:Execute service=\"WPS\" version=\"1.0.0\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0 http://schemas.opengis.net/wps/1.0.0/wpsExecute_request.xsd\">");
        sb.append("<ows:Identifier>gov.usgs.cida.gdp.wps.algorithm.filemanagement.ReceiveFiles</ows:Identifier>");
        sb.append("<wps:DataInputs>");
        sb.append("<wps:Input>");
        sb.append("<ows:Identifier>wfs-url</ows:Identifier>");
        sb.append("<wps:Data>");
        sb.append("<wps:LiteralData>");
        sb.append(WFSUrl);
        sb.append("</wps:LiteralData>");
        sb.append("</wps:Data>");
        sb.append("</wps:Input>");
        sb.append("<wps:Input>");
        sb.append("<ows:Identifier>filename</ows:Identifier>");
        sb.append("<wps:Data>");
        sb.append("<wps:LiteralData>");
        sb.append(filename);
        sb.append("</wps:LiteralData>");
        sb.append("</wps:Data>");
        sb.append("</wps:Input>");
        sb.append("<wps:Input>");
        sb.append("<ows:Identifier>file</ows:Identifier>");
        sb.append("<wps:Data>");
        sb.append("<wps:ComplexData mimeType=\"application/x-zipped-shp\" encoding=\"UTF-8\">");
//        sb.append("<![CDATA[");
        sb.append(new String(FileHelper.base64Encode(inputFile)));
//        sb.append("]]");
        sb.append("</wps:ComplexData>");
        sb.append("</wps:Data>");
        sb.append("</wps:Input>");
        sb.append("</wps:DataInputs>");
        sb.append("<wps:ResponseForm><wps:ResponseDocument><wps:Output><ows:Identifier>result</ows:Identifier></wps:Output><wps:Output><ows:Identifier>wfs-url</ows:Identifier></wps:Output><wps:Output><ows:Identifier>featuretype</ows:Identifier></wps:Output></wps:ResponseDocument></wps:ResponseForm></wps:Execute>");

        return sb.toString();
    }
}