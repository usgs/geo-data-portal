package gov.usgs.cida.gdp.dataaccess.bean;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import gov.usgs.cida.gdp.utilities.bean.XmlResponse;
@XStreamAlias("servers")
public class ServerList extends XmlResponse {

	@XStreamImplicit(itemFieldName="server")
	private List<Server> beans;

	public ServerList(List<Server> threddsServerBeanList) {
		this.beans = threddsServerBeanList;
	}
	public void setBeans(List<Server> beans) {
		this.beans = beans;
	}

	public List<Server> getBeans() {
		return beans;
	} 
}
