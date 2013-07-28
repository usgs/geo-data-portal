/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usgs.cida.gdp.dataaccess.bean;

import gov.usgs.cida.gdp.dataaccess.bean.DataTypeCollection.DataTypeBean;
import gov.usgs.cida.gdp.dataaccess.cache.ResponseCache;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;
import ucar.nc2.Variable;
import ucar.nc2.VariableSimpleIF;
import ucar.nc2.dt.VariableSimpleSubclass;

/**
 *
 * @author isuftin
 */
public class DataTypeCollectionTest {

	public DataTypeCollectionTest() {
	}

	@BeforeClass
	public static void setUpClass() {
	}

	@AfterClass
	public static void tearDownClass() {
	}

	@Before
	public void setUp() {
	}

	@After
	public void tearDown() {
	}

	@Test
	public void testCreateEmptyDataTypeCollection() {
		DataTypeCollection result = new DataTypeCollection();
		assertNotNull(result);
	}

	@Test
	public void testCreateDataTypeCollectionWithEmptyDataTypeBean() {
		DataTypeBean dtb = new DataTypeBean();
		DataTypeCollection result = new DataTypeCollection("test-type", dtb);
		assertNotNull(result);
	}

	@Test
	public void testWriteToCache() {
		DataTypeBean dtb = new DataTypeBean();
		DataTypeCollection inputBean = null;
		inputBean = new DataTypeCollection("test-type", dtb);

		ResponseCache.CacheIdentifier cacheId = null;
		try {
			cacheId = new ResponseCache.CacheIdentifier("http://not-a-r-eal-url.gov", ResponseCache.CacheIdentifier.CacheType.DATA_TYPE, "Dummy cache data");
			boolean didWrite = inputBean.writeToCache(cacheId);
			assertTrue(didWrite);
		} finally {
			if (cacheId != null) {
				try {
					FileUtils.deleteQuietly(cacheId.getFile());
				} catch (IOException ex) {
					// Not handled
				}
			}
		}
	}

	@Test
	public void testBuildFromCache() {
		DataTypeBean dtb = new DataTypeBean();
		DataTypeCollection inputBean = null;
		inputBean = new DataTypeCollection("test-type", dtb);
		inputBean.getDataTypeCollection().get(0).setDescription("TEST DESCRIPTION");
		inputBean.getDataTypeCollection().get(0).setName("TEST NAME");
		inputBean.getDataTypeCollection().get(0).setShape(new int[]{1,2,3,4});
		inputBean.getDataTypeCollection().get(0).setRank(1024);
		inputBean.getDataTypeCollection().get(0).setShortname("SHORT NAME");
		inputBean.getDataTypeCollection().get(0).setUnitsstring("UNITS STRING");
		ResponseCache.CacheIdentifier cacheId = null;
		try {
			cacheId = new ResponseCache.CacheIdentifier("http://not-a-r-eal-url.gov", ResponseCache.CacheIdentifier.CacheType.DATA_TYPE, "Dummy cache data");
			boolean didWrite = inputBean.writeToCache(cacheId);
			assertTrue(didWrite);
			DataTypeCollection resultBean = DataTypeCollection.buildFromCache(cacheId);
			assertNotNull(resultBean);
			
			assertEquals(resultBean.getDataType(), "test-type");
			assertEquals(resultBean.getDataTypeCollection().get(0).getDescription(), "TEST DESCRIPTION");
			assertEquals(resultBean.getDataTypeCollection().get(0).getName(), "TEST NAME");
			assertEquals(resultBean.getDataTypeCollection().get(0).getShortname(), "SHORT NAME");
			assertEquals(resultBean.getDataTypeCollection().get(0).getUnitsstring(), "UNITS STRING");
			assertEquals(resultBean.getDataTypeCollection().get(0).getShape()[0], 1);
			assertEquals(resultBean.getDataTypeCollection().get(0).getShape()[1], 2);
			assertEquals(resultBean.getDataTypeCollection().get(0).getShape()[2], 3);
			assertEquals(resultBean.getDataTypeCollection().get(0).getShape()[3], 4);
			assertEquals(resultBean.getDataTypeCollection().get(0).getRank(), 1024);
		} finally {
			if (cacheId != null) {
				try {
					FileUtils.deleteQuietly(cacheId.getFile());
				} catch (IOException ex) {
					// Not handled
				}
			}
		}
	}
}