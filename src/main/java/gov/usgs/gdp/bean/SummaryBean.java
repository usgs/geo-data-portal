package gov.usgs.gdp.bean;

import java.util.ArrayList;
import java.util.List;

public class SummaryBean {
	private String fileName;
	private List<String> fileSummary;

        public SummaryBean() {
            this.fileName = "";
            this.fileSummary = new ArrayList<String>();
        }

	public SummaryBean(String fileName) {
		new SummaryBean(fileName, null);
	}
	
	public SummaryBean(List<String> fileSummary) {
		new SummaryBean(null, fileSummary);
	}
	
	public SummaryBean(String fileName, List<String> fileSummary) {
		this.fileName = fileName;
		this.fileSummary = fileSummary;
	}
	
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public List<String> getFileSummary() {
		return fileSummary;
	}
	public void setFileSummary(List<String> fileSummary) {
		this.fileSummary = fileSummary;
	}
	
	
}
