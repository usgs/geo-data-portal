package gov.usgs.gdp.analysis.grid;

import gov.usgs.gdp.helper.FileHelper;

import java.net.URL;

public class GridCellHelper {

	public static final int T_SIZE = 2;
	public static final int Z_SIZE = 5;
	public static final int Y_SIZE = 3;
	public static final int X_SIZE = 4;
	public static String RESOURCE_PATH = "";

	public static void setupResourceDir() {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
        URL sampleFileLocation = cl.getResource("Sample_files" + FileHelper.getSeparator());
        RESOURCE_PATH = sampleFileLocation.getPath();
	}
}