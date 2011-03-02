package gov.usgs.cida.gdp.dataaccess;

import com.vividsolutions.jts.geom.Geometry;
import gov.usgs.cida.gdp.utilities.HTTPUtils;

import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import gov.usgs.cida.gdp.constants.AppConstant;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.UUID;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.xerces.parsers.DOMParser;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.geotools.data.shapefile.dbf.DbaseFileHeader;
import org.geotools.data.shapefile.dbf.DbaseFileWriter;
import org.geotools.data.shapefile.shp.ShapeType;
import org.geotools.data.shapefile.shp.ShapefileWriter;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author razoerb
 */
public class WatersService {

    static org.slf4j.Logger log = LoggerFactory.getLogger(WatersService.class);

    static final String multiPolyWrap =
        "<gml:MultiPolygon xmlns:gml=\"http://www.opengis.net/gml\" srsName=\"SDO:8265\">" +
          "<gml:polygonMember>" +
          "</gml:polygonMember>" +
        "</gml:MultiPolygon>";

    public static File getGeometry(String lon, String lat, String name) throws Exception {
        
        // Get reachcode containing lat/lon point from the EPA WATERS web service
        InputStream reachJson =
                HTTPUtils.sendPacket(new URL("http://iaspub.epa.gov/waters10/"
                + "waters_services.PointIndexingService?"
                + "pGeometry=POINT(" + lon + "%20" + lat + ")"
                + "&pGeometryMod=WKT,SRID=8307" + "&pPointIndexingMethod=RAINDROP"
                + "&pPointIndexingRaindropDist=25"), "GET");

        String reachCode = parseJSON(reachJson, "reachcode");

        // Get geometry of reachcode
        InputStream json =
                HTTPUtils.sendPacket(new URL("http://iaspub.epa.gov/waters10/"
                + "waters_services.navigationDelineationService?"
                + "pNavigationType=UT&pStartReachCode=" + reachCode
                + "&optOutGeomFormat=GEOGML&pFeatureType=CATCHMENT_TOPO"
                + "&pMaxDistance=999999999"), "GET");

        String gml = parseJSON(json, "shape");
        Geometry geom = parseGML(gml);

        // Write to a shapefile so GeoServer can load the geometry. As of 2.0.2,
        // Geoserver (GeoTools) GML datastores are unsupported. Hence the
        // GML -> Shapefile conversion.
        String tempDir = AppConstant.SHAPEFILE_LOCATION.getValue() + File.separator + UUID.randomUUID();;

        File shpFile = new File(tempDir, name + ".shp");
        File shxFile = new File(tempDir, name + ".shx");
        File dbfFile = new File(tempDir, name + ".dbf");

        // Make sure all parent directories exist
        shpFile.getParentFile().mkdirs();

        if (shpFile.exists()) shpFile.delete();
        if (shxFile.exists()) shxFile.delete();
        if (dbfFile.exists()) dbfFile.delete();

        shpFile.createNewFile();
        shxFile.createNewFile();
        dbfFile.createNewFile();

        FileOutputStream shpFileOutputStream = new FileOutputStream(shpFile);
        FileOutputStream shxFileOutputStream = new FileOutputStream(shxFile);
        FileOutputStream dbfFileOutputStream = new FileOutputStream(dbfFile);

        int numFeatures = geom.getNumGeometries();

        // The WATERS service returns one big polygon, potentially along with a
        // couple smaller polygons (3-4 vertices). We only care about the big
        // polygon, so find which index it's at in the collection.
        int maxNumPoints = -1;
        int maxGeomIndex = -1;
        for (int i = 0; i < numFeatures; i++) {
            int numPoints = geom.getGeometryN(i).getNumPoints();

            if (numPoints > maxNumPoints) {
                maxNumPoints = numPoints;
                maxGeomIndex = i;
            }
        }

        Geometry bigPoly = geom.getGeometryN(maxGeomIndex);

        GeometryFactory factory = geom.getFactory();
        GeometryCollection bigPolyColl =
                new GeometryCollection(new Geometry[] { bigPoly }, factory);

        // Write dbf file with simple placeholder attribute
        DbaseFileHeader header = new DbaseFileHeader();
        header.addColumn("ID", 'N', 4, 0);
        header.setNumRecords(1);

        DbaseFileWriter dfw = new DbaseFileWriter(header, dbfFileOutputStream.getChannel());
        dfw.write(new Object[] { 0 });
        dfw.close();

        // Write geometry
        ShapefileWriter sw = new ShapefileWriter(shpFileOutputStream.getChannel(),
                shxFileOutputStream.getChannel());
        
        sw.write(bigPolyColl, ShapeType.POLYGON);
        sw.close();

        return shpFile;
    }

    private static String parseJSON(InputStream json, String element)
            throws JsonParseException, IOException {

        if (json == null) {
            throw new IllegalArgumentException();
        }

        JsonFactory f = new JsonFactory();
        JsonParser jp = f.createJsonParser(new InputStreamReader(json));

        while (true) {
            jp.nextToken();

            if (!jp.hasCurrentToken()) {
                break;
            }

            if (element.equals(jp.getCurrentName())) {
                jp.nextToken();
                return jp.getText();
            }
        }

        return null;
    }

    private static Geometry parseGML(String gml)
            throws IOException, SAXException, ParserConfigurationException {

        if (gml == null) {
            throw new IllegalArgumentException();
        }

        gml = convertToMultiPolygon(gml);

        //create the parser with the gml 2.0 configuration
        org.geotools.xml.Configuration configuration = new org.geotools.gml2.GMLConfiguration();
        org.geotools.xml.Parser parser = new org.geotools.xml.Parser(configuration);

        Geometry geom = (Geometry) parser.parse(new StringReader(gml));

        return geom;
    }

    // This is a hack to get around (what appears to be) a GeoTools GML parsing
    // bug. If the GML has a MultiPolygon as the root element, GeoTools has no
    // problem parsing it. However, if the root element is a Polygon, parsing
    // fails. This method wraps a bare Polygon with the multiPolyWrap XML stub.
    // NOTE: this is dependent on the format of the WATERS response, and could
    // break if that format changes.
    static String convertToMultiPolygon(String gml) throws IOException, SAXException {
        DOMParser gmlParser = new DOMParser();

        gmlParser.parse(new InputSource(new StringReader(gml)));

        Document gmlDoc = gmlParser.getDocument();
        Node root = gmlDoc.getFirstChild();

        if (root.getNodeName().equals("gml:Polygon")) {
            DOMParser wrapParser = new DOMParser();
            wrapParser.parse(new InputSource(new StringReader(multiPolyWrap)));
            Document wrapDoc = wrapParser.getDocument();

            // Remove attributes of Polygon (will be attrs of parent MultiPolygon instead)
            NamedNodeMap attrs = root.getAttributes();
            attrs.removeNamedItem("xmlns:gml");
            attrs.removeNamedItem("srsName");

            Node polyNode = wrapDoc.importNode(root, true);
            Node polyMembersNode = wrapDoc.getFirstChild().getFirstChild();
            polyMembersNode.appendChild(polyNode);


            DOMImplementationLS domLS =(DOMImplementationLS)(wrapDoc.getImplementation()).
                   getFeature("LS","3.0");

            StringWriter sw = new StringWriter();

            LSOutput lsOut = domLS.createLSOutput();
            lsOut.setCharacterStream(sw);

            LSSerializer serializer = domLS.createLSSerializer();
            serializer.write(wrapDoc, lsOut);

            return sw.toString();
        } else {
            return gml;
        }
    }
}
