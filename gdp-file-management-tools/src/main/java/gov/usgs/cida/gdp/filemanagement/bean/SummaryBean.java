package gov.usgs.cida.gdp.filemanagement.bean;

import java.util.ArrayList;
import java.util.List;

public class SummaryBean {

    private String fileName;
    private List<String> fileSummary;

    public SummaryBean() {
        this.fileName = "";
        this.fileSummary = new ArrayList<String>();
    }

    public SummaryBean(String localFileName) {
        this.fileName = localFileName;
    }

    public SummaryBean(List<String> fileSummaryParam) {
        this.fileSummary = fileSummaryParam;
    }

    public SummaryBean(String fileNameParam, List<String> fileSummaryParam) {
        this.fileName = fileNameParam;
        this.fileSummary = fileSummaryParam;
    }

    public String getFileName() {
        return this.fileName;
    }

    public void setFileName(String fileNameParam) {
        this.fileName = fileNameParam;
    }

    public List<String> getFileSummary() {
        return this.fileSummary;
    }

    public void setFileSummary(List<String> fileSummaryParam) {
        this.fileSummary = fileSummaryParam;
    }
}
