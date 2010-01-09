package gov.usgs.gdp.bean;

import gov.usgs.gdp.analysis.GeoToolsFileAnalysis;
import gov.usgs.gdp.io.FileHelper;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.geotools.data.FeatureSource;
import org.geotools.data.FileDataStore;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeType;

public class ShapeFileSetBean implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private File projectionFile;
	private File shapeFile;
	private File dbfFile;
	private String name;
	private List<String> attributeList;
	
	public File getProjectionFile() {
		return projectionFile;
	}
	public void setProjectionFile(File projectionFile) {
		this.projectionFile = projectionFile;
	}
	public File getShapeFile() {
		return shapeFile;
	}
	public void setShapeFile(File shapeFile) {
		this.shapeFile = shapeFile;
	}
	public File getDbfFile() {
		return dbfFile;
	}
	public void setDbfFile(File dbfFile) {
		this.dbfFile = dbfFile;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<String> getAttributeList() {
		if (this.attributeList == null) this.attributeList = new ArrayList<String>();
		return attributeList;
	}
	public void setAttributeList(List<String> attributeList) {
		
		this.attributeList = attributeList;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
	public static List<String> getAttributeListFromBean(ShapeFileSetBean shapeFileSetBean) {
		
		List<String> result = new ArrayList<String>();
		FileDataStore fds = GeoToolsFileAnalysis.getFileDataStore(shapeFileSetBean.getShapeFile());		
		FeatureSource<SimpleFeatureType, SimpleFeature> featureSource;
		
		try {
			featureSource = fds.getFeatureSource();
		} catch (IOException e) {
			return result;
		}
		
		List<AttributeType> attribTypes = featureSource.getSchema().getTypes();
		for (AttributeType attribType : attribTypes) {
            String attribTypeName = attribType.getName().toString();
            String selectItem = attribTypeName;
            result.add(selectItem);
        }
		return result;
		
	}
	
}
