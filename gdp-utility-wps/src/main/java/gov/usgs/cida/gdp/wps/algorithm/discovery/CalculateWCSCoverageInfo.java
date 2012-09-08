package gov.usgs.cida.gdp.wps.algorithm.discovery;

import com.google.common.base.Preconditions;
import gov.usgs.cida.gdp.dataaccess.WCSCoverageInfoHelper;
import gov.usgs.cida.gdp.dataaccess.bean.WCSCoverageInfo;
import gov.usgs.cida.n52.wps.algorithm.AbstractAnnotatedAlgorithm;
import gov.usgs.cida.n52.wps.algorithm.annotation.Algorithm;
import gov.usgs.cida.n52.wps.algorithm.annotation.LiteralDataInput;
import gov.usgs.cida.n52.wps.algorithm.annotation.LiteralDataOutput;
import gov.usgs.cida.n52.wps.algorithm.annotation.Process;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author isuftin
 */
@Algorithm(version="1.0.0")
public class CalculateWCSCoverageInfo extends AbstractAnnotatedAlgorithm {
    
    private final static Logger LOGGER = LoggerFactory.getLogger(CalculateWCSCoverageInfo.class);
    
    private final static String PARAM_CRS = "crs";
    private final static String PARAM_LOWER_CORNER = "lower-corner";
    private final static String PARAM_UPPER_CORNER = "upper-corner";
    private final static String PARAM_GRID_OFFSETS = "grid-offsets";
    private final static String PARAM_DATA_TYPE = "data-type";
    private final static String PARAM_WFS_URL = "wfs-url";
    private final static String PARAM_DATASTORE = "datastore";
    private final static String PARAM_RESULT = "result";
    
    private String crs;
    private String lowerCorner;
    private String upperCorner;
    private String gridOffsets;
    private String dataType;
    private String wfsURL;
    private String datastore;
    private String result;
    
    
    @LiteralDataInput(identifier=PARAM_CRS)
    public void setCRS(String crs) {
        this.crs = crs;
    }
    
    @LiteralDataInput(identifier=PARAM_LOWER_CORNER)
    public void setLowerCorner(String lowerCorner) {
        this.lowerCorner = lowerCorner;
    }
    
    @LiteralDataInput(identifier=PARAM_UPPER_CORNER)
    public void setUpperCorner(String upperCorner) {
        this.upperCorner = upperCorner;
    }
    
    @LiteralDataInput(identifier=PARAM_GRID_OFFSETS)
    public void setGridOffsets (String gridOffsets) {
        this.gridOffsets = gridOffsets;
    }
    
    @LiteralDataInput(identifier=PARAM_DATA_TYPE)
    public void setDataType(String dataType) {
        this.dataType = dataType;
    }
    
    @LiteralDataInput(identifier=PARAM_WFS_URL)
    public void setWfsURL(String wfsURL) {
        this.wfsURL = wfsURL;
    }
    
    @LiteralDataInput(identifier=PARAM_DATASTORE)
    public void setDatastore(String datastore) {
        this.datastore = datastore;
    }
    
    @LiteralDataOutput(identifier=PARAM_RESULT)
    public String getResult() {
        return result;
    }
    
    @Process
    public void process() {
        Preconditions.checkArgument(StringUtils.isNotBlank(crs), "Invalid " + PARAM_CRS);
        Preconditions.checkArgument(StringUtils.isNotBlank(lowerCorner), "Invalid " + PARAM_LOWER_CORNER);
        Preconditions.checkArgument(StringUtils.isNotBlank(upperCorner), "Invalid " + PARAM_UPPER_CORNER);
        Preconditions.checkArgument(StringUtils.isNotBlank(gridOffsets), "Invalid " + PARAM_GRID_OFFSETS);
        Preconditions.checkArgument(StringUtils.isNotBlank(dataType), "Invalid " + PARAM_DATA_TYPE);
        Preconditions.checkArgument(StringUtils.isNotBlank(wfsURL), "Invalid " + PARAM_WFS_URL);
        Preconditions.checkArgument(StringUtils.isNotBlank(datastore), "Invalid " + PARAM_DATASTORE);

        try {
        	WCSCoverageInfo wcsCoverageInfoBean = WCSCoverageInfoHelper.calculateWCSCoverageInfo(
                    wfsURL,
                    datastore,
                    lowerCorner,
                    upperCorner,
                    crs,
                    gridOffsets,
                    dataType);
            result = wcsCoverageInfoBean.toXML();
        } catch (Exception e) {
        	LOGGER.error(e.getMessage());
            addError(e.getMessage());
        }
    }
}