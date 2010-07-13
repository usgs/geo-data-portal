package gov.usgs.gdp.analysis.grid;

import java.io.IOException;
import java.util.Formatter;

import org.junit.BeforeClass;
import org.junit.Test;

import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.dt.grid.GridDataset;
import ucar.nc2.ft.FeatureDataset;
import ucar.nc2.ft.FeatureDatasetFactoryManager;

import static gov.usgs.gdp.analysis.grid.GridCellHelper.*;
import static org.junit.Assert.assertEquals;

public class GridCellGeometryTest {
	
	@BeforeClass
	public static void setUpAll() {
		setupResourceDir();
	}
	
	@Test
	public void testGeometryCellCount() throws IOException {
		String datasetUrl = RESOURCE_PATH + "testSimpleYXGrid.ncml";
		FeatureDataset fd = FeatureDatasetFactoryManager.open(null, datasetUrl, null, new Formatter(System.err));
		GridDataset dataset = (GridDataset)fd;
		GridDatatype gdt = dataset.findGridDatatype(GridTypeTest.DATATYPE_RH);
		GridCoordSystem gcs = gdt.getCoordinateSystem();
		GridCellGeometry gcg = new GridCellGeometry(gcs);
		int cellCount = X_SIZE * Y_SIZE;
		assertEquals("The geometry", gcg.getCellCount(), cellCount);
	}
}
