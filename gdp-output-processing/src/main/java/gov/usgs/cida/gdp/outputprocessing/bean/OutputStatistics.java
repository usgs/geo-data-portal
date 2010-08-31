package gov.usgs.cida.gdp.outputprocessing.bean;

import gov.usgs.cida.gdp.utilities.PropertyFactory;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import gov.usgs.cida.gdp.utilities.bean.XmlResponse;

@XStreamAlias("outputStatistics")
public class OutputStatistics implements XmlResponse {

	@XStreamAlias("statistics")
	@XStreamImplicit(itemFieldName="statistic")
	private List<String> statistics;

	OutputStatistics() {
		statistics = new ArrayList<String>();
	}
	
	OutputStatistics(List<String> statistics) {
		this.statistics = statistics;
	}
	
	public static OutputStatistics getOutputStatisticsBean() {
		OutputStatistics result = null;
		int index = 0;
		String statistic = null;
		List<String> statistics = new ArrayList<String>();
		do {
			statistic = PropertyFactory.getProperty("stat.output." + index++);
			if (!"".equals(statistic)) {
				statistics.add(statistic);
			}
		} while (!"".equals(statistic));
		if (!statistics.isEmpty()) result = new OutputStatistics(statistics);
		return result;
	}
	
	public void setStatistics(List<String> statistics) {
		this.statistics = statistics;
	}

	public List<String> getStatistics() {
		return statistics;
	}
	
}
