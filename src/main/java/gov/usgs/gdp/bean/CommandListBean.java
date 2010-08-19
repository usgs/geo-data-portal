package gov.usgs.gdp.bean;

import gov.usgs.cida.gdp.utilities.bean.XmlBean;
import java.util.List;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("command-list")
public class CommandListBean implements XmlBean {

    @XStreamImplicit
    private List<CommandBean> commandList;

    private CommandListBean()  {}
    
    public static CommandListBean getCommandListBean() {
	CommandListBean result = new CommandListBean();
	
	result.setCommandList(CommandBean.getCommandList());
	
	return result;
    }
    
    @Override
    public String toXml() {
    	String result = "";
    	XStream xstream = new XStream();
    	xstream.processAnnotations(CommandListBean.class);
    	StringBuffer sb = new StringBuffer();
    	sb.append(xstream.toXML(this));
    	result = sb.toString();
    	return result;
    }
    
    public List<CommandBean> getCommandList() {
        return commandList;
    }

    public void setCommandList(List<CommandBean> commandList) {
        this.commandList = commandList;
    }

    
}
