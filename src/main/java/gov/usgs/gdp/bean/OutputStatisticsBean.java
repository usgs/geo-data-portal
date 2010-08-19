package gov.usgs.gdp.bean;

import gov.usgs.cida.gdp.utilities.bean.XmlBean;
import gov.usgs.cida.gdp.utilities.PropertyFactory;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("outputStatistics")
public class OutputStatisticsBean implements XmlBean {

	@XStreamAlias("statistics")
	@XStreamImplicit(itemFieldName="statistic")
	private List<String> statistics;

	OutputStatisticsBean() {
		statistics = new ArrayList<String>();
	}
	
	OutputStatisticsBean(List<String> statistics) {
		this.statistics = statistics;
	}
	
	public static OutputStatisticsBean getOutputStatisticsBean() {
		OutputStatisticsBean result = null;
		int index = 0;
		String statistic = null;
		List<String> statistics = new ArrayList<String>();
		do {
			statistic = PropertyFactory.getProperty("stat.output." + index++);
			if (!"".equals(statistic)) {
				statistics.add(statistic);
			}
		} while (!"".equals(statistic));
		if (!statistics.isEmpty()) result = new OutputStatisticsBean(statistics);
		return result;
	}
	
	@Override
	public String toXml() {
		XStream xstream = new XStream();
		xstream.processAnnotations(OutputStatisticsBean.class);
		StringBuffer sb = new StringBuffer();
		String result = "";
		sb.append(xstream.toXML(this));
		result = sb.toString();
		return result;
	}
	
	public void setStatistics(List<String> statistics) {
		this.statistics = statistics;
	}

	public List<String> getStatistics() {
		return statistics;
	}
	
}
