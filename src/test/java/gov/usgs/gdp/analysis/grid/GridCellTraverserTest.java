package gov.usgs.gdp.analysis.grid;

import static gov.usgs.gdp.analysis.grid.GridCellHelper.*;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Formatter;

import org.junit.BeforeClass;
import org.junit.Test;

import ucar.nc2.dt.GridDatatype;
import ucar.nc2.dt.grid.GridDataset;
import ucar.nc2.ft.FeatureDataset;
import ucar.nc2.ft.FeatureDatasetFactoryManager;

public class GridCellTraverserTest {

	@BeforeClass
	public static void setUpAll() {
		setupResourceDir();
	}
	@Test
	public void testYXTraversal() throws IOException {
		String datasetUrl = RESOURCE_PATH + "testSimpleYXGrid.ncml";
		FeatureDataset fd = FeatureDatasetFactoryManager.open(null, datasetUrl, null, new Formatter(System.err));
		GridDataset dataset = (GridDataset)fd;
		GridDatatype gdt = dataset.findGridDatatype(GridTypeTest.DATATYPE_RH);
		GridCellTraverserHelper gct = new GridCellTraverserHelper(gdt);
		assertEquals("X cell count should match ncml", gct.getXCellCount(), X_SIZE);
		assertEquals("Y cell count should match ncml", gct.getYCellCount(), Y_SIZE);
		assertEquals("Z cell count should match ncml", gct.getZCellCount(), 0);
		assertEquals("T cell count should match ncml", gct.getTCellCount(), 0);
	}
	
	@Test
	public void testZYXTraversal() throws IOException {
		String datasetUrl = RESOURCE_PATH + "testSimpleZYXGrid.ncml";
		FeatureDataset fd = FeatureDatasetFactoryManager.open(null, datasetUrl, null, new Formatter(System.err));
		GridDataset dataset = (GridDataset)fd;
		GridDatatype gdt = dataset.findGridDatatype(GridTypeTest.DATATYPE_RH);
		GridCellTraverserHelper gct = new GridCellTraverserHelper(gdt);
		assertEquals("X cell count should match ncml", gct.getXCellCount(), X_SIZE);
		assertEquals("Y cell count should match ncml", gct.getYCellCount(), Y_SIZE);
		assertEquals("Z cell count should match ncml", gct.getZCellCount(), Z_SIZE);
		assertEquals("T cell count should match ncml", gct.getTCellCount(), 0);
	}
	
	@Test
	public void testTYXTraversal() throws IOException {
		String datasetUrl = RESOURCE_PATH + "testSimpleTYXGrid.ncml";
		FeatureDataset fd = FeatureDatasetFactoryManager.open(null, datasetUrl, null, new Formatter(System.err));
		GridDataset dataset = (GridDataset)fd;
		GridDatatype gdt = dataset.findGridDatatype(GridTypeTest.DATATYPE_RH);
		GridCellTraverserHelper gct = new GridCellTraverserHelper(gdt);
		assertEquals("X cell count should match ncml", gct.getXCellCount(), X_SIZE);
		assertEquals("Y cell count should match ncml", gct.getYCellCount(), Y_SIZE);
		assertEquals("Z cell count should match ncml", gct.getZCellCount(), 0);
		assertEquals("T cell count should match ncml", gct.getTCellCount(), T_SIZE);
	}
	
	@Test
	public void testTZYXTraversal() throws IOException {
		String datasetUrl = RESOURCE_PATH + "testSimpleTZYXGrid.ncml";
		FeatureDataset fd = FeatureDatasetFactoryManager.open(null, datasetUrl, null, new Formatter(System.err));
		GridDataset dataset = (GridDataset)fd;
		GridDatatype gdt = dataset.findGridDatatype(GridTypeTest.DATATYPE_RH);
		GridCellTraverserHelper gct = new GridCellTraverserHelper(gdt);
		assertEquals("X cell count should match ncml", gct.getXCellCount(), X_SIZE);
		assertEquals("Y cell count should match ncml", gct.getYCellCount(), Y_SIZE);
		assertEquals("Z cell count should match ncml", gct.getZCellCount(), Z_SIZE);
		assertEquals("T cell count should match ncml", gct.getTCellCount(), T_SIZE);
	}
}
