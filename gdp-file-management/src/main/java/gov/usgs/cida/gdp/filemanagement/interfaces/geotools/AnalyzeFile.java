package gov.usgs.cida.gdp.filemanagement.interfaces.geotools;

import gov.usgs.cida.gdp.filemanagement.GeoToolsFileAnalysis;
import gov.usgs.cida.gdp.utilities.bean.ShapeFileSet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AnalyzeFile {
	
	/**
	 * Summarizes a DBF, SHP or PRJ file 
	 * 
	 * @param file
	 * @return
	 * @throws IOException 
	 */
	public static List<String> getFileSummary(File file) throws IOException {
		List<String> result = new ArrayList<String>();
		if (!file.exists() 
				|| file.isDirectory() 
				|| file.length() == 0) return result;
		
		if (file.getName().toLowerCase().contains(".dbf")) {
			result = GeoToolsFileAnalysis.getDBaseFileSummary(file);
		} 
		
		if (file.getName().toLowerCase().contains(".shp")) {
			result = GeoToolsFileAnalysis.getShapeFileHeaderSummary(file);
			result.addAll(GeoToolsFileAnalysis.getShapeFileSummary(file));
		}
		
		return result;
	}
	
	/**
	 * Summarizes files in a ShapeFileSet file set
	 * @param shapeFileSetBean
	 * @return
	 * @throws IOException 
	 */
	public static List<String> getFileSummary(ShapeFileSet shapeFileSetBean) throws IOException {
		List<String> result = new ArrayList<String>();
		File dbFile = shapeFileSetBean.getDbfFile();
		File shpFile = shapeFileSetBean.getShapeFile();
		
		if (dbFile != null && dbFile.exists() && dbFile.length() != 0) {
			result.add("DBF:\n");
			result.addAll(GeoToolsFileAnalysis.getDBaseFileSummary(dbFile));
		}
		
		if (shpFile != null && shpFile.exists() && shpFile.length() != 0) {
			result.add("\nSHP:\n");
			result.add("HEADER:\n");
			result.addAll(GeoToolsFileAnalysis.getShapeFileHeaderSummary(shapeFileSetBean.getShapeFile()));
			result.add("SUMMARY:\n");
			result.addAll(GeoToolsFileAnalysis.getShapeFileSummary(shapeFileSetBean.getShapeFile()));
		}
		return result;
	}
	
	public static List<String> getFileSummary(String file) throws IOException {
		return AnalyzeFile.getFileSummary(new File(file));
	}
}
