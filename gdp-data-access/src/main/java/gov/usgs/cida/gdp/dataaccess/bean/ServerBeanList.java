package gov.usgs.cida.gdp.dataaccess.bean;

import com.thoughtworks.xstream.XStream;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import gov.usgs.cida.gdp.utilities.bean.XmlResponse;
@XStreamAlias("servers")
public class ServerBeanList implements XmlResponse {

	@XStreamImplicit(itemFieldName="server")
	private List<ServerBean> beans;

	public ServerBeanList(List<ServerBean> threddsServerBeanList) {
		this.beans = threddsServerBeanList;
	}
	public void setBeans(List<ServerBean> beans) {
		this.beans = beans;
	}

	public List<ServerBean> getBeans() {
		return beans;
	} 
}
