package gov.usgs.gdp.interfaces.geotools;

import gov.usgs.gdp.analysis.GeoToolsFileAnalysis;
import gov.usgs.gdp.bean.ShapeFileSetBean;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AnalyzeFile {
	
	/**
	 * Summarizes a DBF, SHP or PRJ file 
	 * 
	 * @param file
	 * @return
	 */
	public static List<String> getFileSummary(File file) {
		List<String> result = new ArrayList<String>();
		if (!file.exists() 
				|| file.isDirectory() 
				|| file.length() == 0) return result;
		
		if (file.getName().toLowerCase().contains(".dbf")) {
			result = GeoToolsFileAnalysis.getDBaseFileSummary(file);
		} else if (file.getName().toLowerCase().contains(".shp")) {
			result = GeoToolsFileAnalysis.getShapeFileHeaderSummary(file);
			result.addAll(GeoToolsFileAnalysis.getShapeFileSummary(file));
		}
		return result;
	}
	
	/**
	 * Summarizes files in a ShapeFileSetBean file set
	 * @param shapeFileSetBean
	 * @return
	 */
	public static List<String> getFileSummary(ShapeFileSetBean shapeFileSetBean) {
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
	
	public static List<String> getFileSummary(String file) {
		return AnalyzeFile.getFileSummary(new File(file));
	}
}
