package gov.usgs.cida.gdp.utilities.bean;

import gov.usgs.cida.gdp.utilities.FileHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("fileSet")
public class Files implements XmlResponse {

    @XStreamAlias("fileSetName")
    @XStreamAsAttribute
    private String name;
    @XStreamAlias("userDirectory")
    @XStreamAsAttribute
    private String userDirectory;
    private static final long serialVersionUID = 1L;
    @XStreamAlias("files")
    @XStreamImplicit
    private Collection<File> files;

//    public String toXml() {
//        XStream xstream = new XStream();
//        xstream.processAnnotations(Files.class);
//        return xstream.toXML(this);
//    }

    public ShapeFileSet getShapeFileSetBean() {
        ShapeFileSet result = new ShapeFileSet();
        for (File file : this.files) {
            if (file.getName().toLowerCase().contains(".shp")) {
                result.setShapeFile(file);
            } else if (file.getName().toLowerCase().contains(".prj")) {
                result.setProjectionFile(file);
            } else if (file.getName().toLowerCase().contains(".dbf")) {
                result.setDbfFile(file);
            }
        }
        return result;
    }

    public Collection<File> getFiles() {
        if (this.files == null) {
            this.files = new ArrayList<File>();
        }
        return this.files;
    }

    public void setFiles(Collection<File> localFiles) {
        this.files = localFiles;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String localName) {
        this.name = localName;
    }

    /**
     * Gets sets of FileBeans from the exampleDir and optionally userDir
     *
     * @param exampleDir
     * @param userDir
     * @return
     */
    public static List<Files> getFilesBeanSetList(String exampleDir, String userDir) {
        List<Files> result = new ArrayList<Files>();
        result.addAll(Files.getFilesBeanSetList(exampleDir, true));
        List<Files> userFiles = new ArrayList<Files>();
        if (userDir != null && !"".equals(userDir)) {
            //result.addAll(Files.getFilesBeanSetList(userDir, false));
            userFiles = Files.getFilesBeanSetList(userDir, false);

            for (Files filesBean : userFiles) {
                filesBean.setUserDirectory(userDir.substring(userDir.lastIndexOf('/') + 1));
            }
            result.addAll(userFiles);
        }
        return result;
    }

    public static List<Files> getFilesBeanSetList(String directory, boolean recursive) throws IllegalArgumentException {
        return Files.getFilesBeanSetList(FileHelper.getFileCollection(directory, recursive));
    }

    /**
     * Gets a list of FileBean objects organized from a Collection of File objects
     *
     * @param files
     * @return
     */
    public static List<Files> getFilesBeanSetList(Collection<File> files) {
        if (files == null) {
            return null;
        }
        List<Files> result = new ArrayList<Files>();
        Map<String, Files> filesBeanMap = new HashMap<String, Files>();

        for (File file : files) {
            String fileName = file.getName();
            String fileNameWithoutSuffix = fileName;
            if (fileName.contains(".")) {
                fileNameWithoutSuffix = fileName.substring(0, fileName.lastIndexOf('.'));
            }

            //Check if we already have a Files by this name
            Files filesBean = filesBeanMap.get(fileNameWithoutSuffix);
            if (filesBean == null) {
                filesBean = new Files();
                filesBean.setName(fileNameWithoutSuffix);
            }
            filesBean.getFiles().add(file);
            filesBeanMap.put(fileNameWithoutSuffix, filesBean);
        }

        Iterator<String> filesBeanMapIterator = filesBeanMap.keySet().iterator();

        while (filesBeanMapIterator.hasNext()) {
            String key = filesBeanMapIterator.next();
            result.add(filesBeanMap.get(key));
        }
        return result;
    }

    /**
     * @return the userDirectory
     */
    public String getUserDirectory() {
        return userDirectory;
    }

    /**
     * @param userDirectory the userDirectory to set
     */
    public void setUserDirectory(String userDirectory) {
        this.userDirectory = userDirectory;
    }
}
