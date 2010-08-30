/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usgs.cida.gdp.geometry;

import com.vividsolutions.jts.geom.Geometry;
import gov.usgs.cida.gdp.utilities.HTTPUtils;

import com.vividsolutions.jts.geom.GeometryCollection;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.parsers.ParserConfigurationException;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.geotools.data.shapefile.shp.ShapeType;
import org.geotools.data.shapefile.shp.ShapefileWriter;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.xml.sax.SAXException;

/**
 *
 * @author admin
 */
public class NHDService {

    public static FeatureCollection<SimpleFeatureType, SimpleFeature> getGeometry(
            String lon, String lat, String shapefilePath, String shxFilePath)
            throws MalformedURLException, IOException, SchemaException,
            NoSuchAuthorityCodeException, FactoryException {

        FeatureCollection<SimpleFeatureType, SimpleFeature> featureCollection = null;
        // Get reachcode containing lat/lon point from the EPA WATERS web service
        InputStream reachJson =
                HTTPUtils.sendPacket(new URL("http://iaspub.epa.gov/waters10/waters_services.PointIndexingService?"
                + "pGeometry=POINT(" + lon + "%20" + lat + ")" + "&pGeometryMod=WKT,SRID=8307"
                + "&pPointIndexingMethod=RAINDROP" + "&pPointIndexingRaindropDist=25"), "GET");

        String reachCode = parseJSON(reachJson, "reachcode");

        // Get geometry of reachcode
        InputStream json =
                HTTPUtils.sendPacket(new URL("http://iaspub.epa.gov/waters10/waters_services.navigationDelineationService?"
                + "pNavigationType=UT&pStartReachCode=" + reachCode + "&optOutGeomFormat=GEOGML&pFeatureType=CATCHMENT_TOPO&pMaxDistance=999999999"),
                "GET");

        String gml = parseJSON(json, "shape");


        try {
            GeometryCollection g = parseGML(gml);

            // Write to a shapefile so GeoServer can load the geometry
            File shpFile = new File(shapefilePath);
            File shxFile = new File(shxFilePath);

            if (shpFile.exists()) {
                shpFile.delete();
            }
            if (shxFile.exists()) {
                shxFile.delete();
            }

            shpFile.createNewFile();
            shxFile.createNewFile();

            FileOutputStream shpFileInputStream = new FileOutputStream(shpFile);
            FileOutputStream shxFileInputStream = new FileOutputStream(shxFile);
            ShapefileWriter sw = new ShapefileWriter(shpFileInputStream.getChannel(),
                    shxFileInputStream.getChannel());
            sw.write(g, ShapeType.POLYGON);

            featureCollection = createFeatureCollection(g);

            return featureCollection;

        } catch (SAXException e) {
            e.printStackTrace();
            return null;
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String parseJSON(InputStream json, String element)
            throws JsonParseException, IOException {

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
            throws SchemaException, IOException, SAXException, ParserConfigurationException {

        if (gml == null) {
            throw new IOException();
        }

        //create the parser with the gml 2.0 configuration
        org.geotools.xml.Configuration configuration = new org.geotools.gml2.GMLConfiguration();
        org.geotools.xml.Parser parser = new org.geotools.xml.Parser(configuration);


        // TODO: parse fails with when gml is only a single polygon with "Authority "SDO" is unknown".
        //parse
        GeometryCollection geom = (GeometryCollection) parser.parse(new StringReader(gml));

        return geom;
    }

    private static FeatureCollection<SimpleFeatureType, SimpleFeature> createFeatureCollection(GeometryCollection geom)
            throws NoSuchAuthorityCodeException, FactoryException {

        FeatureCollection<SimpleFeatureType, SimpleFeature> fc = FeatureCollections.newCollection();

        for (int i = 0; i < geom.getNumGeometries(); i++) {
            Geometry g = geom.getGeometryN(i);

            SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
            typeBuilder.setName("testType");
            typeBuilder.setCRS(CRS.decode("EPSG:4326"));
            typeBuilder.add("blah", Integer.class);
            typeBuilder.add("geom", Geometry.class);
            typeBuilder.setDefaultGeometry("geom");

            SimpleFeatureType type = typeBuilder.buildFeatureType();


//			GeometryFactory geomFactory = new GeometryFactory();
            SimpleFeatureBuilder build = new SimpleFeatureBuilder(type);

//			for (Coordinate c : g.getCoordinates()) {
//				System.out.println(c.x + ", " + c.y);
//				build.add( geomFactory.createPoint( c ));
//			}

            build.set("geom", g);
            build.set("blah", i);

            SimpleFeature sf = build.buildFeature(null);
            sf.getBounds();

//			SimpleFeature sf = SimpleFeatureBuilder.build(type, g.getCoordinates(), null);

            fc.add(sf);
        }

        return fc;
    }
}
