package gov.usgs.gdp.bean;

import gov.usgs.gdp.helper.FileHelper;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("files")
public class AvailableFilesBean implements XmlBean{
	
	@XStreamAlias("shape-sets")
	private List<ShapeFileSetBean> shapeSetList;
	
	@XStreamAlias("example-files")
	private List<FilesBean> exampleFileList;
	
	@XStreamAlias("user-files")
	private List<FilesBean> userFileList;
	
	@Override
	public String toXml() {
		String result = "";
		XStream xstream = new XStream();
		xstream.processAnnotations(AvailableFilesBean.class);
		StringBuffer sb = new StringBuffer();
		sb.append(xstream.toXML(this));
		result = sb.toString();
		return result;
	}
	
	/**
	 * Create an AvailableFilesBean from a passed base directory. 
	 * 
	 * @param baseDirectory
	 * @param userDirectory
	 * @return
	 */
	static public AvailableFilesBean getAvailableFilesBean(String baseDirectory, String userDirectory) throws IllegalArgumentException {
		if (baseDirectory == null) return null;
		if ("".equals(baseDirectory)) return null;
		
		AvailableFilesBean result = new AvailableFilesBean();
		List<ShapeFileSetBean> shapeFileSetBeanList = new ArrayList<ShapeFileSetBean>();
		List<FilesBean> exampleFileBeanList = new ArrayList<FilesBean>();
		List<FilesBean> userFileBeanList = new ArrayList<FilesBean>();
		String exampleDirectory = baseDirectory 
			+ FileHelper.getSeparator() 
			+ "Sample_Files" 
			+ FileHelper.getSeparator();
		
		// Create the example file bean list
		exampleFileBeanList = FilesBean.getFilesBeanSetList(exampleDirectory, true);
		result.setExampleFileList(exampleFileBeanList);
		
		// Create the user file bean list (if calling method decides)
		if (userDirectory != null && !"".equals(userDirectory)) {
			userFileBeanList = FilesBean.getFilesBeanSetList(userDirectory, false);
			result.setUserFileList(userFileBeanList);
		}
		
		// Begin populating the ShapeFileSetBean list
		for (FilesBean exampleBean : exampleFileBeanList) {
			ShapeFileSetBean sfsb = null;
			if ((sfsb = ShapeFileSetBean.getShapeFileSetBeanFromFilesBean(exampleBean)) != null){
				shapeFileSetBeanList.add(sfsb);
			}
			
		}
		
		// Begin populating the ShapeFileSetBean list
		for (FilesBean userBean : userFileBeanList) {
			ShapeFileSetBean sfsb = null;
			if ((sfsb = ShapeFileSetBean.getShapeFileSetBeanFromFilesBean(userBean)) != null){
				shapeFileSetBeanList.add(sfsb);
			}
			
		}

		result.setShapeSetList(shapeFileSetBeanList);
		return result;
	}
	
	public List<ShapeFileSetBean> getShapeSetList() {
		if (this.shapeSetList == null) this.shapeSetList = new ArrayList<ShapeFileSetBean>();
		return this.shapeSetList;
	}
	public void setShapeSetList(List<ShapeFileSetBean> shapeSetList) {
		this.shapeSetList = shapeSetList;
	}
	public List<FilesBean> getExampleFileList() {
		if (this.exampleFileList == null) this.exampleFileList = new ArrayList<FilesBean>();
		return this.exampleFileList;
	}
	public void setExampleFileList(List<FilesBean> exampleFileList) {
		this.exampleFileList = exampleFileList;
	}
	public List<FilesBean> getUserFileList() {
		if (this.userFileList == null) this.userFileList = new ArrayList<FilesBean>();
		return this.userFileList;
	}
	public void setUserFileList(List<FilesBean> userFileList) {
		this.userFileList = userFileList;
	}
	
	
	
}
