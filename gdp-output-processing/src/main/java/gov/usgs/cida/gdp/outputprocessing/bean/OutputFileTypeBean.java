package gov.usgs.cida.gdp.outputprocessing.bean;

import gov.usgs.cida.gdp.utilities.bean.XmlBean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("outputfiletypes")
public class OutputFileTypeBean implements XmlBean{
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
        this.types.addAll(Arrays.asList(types));
	}
	
	@Override
	public String toXml() {
		XStream xstream = new XStream();
		xstream.processAnnotations(OutputFileTypeBean.class);
		StringBuilder sb = new StringBuilder();
		String result = "";
		sb.append(xstream.toXML(this));
		result = sb.toString();
		return result;
	}
	
	public void setTypes(List<String> types) {
		this.types = types;
	}

	public List<String> getTypes() {
		return types;
	}

	
}
