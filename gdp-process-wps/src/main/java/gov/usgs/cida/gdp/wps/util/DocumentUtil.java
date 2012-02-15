package gov.usgs.cida.gdp.wps.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.iosp.geotiff.GeoTiffIOServiceProvider;

/**
 *
 * @author tkunicki
 */
public class DocumentUtil {

    private final static DocumentBuilder DOCUMENT_BUILDER;

    static {

        DocumentBuilder documentBuilder = null;
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            System.err.println(e);
        }
        DOCUMENT_BUILDER = documentBuilder;

        try {
            NetcdfFile.registerIOProvider(GeoTiffIOServiceProvider.class);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(WCSUtil.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(WCSUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static Document createDocument(InputStream inputStream) throws SAXException, IOException {
        return DOCUMENT_BUILDER.parse(inputStream);
    }

    public static Document createDocument(InputSource inputSource) throws SAXException, IOException {
        return DOCUMENT_BUILDER.parse(inputSource);
    }

    public static Document createDocument(File inputFile) throws SAXException, IOException {
        return DOCUMENT_BUILDER.parse(inputFile);
    }

    public static Document createDocument(String uri) throws SAXException, IOException {
        return DOCUMENT_BUILDER.parse(uri);
    }
}
