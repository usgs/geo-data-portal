package gov.usgs.cida.gdp.dataaccess.bean;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import gov.usgs.cida.gdp.utilities.bean.XmlResponse;

@XStreamAlias("WCSCoverageInfo")
public class WCSCoverageInfo extends XmlResponse {

    private static long serialVersionUID = 1L;

    /**
     * @return the serialVersionUID
     */
    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    /**
     * @param aSerialVersionUID the serialVersionUID to set
     */
    public static void setSerialVersionUID(long aSerialVersionUID) {
        serialVersionUID = aSerialVersionUID;
    }
    private String minResamplingFactor;
    private String fullyCovers;
    private String units;
    private String boundingBox;

    public WCSCoverageInfo(int minResamplingFactor, boolean fullyCovers,
            String units, String boundingBox) {

        this.minResamplingFactor = String.valueOf(minResamplingFactor);
        this.fullyCovers = String.valueOf(fullyCovers);
        this.units = units;
        this.boundingBox = boundingBox;
    }

    /**
     * @return the minResamplingFactor
     */
    public String getMinResamplingFactor() {
        return minResamplingFactor;
    }

    /**
     * @param minResamplingFactor the minResamplingFactor to set
     */
    public void setMinResamplingFactor(String minResamplingFactor) {
        this.minResamplingFactor = minResamplingFactor;
    }

    /**
     * @return the fullyCovers
     */
    public String getFullyCovers() {
        return fullyCovers;
    }

    /**
     * @param fullyCovers the fullyCovers to set
     */
    public void setFullyCovers(String fullyCovers) {
        this.fullyCovers = fullyCovers;
    }

    /**
     * @return the units
     */
    public String getUnits() {
        return units;
    }

    /**
     * @param units the units to set
     */
    public void setUnits(String units) {
        this.units = units;
    }

    /**
     * @return the boundingBox
     */
    public String getBoundingBox() {
        return boundingBox;
    }

    /**
     * @param boundingBox the boundingBox to set
     */
    public void setBoundingBox(String boundingBox) {
        this.boundingBox = boundingBox;
    }
}
