package gov.usgs.gdp.analysis;

import java.io.BufferedWriter;
import java.io.IOException;

public interface GridStatisticsWriter {

	public void write(BufferedWriter writer)  throws IOException;
	
	
}
