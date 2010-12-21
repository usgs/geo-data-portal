package gov.usgs.cida.gdp.coreprocessing.analysis.grid;

import java.io.File;

import java.io.IOException;
import java.util.Formatter;

import org.junit.Before;
import org.junit.Test;

import ucar.nc2.constants.FeatureType;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.dt.grid.GridDataset;
import ucar.nc2.ft.FeatureDataset;
import ucar.nc2.ft.FeatureDatasetFactoryManager;

import static gov.usgs.cida.gdp.coreprocessing.GridCellHelper.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

public class GridCellGeometryTest {
	
	private GridCellGeometry gcg = null;
	private GridCoordSystem gcs = null;
	
	@Before
	public void setUp() throws IOException {
		String datasetUrl = getResourceDir() + File.separator + "testSimpleYXGrid.ncml";
		assignGridCellGeometryAndGridCoordSystem(datasetUrl);
	}
	
	@Test
	public void testGeometryCellCount() {
		int cellCount = X_SIZE * Y_SIZE;
		assertEquals("The geometry should have equal cell count with dataset", gcg.getCellCount(), cellCount);
	}
	
	@Test
	public void testGeometryCellCountX() {
		assertEquals("The geometry should have equal X to dataset", gcg.getCellCountX(), X_SIZE);
	}
	
	@Test
	public void testGeometryCellCountY() {
		assertEquals("The geometry should have equal Y to dataset", gcg.getCellCountY(), Y_SIZE);
	}
	
	@Test
	public void testGridCoordSystem() {
		assertEquals("The geometry gcs should match dataset's", gcg.getGridCoordSystem(), gcs);
	}
	
	@Test
	public void testGeometryIndexOOB() {
		try {
            gcg.getCellGeometry(X_SIZE, 0);
			fail("expected IndexOutOfBoundsException on X index = X_SIZE");
        } catch (IndexOutOfBoundsException e) { }
		try {
            gcg.getCellGeometry(-1, 0);
			fail("expected IndexOutOfBoundsException on X index = -1");
        } catch (IndexOutOfBoundsException e) { }
		try {
            gcg.getCellGeometry(0, Y_SIZE);
			fail("expected IndexOutOfBoundsException on Y index = Y_SIZE");
        } catch (IndexOutOfBoundsException e) { }

		try {
            gcg.getCellGeometry(0, -1);
			fail("expected IndexOutOfBoundsException on Y index = -1");
        } catch (IndexOutOfBoundsException e) { }
	}
	
	@Test
	public void testGetGeometryProper() {
		assertNotNull("Geometry should not be null", gcg.getCellGeometry(0, 0));
	}
	
	@Test
	public void testProjectionGeometry() throws IOException {
		String datasetUrl = getResourceDir() + File.separator + "testProjectedTYXGrid.ncml";
		assignGridCellGeometryAndGridCoordSystem(datasetUrl);
		assertNotNull("Geometry should not be null", gcg.getCellGeometry(0, 0));
	}
	
	@Test
	public void testRotatedGeometry() throws IOException {
		String datasetUrl = getResourceDir() + File.separator + "testRotatedTYXGrid.ncml";
		assignGridCellGeometryAndGridCoordSystem(datasetUrl);
		assertNotNull("Geometry should not be null", gcg.getCellGeometry(0, 0));
	}
	
	private void assignGridCellGeometryAndGridCoordSystem(String datasetUrl) throws IOException {
		FeatureDataset fd = FeatureDatasetFactoryManager.open(FeatureType.GRID, datasetUrl, null, new Formatter(System.err));
		if (fd == null) fail("Feature dataset didn't return anything");
		GridDataset dataset = (GridDataset)fd;
		GridDatatype gdt = dataset.findGridDatatype(GridTypeTest.DATATYPE_RH);
		gcs = gdt.getCoordinateSystem();
		gcg = new GridCellGeometry(gcs);
	}
}
