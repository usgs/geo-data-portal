package gov.usgs.gdp.analysis.grid;

import static gov.usgs.gdp.analysis.grid.GridCellHelper.*;
import static org.junit.Assert.assertEquals;
import gov.usgs.cida.gdp.utilities.FileHelper;

import java.io.IOException;
import java.util.Formatter;

import org.junit.Before;
import org.junit.Test;

import ucar.nc2.dt.GridDatatype;
import ucar.nc2.dt.grid.GridDataset;
import ucar.nc2.ft.FeatureDataset;
import ucar.nc2.ft.FeatureDatasetFactoryManager;

public class YXGridCellVisitorTest {
	
	private GridCellVisitorMock gcvm = null;
	
	@Before
	public void setUp() throws IOException {
		String datasetUrl = getResourceDir() + FileHelper.getSeparator() + "testSimpleYXGrid.ncml";
		FeatureDataset fd = FeatureDatasetFactoryManager.open(null, datasetUrl, null, new Formatter(System.err));
		GridDataset dataset = (GridDataset)fd;
		GridDatatype gdt = dataset.findGridDatatype(GridTypeTest.DATATYPE_RH);
		GridCellTraverser gct = new GridCellTraverser(gdt);
		gcvm = new GridCellVisitorMock();
	    gct.traverse(gcvm);
	}

	@Test
	public void testVisitorMockProcessCount() throws IOException {
		int cells = Y_SIZE * X_SIZE;
	    assertEquals("The number of cells processed should be reported accurately", gcvm.getProcessCount(), cells);
	}
	
	@Test
	public void testVisitorMockYXCellCount() {
		int cells = 1;
		assertEquals("The number of YX cells should be reported accurately", gcvm.getYXCount(), cells);
	}
	
	@Test
	public void testVisitorMockZCellCount() {
		int cells = 0;
		assertEquals("The number of Z cells should be reported accurately", gcvm.getZCount(), cells);
	}
	
	@Test
	public void testVisitorMockTCellCount() {
		int cells = 0;
		assertEquals("The number of T cells should be reported accurately", gcvm.getTCount(), cells);
	}
	
	@Test
	public void testVisitorMockTraverseCount() {
		assertEquals("The number of Traversals? should be reported accurately", gcvm.getTraverseCount(), 1);
	}
}
