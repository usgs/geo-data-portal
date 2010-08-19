package gov.usgs.gdp.bean;

import gov.usgs.cida.gdp.webapp.bean.ErrorBean;
import gov.usgs.cida.gdp.utilities.bean.XmlBean;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("xmlresponse")
public class PassThroughXmlResponseBean implements XmlBean {

	private String xml;
	
	public PassThroughXmlResponseBean(String xml) {
		this.xml = xml;
	}
	
	@Override
	public String toXml() {
		XStream xstream = new XStream();
		xstream.processAnnotations(ErrorBean.class);
		StringBuffer sb = new StringBuffer();
		String result = "";
		sb.append(xstream.toXML(this));
		result = sb.toString();
		return result;
	}
	public void setXml(String xml) {
		this.xml = xml;
	}
	public String getXml() {
		return xml;
	}

}
