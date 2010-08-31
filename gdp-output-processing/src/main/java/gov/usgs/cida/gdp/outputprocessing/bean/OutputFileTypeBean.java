package gov.usgs.cida.gdp.outputprocessing.bean;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import gov.usgs.cida.gdp.utilities.bean.XmlResponse;

@XStreamAlias("outputfiletypes")
public class OutputFileTypeBean implements XmlResponse{
	@XStreamImplicit(itemFieldName="type")
	private List<String> types;

	public OutputFileTypeBean() {
		this.types = new ArrayList<String>();
	}
	
	public OutputFileTypeBean(List<String> types) {
		this.types = types;
	}
	
	public OutputFileTypeBean(String... types) {
		this.types = new ArrayList<String>();
		for (String type : types) {
			this.types.add(type);
		}
	}
	
	public void setTypes(List<String> types) {
		this.types = types;
	}

	public List<String> getTypes() {
		return types;
	}

	
}
