package gov.usgs.cida.gdp.dataaccess.bean;


import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import gov.usgs.cida.gdp.utilities.bean.XmlResponse;


@XStreamAlias("WCSCoverageInfo")
public class WCSCoverageInfoBean implements XmlResponse {
	
	private static final long serialVersionUID = 1L;
	
	private String minResamplingFactor;
	private String fullyCovers;
	private String units;
	private String boundingBox;

	public WCSCoverageInfoBean(int minResamplingFactor, boolean fullyCovers, 
			String units, String boundingBox) {
		
		this.minResamplingFactor = String.valueOf(minResamplingFactor);
		this.fullyCovers = String.valueOf(fullyCovers);
		this.units = units;
		this.boundingBox = boundingBox;
	}
}
