/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usgs.cida.gdp.io.data;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.UUID;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author isuftin
 */
public class ZippedGenericFileDataTest {
    private File multiDirZip = null;
    private FileInputStream fis = null;
    private File tempArea = null;
    public ZippedGenericFileDataTest() {
    }

    @Before
    public  void beforeTest() throws Exception {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        URL sampleFileLocation = cl.getResource("Sample_Zips/testShapes_multidir.zip");
        multiDirZip = new File(sampleFileLocation.toURI());
        fis = new FileInputStream(multiDirZip);
        tempArea = new File(System.getProperty("java.io.tmpdir") + File.separator + UUID.randomUUID().toString());
        FileUtils.forceMkdir(tempArea);
    }

    @After
    public  void afterTest() throws Exception {
        IOUtils.closeQuietly(fis);
        FileUtils.deleteQuietly(tempArea);
    }

    @Test
    public void testZippedGenericFileData() {
        ZippedGenericFileData test = new ZippedGenericFileData(fis, "application/x-zipped-shp");
        test.writeData(tempArea);
        assertFalse(FileUtils.listFiles(tempArea, null, true).isEmpty());
        assertTrue(FileUtils.listFiles(tempArea, null, true).size() == 6);
    }
}
