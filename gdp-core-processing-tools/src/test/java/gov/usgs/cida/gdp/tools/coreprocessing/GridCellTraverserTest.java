package gov.usgs.cida.gdp.tools.coreprocessing;

import static gov.usgs.cida.gdp.tools.coreprocessing.GridCellHelper.*;
import static org.junit.Assert.assertEquals;

import gov.usgs.cida.gdp.utilities.FileHelper;

import java.io.IOException;
import java.util.Formatter;

import org.junit.Test;

import ucar.nc2.dt.GridDatatype;
import ucar.nc2.dt.grid.GridDataset;
import ucar.nc2.ft.FeatureDataset;
import ucar.nc2.ft.FeatureDatasetFactoryManager;

public class GridCellTraverserTest {

	@Test
	public void testYXTraversal() throws IOException {
		String datasetUrl = getResourceDir() + FileHelper.getSeparator() + "testSimpleYXGrid.ncml";
		GridCellTraverserHelper gct = getGridCellTraverserHelper(datasetUrl);
		assertEquals("X cell count should match ncml", gct.getXCellCount(), X_SIZE);
		assertEquals("Y cell count should match ncml", gct.getYCellCount(), Y_SIZE);
		assertEquals("Z cell count should match ncml", gct.getZCellCount(), 0);
		assertEquals("T cell count should match ncml", gct.getTCellCount(), 0);
	}
	
	@Test
	public void testZYXTraversal() throws IOException {
		String datasetUrl = getResourceDir() + FileHelper.getSeparator() + "testSimpleZYXGrid.ncml";
		GridCellTraverserHelper gct = getGridCellTraverserHelper(datasetUrl);
		assertEquals("X cell count should match ncml", gct.getXCellCount(), X_SIZE);
		assertEquals("Y cell count should match ncml", gct.getYCellCount(), Y_SIZE);
		assertEquals("Z cell count should match ncml", gct.getZCellCount(), Z_SIZE);
		assertEquals("T cell count should match ncml", gct.getTCellCount(), 0);
	}
	
	@Test
	public void testTYXTraversal() throws IOException {
		String datasetUrl = getResourceDir() + FileHelper.getSeparator() + "testSimpleTYXGrid.ncml";
		GridCellTraverserHelper gct = getGridCellTraverserHelper(datasetUrl);
		assertEquals("X cell count should match ncml", gct.getXCellCount(), X_SIZE);
		assertEquals("Y cell count should match ncml", gct.getYCellCount(), Y_SIZE);
		assertEquals("Z cell count should match ncml", gct.getZCellCount(), 0);
		assertEquals("T cell count should match ncml", gct.getTCellCount(), T_SIZE);
	}
	
	@Test
	public void testTZYXTraversal() throws IOException {
		String datasetUrl = getResourceDir() + FileHelper.getSeparator() + "testSimpleTZYXGrid.ncml";
		GridCellTraverserHelper gct = getGridCellTraverserHelper(datasetUrl);
		assertEquals("X cell count should match ncml", gct.getXCellCount(), X_SIZE);
		assertEquals("Y cell count should match ncml", gct.getYCellCount(), Y_SIZE);
		assertEquals("Z cell count should match ncml", gct.getZCellCount(), Z_SIZE);
		assertEquals("T cell count should match ncml", gct.getTCellCount(), T_SIZE);
	}
	
	private GridCellTraverserHelper getGridCellTraverserHelper(String datasetUrl) throws IOException {
		FeatureDataset fd = FeatureDatasetFactoryManager.open(null, datasetUrl, null, new Formatter(System.err));
		GridDataset dataset = (GridDataset)fd;
		GridDatatype gdt = dataset.findGridDatatype(GridTypeTest.DATATYPE_RH);
		return new GridCellTraverserHelper(gdt);
	}
}
