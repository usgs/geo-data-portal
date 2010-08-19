package gov.usgs.gdp.bean;

import gov.usgs.cida.gdp.utilities.bean.XmlBean;
import java.util.List;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
@XStreamAlias("servers")
public class ServerBeanList implements XmlBean {

	@XStreamImplicit(itemFieldName="server")
	private List<ServerBean> beans;

	public ServerBeanList(List<ServerBean> threddsServerBeanList) {
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

	public void setBeans(List<ServerBean> beans) {
		this.beans = beans;
	}

	public List<ServerBean> getBeans() {
		return beans;
	} 
}
