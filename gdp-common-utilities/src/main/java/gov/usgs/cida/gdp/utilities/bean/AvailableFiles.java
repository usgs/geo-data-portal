package gov.usgs.cida.gdp.utilities.bean;

import gov.usgs.cida.gdp.utilities.FileHelper;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("files")
public class AvailableFiles implements XmlResponse {

    @XStreamAlias("shapeSets")
    private List<ShapeFileSet> shapeSetList;
    @XStreamAlias("exampleFiles")
    private List<Files> exampleFileList;
    @XStreamAlias("userFiles")
    private List<Files> userFileList;

    static public AvailableFiles getAvailableFilesBean(String baseDirectory) throws IllegalArgumentException {
        return AvailableFiles.getAvailableFilesBean(baseDirectory, null);
    }

    /**
     * Create an AvailableFiles from a passed base directory.
     *
     * @param baseDirectory
     * @param userDirectory
     * @return
     */
    static public AvailableFiles getAvailableFilesBean(String baseDirectory, String userDirectory) throws IllegalArgumentException {
        if (baseDirectory == null) return null;
        if ("".equals(baseDirectory)) return null;

        AvailableFiles result = new AvailableFiles();
        List<Files> allFilesBeanList = new ArrayList<Files>();
        List<ShapeFileSet> allShapes = new ArrayList<ShapeFileSet>();
        String exampleDirectory = baseDirectory
                + "Sample_Files"
                + FileHelper.getSeparator();

        // Create the user file bean list (if calling method decides)
        if (userDirectory != null && !"".equals(userDirectory)) {
            allFilesBeanList = Files.getFilesBeanSetList(exampleDirectory, userDirectory);
        } else {
            allFilesBeanList = Files.getFilesBeanSetList(exampleDirectory, true);
        }


        for (Files filesBean : allFilesBeanList) {
            ShapeFileSet sfsb = null;
            if (filesBean.getUserDirectory() != null) {
                result.getUserFileList().add(filesBean);
            } else {
                result.getExampleFileList().add(filesBean);
            }

            if ((sfsb = ShapeFileSet.getShapeFileSetBeanFromFilesBean(filesBean)) != null) {
                allShapes.add(sfsb);
            }
        }

        result.setShapeSetList(allShapes);
        return result;
    }

    public List<ShapeFileSet> getShapeSetList() {
        if (this.shapeSetList == null) {
            this.shapeSetList = new ArrayList<ShapeFileSet>();
        }
        return this.shapeSetList;
    }

    public void setShapeSetList(List<ShapeFileSet> shapeSetList) {
        this.shapeSetList = shapeSetList;
    }

    public List<Files> getExampleFileList() {
        if (this.exampleFileList == null) {
            this.exampleFileList = new ArrayList<Files>();
        }
        return this.exampleFileList;
    }

    public void setExampleFileList(List<Files> exampleFileList) {
        this.exampleFileList = exampleFileList;
    }

    public List<Files> getUserFileList() {
        if (this.userFileList == null) {
            this.userFileList = new ArrayList<Files>();
        }
        return this.userFileList;
    }

    public void setUserFileList(List<Files> userFileList) {
        this.userFileList = userFileList;
    }
}
