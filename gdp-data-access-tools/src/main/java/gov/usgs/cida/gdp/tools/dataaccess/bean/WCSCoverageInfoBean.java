package gov.usgs.cida.gdp.tools.dataaccess.bean;


import gov.usgs.cida.gdp.utilities.bean.XmlBean;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;


@XStreamAlias("WCSCoverageInfo")
public class WCSCoverageInfoBean implements XmlBean {
	
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

	@Override
	public String toXml() {
		XStream xstream = new XStream();
		xstream.processAnnotations(WCSCoverageInfoBean.class);
		StringBuffer sb = new StringBuffer();
		String result = "";
		sb.append(xstream.toXML(this));
		result = sb.toString();
		return result;
	}
}
