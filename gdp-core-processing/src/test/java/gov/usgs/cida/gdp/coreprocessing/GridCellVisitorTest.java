package gov.usgs.cida.gdp.coreprocessing;

import gov.usgs.cida.gdp.coreprocessing.analysis.grid.GridCellTraverser;
import gov.usgs.cida.gdp.coreprocessing.analysis.grid.GridCellTraverser;
import static gov.usgs.cida.gdp.coreprocessing.GridCellHelper.*;
import static org.junit.Assert.assertEquals;

import gov.usgs.cida.gdp.utilities.FileHelper;

import java.io.IOException;
import java.util.Formatter;

import org.junit.Test;

import ucar.nc2.dt.GridDatatype;
import ucar.nc2.dt.grid.GridDataset;
import ucar.nc2.ft.FeatureDataset;
import ucar.nc2.ft.FeatureDatasetFactoryManager;

public class GridCellVisitorTest {

	@Test
	public void testTZYXVisitor() throws IOException {
		String datasetUrl = getResourceDir() + FileHelper.getSeparator() + "testSimpleTZYXGrid.ncml";
		FeatureDataset fd = FeatureDatasetFactoryManager.open(null, datasetUrl, null, new Formatter(System.err));
		GridDataset dataset = (GridDataset)fd;
		GridDatatype gdt = dataset.findGridDatatype(GridTypeTest.DATATYPE_RH);
		GridCellTraverser gct = new GridCellTraverser(gdt);
		GridCellVisitorInheritMock gcvim = new GridCellVisitorInheritMock();
		gct.traverse(gcvim);
		int cells = X_SIZE * Y_SIZE * T_SIZE * Z_SIZE;
		assertEquals("Should count all the cells", gcvim.getProcessCount(), cells);
		
	}
}
