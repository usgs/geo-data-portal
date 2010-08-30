package gov.usgs.cida.gdp.coreprocessing;

import gov.usgs.cida.gdp.coreprocessing.analysis.grid.GridCellTraverser;
import gov.usgs.cida.gdp.coreprocessing.analysis.grid.GridCellTraverser;
import static org.junit.Assert.assertEquals;
import static gov.usgs.cida.gdp.coreprocessing.GridCellHelper.*;
import gov.usgs.cida.gdp.utilities.FileHelper;

import java.io.IOException;
import java.util.Formatter;

import org.junit.Before;
import org.junit.Test;

import ucar.nc2.dt.GridDatatype;
import ucar.nc2.dt.grid.GridDataset;
import ucar.nc2.ft.FeatureDataset;
import ucar.nc2.ft.FeatureDatasetFactoryManager;

public class TYXGridCellVisitorTest {
	
	private GridCellVisitorMock gcvm = null;
	
	@Before
	public void setUp() throws IOException {
		String datasetUrl = getResourceDir() + FileHelper.getSeparator() + "testSimpleTYXGrid.ncml";
		FeatureDataset fd = FeatureDatasetFactoryManager.open(null, datasetUrl, null, new Formatter(System.err));
		GridDataset dataset = (GridDataset)fd;
		GridDatatype gdt = dataset.findGridDatatype(GridTypeTest.DATATYPE_RH);
		GridCellTraverser gct = new GridCellTraverser(gdt);
		gcvm = new GridCellVisitorMock();
	    gct.traverse(gcvm);
	}

	@Test
	public void testVisitorMockProcessCount() throws IOException {
		int cells = T_SIZE * Y_SIZE * X_SIZE;
	    assertEquals("The number of cells processed should be reported accurately", gcvm.getProcessCount(), cells);
	}
	
	@Test
	public void testVisitorMockYXCellCount() {
		int cells = T_SIZE;
		assertEquals("The number of YX cells should be reported accurately", gcvm.getYXCount(), cells);
	}
	
	@Test
	public void testVisitorMockZCellCount() {
		int cells = 0;
		assertEquals("The number of Z cells should be reported accurately", gcvm.getZCount(), cells);
	}
	
	@Test
	public void testVisitorMockTCellCount() {
		int cells = T_SIZE;
		assertEquals("The number of T cells should be reported accurately", gcvm.getTCount(), cells);
	}
	
	@Test
	public void testVisitorMockTraverseCount() {
		assertEquals("The number of Traversals? should be reported accurately", gcvm.getTraverseCount(), 1);
	}
}
