package gov.usgs.cida.gdp.geometry;

import gov.usgs.cida.gdp.utilities.HTTPUtils;

import com.vividsolutions.jts.geom.GeometryCollection;
import gov.usgs.cida.gdp.constants.AppConstant;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import javax.xml.parsers.ParserConfigurationException;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.geotools.data.shapefile.shp.ShapeType;
import org.geotools.data.shapefile.shp.ShapefileWriter;
import org.xml.sax.SAXException;

/**
 * @author razoerb
 */
public class WatersService {

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
        GeometryCollection g = parseGML(gml);

        // Write to a shapefile so GeoServer can load the geometry. As of 2.0.2,
        // Geoserver (GeoTools) GML datastores are unsupported. Hence the
        // GML -> Shapefile conversion.
        String tempDir = AppConstant.NEW_SHAPEFILE_LOCATION.toString();

        File shpFile = new File(tempDir, name + ".shp");
        File shxFile = new File(tempDir, name + ".shx");

        if (shpFile.exists()) shpFile.delete();
        if (shxFile.exists()) shxFile.delete();

        // Make sure all parent directories exist
        shpFile.getParentFile().mkdirs();

        shpFile.createNewFile();
        shxFile.createNewFile();

        FileOutputStream shpFileInputStream = new FileOutputStream(shpFile);
        FileOutputStream shxFileInputStream = new FileOutputStream(shxFile);

        ShapefileWriter sw = new ShapefileWriter(shpFileInputStream.getChannel(),
                shxFileInputStream.getChannel());
        
        sw.write(g, ShapeType.POLYGON);

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

    private static GeometryCollection parseGML(String gml)
            throws IOException, SAXException, ParserConfigurationException {

        if (gml == null) {
            throw new IllegalArgumentException();
        }

        //create the parser with the gml 2.0 configuration
        org.geotools.xml.Configuration configuration = new org.geotools.gml2.GMLConfiguration();
        org.geotools.xml.Parser parser = new org.geotools.xml.Parser(configuration);


        // TODO: parse fails with when gml is only a single polygon with "Authority "SDO" is unknown".
        //parse
        GeometryCollection geom = (GeometryCollection) parser.parse(new StringReader(gml));

        return geom;
    }
}
