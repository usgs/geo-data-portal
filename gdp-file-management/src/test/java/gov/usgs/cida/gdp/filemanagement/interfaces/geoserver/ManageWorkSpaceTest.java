/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.usgs.cida.gdp.filemanagement.interfaces.geoserver;

import org.junit.Ignore;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author isuftin
 */
public class ManageWorkSpaceTest {

    public ManageWorkSpaceTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of createDataStore method, of class ManageWorkSpace.
     */
    @Test
    public void testCreateDataStore_3args() throws Exception {
    }

    /**
     * Test of createDataStore method, of class ManageWorkSpace.
     */
    @Test
    public void testCreateDataStore_4args() throws Exception {
    }

    /**
     * Test of listDataStores method, of class ManageWorkSpace.
     */
    @Test
    public void testListDataStores_0args() throws Exception {
    }

    /**
     * Test of listDataStores method, of class ManageWorkSpace.
     */
    @Test
    public void testListDataStores_String() throws Exception {
    }

    /**
     * Test of workspaceExists method, of class ManageWorkSpace.
     */
    @Test
    public void testWorkspaceExists() throws Exception {
    }

    /**
     * Test of dataStoreExists method, of class ManageWorkSpace.
     */
    @Test
    public void testDataStoreExists() throws Exception {
    }

    /**
     * Test of styleExists method, of class ManageWorkSpace.
     */
    @Test
    public void testStyleExists() throws Exception {
    }

    /**
     * Test of createColoredMap method, of class ManageWorkSpace.
     */
    @Test
    public void testCreateColoredMap() throws Exception {
    }

    /**
     * Test of getResponse method, of class ManageWorkSpace.
     */
    @Test
    @Ignore
    public void testGetResponse() {
        try {
            ManageWorkSpace mws = new ManageWorkSpace();
            String response = mws.listDataStores("gdp");
            assertNotNull(response);
        } catch (MalformedURLException ex) {
            Logger.getLogger(ManageWorkSpaceTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ManageWorkSpaceTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Test of sendPacket method, of class ManageWorkSpace.
     */
    @Test
    public void testSendPacket() throws Exception {
    }

    /**
     * Test of createWorkspaceXML method, of class ManageWorkSpace.
     */
    @Test
    public void testCreateWorkspaceXML() {
    }

    /**
     * Test of createDataStoreXML method, of class ManageWorkSpace.
     */
    @Test
    public void testCreateDataStoreXML() {
    }

    /**
     * Test of createFeatureTypeXML method, of class ManageWorkSpace.
     */
    @Test
    public void testCreateFeatureTypeXML() {
    }

    /**
     * Test of parseDates method, of class ManageWorkSpace.
     */
    @Test
    public void testParseDates() {
    }

    /**
     * Test of parseCSV method, of class ManageWorkSpace.
     */
    @Test
    public void testParseCSV() {
    }

    /**
     * Test of createStyle method, of class ManageWorkSpace.
     */
    @Test
    public void testCreateStyle() {
    }

}