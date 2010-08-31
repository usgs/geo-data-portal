package gov.usgs.cida.gdp.outputprocessing.bean;

import gov.usgs.cida.gdp.utilities.PropertyFactory;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import gov.usgs.cida.gdp.utilities.bean.XmlResponse;

@XStreamAlias("outputStatistics")
public class OutputStatisticsBean implements XmlResponse {

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
	
	public void setStatistics(List<String> statistics) {
		this.statistics = statistics;
	}

	public List<String> getStatistics() {
		return statistics;
	}
	
}
