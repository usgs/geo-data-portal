 package gov.usgs.cida.gdp.wps.algorithm.filemanagement;

import com.google.common.base.Preconditions;
import gov.usgs.cida.gdp.constants.AppConstant;
import gov.usgs.cida.gdp.dataaccess.GeoserverManager;
import gov.usgs.cida.gdp.dataaccess.helper.ShapeFileEPSGHelper;
import gov.usgs.cida.gdp.io.data.ZippedGenericFileData;
import gov.usgs.cida.gdp.io.data.ZippedGenericFileDataBinding;
import gov.usgs.cida.gdp.utilities.FileHelper;
import gov.usgs.cida.n52.wps.algorithm.AbstractAnnotatedAlgorithm;
import gov.usgs.cida.n52.wps.algorithm.annotation.Algorithm;
import gov.usgs.cida.n52.wps.algorithm.annotation.ComplexDataInput;
import gov.usgs.cida.n52.wps.algorithm.annotation.LiteralDataInput;
import gov.usgs.cida.n52.wps.algorithm.annotation.LiteralDataOutput;
import gov.usgs.cida.n52.wps.algorithm.annotation.Process;
import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.lang.StringUtils;
import org.n52.wps.io.data.IData;
import org.n52.wps.io.data.binding.literal.LiteralStringBinding;
import org.opengis.referencing.FactoryException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This algorithm allows a client to upload a file to the server via WPS.
 * More info: http://privusgs2.er.usgs.gov/display/GDP/Adding+a+Shapefile+as+a+GeoServer+WFS+EndPoint
 *
 * @author isuftin
 */
@Algorithm(version="1.0.0")
public class ReceiveFiles extends AbstractAnnotatedAlgorithm {
     
    private Logger LOGGER = LoggerFactory.getLogger(ReceiveFiles.class);
    
    private static final String SUFFIX_SHP = ".shp";
    private static final String SUFFIX_SHX = ".shx";
    private static final String SUFFIX_PRJ = ".prj";
    private static final String SUFFIX_DBF = ".dbf";
    private static final String UPLOAD_WORKSPACE = "upload";
    
    private static final String PARAM_FILE = "file";
    private static final String PARAM_FILENAME = "filename";
    private static final String PARAM_WFS_URL = "wfs-url";
    private static final String PARAM_RESULT = "result";
    private static final String PARAM_FEATURETYPE = "featuretype";
    
    private ZippedGenericFileData file;
    private String fileName;
    private String wfsURL;
    private String featureType;
    private String result;

    @ComplexDataInput(identifier=PARAM_FILE, binding=ZippedGenericFileDataBinding.class)
    public void setFile(ZippedGenericFileData file) {
        this.file = file;
    }
    
    @LiteralDataInput(identifier=PARAM_FILENAME)
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    @LiteralDataInput(identifier=PARAM_WFS_URL)
    public void setWfsURL(String wfsURL) {
        this.wfsURL = wfsURL;
    }
    
    @LiteralDataOutput(identifier=PARAM_WFS_URL)
    public String getWfsURL() {
        return wfsURL;
    }
    
    @LiteralDataOutput(identifier=PARAM_FEATURETYPE)
    public String getFeatureType() {
        return featureType;
    }
    
    @LiteralDataOutput(identifier=PARAM_RESULT)
    public String getResult() {
        return result;
    }
    
    
    @Process
    public void process() {
        Preconditions.checkArgument(file != null, "Error while processing file: Could not get file from server");
        Preconditions.checkArgument(StringUtils.isNotBlank(wfsURL), "Invalid " + PARAM_WFS_URL);
        Preconditions.checkArgument(StringUtils.isNotBlank(fileName), "Invalid " + PARAM_FILENAME);
        
        // "gdp.shapefile.temp.path" should be set in the tomcat startup script or setenv.sh as JAVA_OPTS="-Dgdp.shapefile.temp.path=/wherever/you/want/this/file/placed"
        String fileDump = AppConstant.SHAPEFILE_LOCATION.getValue() + File.separator + UUID.randomUUID();

        // Ensure that the temp directory exists
        File temp = new File(fileDump);
        temp.mkdirs();

        fileName = fileName.replace(" ", "_");

        String shapefilePath = file.writeData(temp);
        if (shapefilePath == null) { // Not sure if that is the only reason newFilename would be null
            String errorMessage = "Error while processing file: Malformed zip file or incomplete shapefile";
            LOGGER.error(errorMessage);
            addError(errorMessage);
            throw new RuntimeException(errorMessage);
        }

        File shapefileFile = new File(shapefilePath);
        File shapefileDir = shapefileFile.getParentFile();
        
        String shapefileName = shapefileFile.getName();
        String shapefileNamePrefix = shapefileName.substring(0, shapefileName.lastIndexOf("."));

        // Find all files with filename with any extension
        String pattern = shapefileNamePrefix + "\\..*";
        FileFilter filter = new RegexFileFilter(pattern);

        String[] filenames = shapefileDir.list((FilenameFilter) filter);
        List<String> filenamesList = Arrays.asList(filenames);

        // Make sure required files are present
        String[] requiredFiles = { SUFFIX_SHP, SUFFIX_SHX, SUFFIX_PRJ, SUFFIX_DBF };
        for (String requiredFile : requiredFiles) {
            if (!filenamesList.contains(shapefileNamePrefix + requiredFile)) {
                String errorMessage = "Zip file missing " + requiredFile + " file.";
                LOGGER.error(errorMessage);
                addError(errorMessage);
                throw new RuntimeException(errorMessage);
            }
        }

        // Rename the files to the desired filenames
        File[] files = shapefileDir.listFiles(filter);
        for (File f : files) {
            String name = f.getName();
            String extension = name.substring(name.lastIndexOf("."));

            f.renameTo(new File(shapefileDir.getPath() + File.separator + fileName + extension));
        }

        String renamedShpPath = shapefileDir.getPath() + File.separator + fileName + SUFFIX_SHP;
        String renamedPrjPath = shapefileDir.getPath() + File.separator + fileName + SUFFIX_PRJ;

        // Do EPSG processing
        String declaredCRS = null;
        String nativeCRS = null;
        String warning = "";
        try {
            nativeCRS = new String(FileHelper.getByteArrayFromFile(new File(renamedPrjPath)));
            if (nativeCRS == null || nativeCRS.isEmpty()) {
                String errorMessage = "Error while getting Prj/WKT information from PRJ file. Function halted.";
                LOGGER.error(errorMessage);
                addError(errorMessage);
                throw new RuntimeException(errorMessage);
            }
            // The behavior of this method requires that the layer always force
            // projection from native to declared...
            declaredCRS = ShapeFileEPSGHelper.getDeclaredEPSGFromWKT(nativeCRS, false);
            if (declaredCRS == null || declaredCRS.isEmpty()) {
                declaredCRS = ShapeFileEPSGHelper.getDeclaredEPSGFromWKT(nativeCRS, true);
                warning = "Could not find EPSG code for prj definition. The geographic coordinate system '" + declaredCRS + "' will be used";
            } else if (declaredCRS.startsWith("ESRI:")) {
                declaredCRS = declaredCRS.replaceFirst("ESRI:", "EPSG:");
            }
            if (declaredCRS == null || declaredCRS.isEmpty()) {
                throw new RuntimeException("Could not attain EPSG code from shapefile. Please ensure proper projection and a valid PRJ file.");
            }
        } catch (Exception ex) {
            String errorMessage = "Error while getting EPSG information from PRJ file. Function halted.";
            LOGGER.error(errorMessage, ex);
            addError(errorMessage);
            throw new RuntimeException(errorMessage ,ex);
        }

        String workspace = UPLOAD_WORKSPACE;
        try {
            GeoserverManager mws = new GeoserverManager(wfsURL,
                    AppConstant.WFS_USER.getValue(), AppConstant.WFS_PASS.getValue());
            
            mws.createDataStore(renamedShpPath, fileName, workspace, nativeCRS, declaredCRS);
        } catch (IOException ex) {
            String errorMessage = "Error while communicating with WFS server. Please try again or contact system administrator.";
            LOGGER.error(errorMessage, ex);
            addError(errorMessage);
            throw new RuntimeException(errorMessage, ex);
        }
        
        // GeoServer has accepted the shapefile. Send the success response to the client.
        if (StringUtils.isBlank(warning)) {
            result = "OK: " + fileName + " successfully uploaded to workspace '" + workspace + "'!";
        } else {
            result = "WARNING: " + warning;
        }
        wfsURL += "?Service=WFS&Version=1.0.0&";
        featureType = workspace + ":" + fileName;
    }
}
