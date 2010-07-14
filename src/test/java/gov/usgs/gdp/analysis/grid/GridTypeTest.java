package gov.usgs.gdp.analysis.grid;

import static org.junit.Assert.fail;
import static gov.usgs.gdp.analysis.grid.GridCellHelper.*;

import java.io.IOException;
import java.util.Formatter;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.dt.grid.GridDataset;
import ucar.nc2.ft.FeatureDataset;
import ucar.nc2.ft.FeatureDatasetFactoryManager;

/**
 * @author jordan
 *
 */
public class GridTypeTest {

	public static final String DATATYPE_RH = "rh";
	
	@BeforeClass
	public static void setUpAll() {
		setupResourceDir();
	}
	
	@Test
	public void testSimpleTYXGrid() throws IOException {
		String datasetUrl = RESOURCE_PATH + "testSimpleTYXGrid.ncml";
		if (getGridType(datasetUrl, DATATYPE_RH) != GridType.TYX) {
			fail("Should be TYX dataset.");
		}
	}
	
	@Test
	public void testSimpleTZYXGrid() throws IOException {
		String datasetUrl = RESOURCE_PATH + "testSimpleTZYXGrid.ncml";
		if (getGridType(datasetUrl, DATATYPE_RH) != GridType.TZYX) {
			fail("Should be TZYX dataset.");
		}
	}
	
	@Test
	public void testSimpleYXGrid() throws IOException {
		String datasetUrl = RESOURCE_PATH + "testSimpleYXGrid.ncml";
		if (getGridType(datasetUrl, DATATYPE_RH) != GridType.YX) {
			fail("Should be YX dataset.");
		}
	}
	
	@Test
	public void testSimpleZYXGrid() throws IOException {
		String datasetUrl = RESOURCE_PATH + "testSimpleZYXGrid.ncml";
		if (getGridType(datasetUrl, DATATYPE_RH) != GridType.ZYX) {
			fail("Should be ZYX dataset");
		}
	}
	
	/**
	 * Basically the OTHER is tricky to do.  So right now it isn't working.
	 * I don't know if my ncml is bad, or netCDF java can't recognize it 
	 * @throws IOException
	 */
	@Test
	@Ignore // throws null pointer exception // cannot find dataset
	public void testSimpleOtherGrid() throws IOException {
		String datasetUrl = RESOURCE_PATH + "testSimpleOtherGrid.ncml";
		GridType gt = getGridType(datasetUrl, DATATYPE_RH);
		if (gt != GridType.OTHER) {
			fail("Should be OTHER dataset not " + gt.toString());
		}
	}
	
	public static GridType getGridType(String filename, String type) throws IOException {
		FeatureDataset fd = FeatureDatasetFactoryManager.open(null, filename, null, new Formatter(System.err));
		GridDataset dataset = (GridDataset)fd;
		GridDatatype gdt = dataset.findGridDatatype(type);
		GridCoordSystem gcs = gdt.getCoordinateSystem();
		return GridType.findGridType(gcs);
	}
}