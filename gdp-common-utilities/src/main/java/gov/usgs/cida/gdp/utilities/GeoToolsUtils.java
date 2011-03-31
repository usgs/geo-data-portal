package gov.usgs.cida.gdp.utilities;

import com.vividsolutions.jts.geom.Envelope;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import org.geotools.data.shapefile.dbf.DbaseFileHeader;
import org.geotools.data.shapefile.dbf.DbaseFileWriter;
import org.geotools.data.shapefile.shp.ShapeType;
import org.geotools.data.shapefile.shp.ShapefileWriter;
import org.slf4j.LoggerFactory;

/**
 *
 * @author admin
 */
public class GeoToolsUtils {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(GeoToolsUtils.class);

    public static File createEmptyShapefile(String path, String name) throws IOException {
        File shpFile = new File(path, name + ".shp");
        File shxFile = new File(path, name + ".shx");
        File dbfFile = new File(path, name + ".dbf");

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


        // Write dbf file with single column, values will be added over WFS-T
        DbaseFileHeader header = new DbaseFileHeader();
        header.addColumn("ID", 'N', 4, 0);
        header.setNumRecords(0);

        DbaseFileWriter dfw = new DbaseFileWriter(header, dbfFileOutputStream.getChannel());
        dfw.close();

        // Only write headers, geometry will be added over WFS-T
        ShapefileWriter sw = new ShapefileWriter(shpFileOutputStream.getChannel(),
                shxFileOutputStream.getChannel());

        sw.writeHeaders(new Envelope(0, 0, 0, 0), ShapeType.POLYGON, 0, 0);
        
        sw.close();

        return shpFile;
    }
}
