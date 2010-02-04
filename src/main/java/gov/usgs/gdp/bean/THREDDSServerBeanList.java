package gov.usgs.gdp.bean;

import java.util.List;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
@XStreamAlias("threddsbeans")
public class THREDDSServerBeanList implements XmlBean {

	@XStreamImplicit(itemFieldName="threddsbean")
	private List<THREDDSServerBean> beans;

	public THREDDSServerBeanList(List<THREDDSServerBean> threddsServerBeanList) {
		this.beans = threddsServerBeanList;
	}

	@Override
	public String toXml() {
		XStream xstream = new XStream();
		xstream.autodetectAnnotations(true);
		StringBuffer sb = new StringBuffer();
		String result = "";
		sb.append(xstream.toXML(this));
		result = sb.toString();
		return result;
	}

	public void setBeans(List<THREDDSServerBean> beans) {
		this.beans = beans;
	}

	public List<THREDDSServerBean> getBeans() {
		return beans;
	} 
}
