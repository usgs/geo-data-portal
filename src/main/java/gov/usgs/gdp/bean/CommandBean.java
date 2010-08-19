package gov.usgs.gdp.bean;

import gov.usgs.cida.gdp.utilities.bean.XmlBean;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import gov.usgs.cida.gdp.utilities.PropertyFactory;
import java.util.ArrayList;
import java.util.List;

@XStreamAlias("command")
public class CommandBean implements XmlBean {

    //command;useage;required parameters;optional parameters;example;description
    @XStreamAlias("name")
    private String command;
    private String useage;
    private String requiredParameters;
    private String optionalParameters;
    private String example;
    private String description;
    

    public CommandBean(String command, String useage, String requiredParameters, String optionalParameters, String example, String description) {
        this.command = command;
        this.useage = useage;
        this.requiredParameters = requiredParameters;
        this.optionalParameters = optionalParameters;
        this.example = example;
        this.description = description;
    }
    
    public static List<CommandBean> getCommandList() {
        List<CommandBean> result = new ArrayList<CommandBean>();
        String commandString = null;
        int index = 0;
        while (!"".equals(commandString)) {
            commandString = PropertyFactory.getProperty("commandlist." + index++);
            if (!"".equals(commandString)) {
               String[] commandArray = commandString.split(";");
               CommandBean commandListBean = new CommandBean(commandArray[0], commandArray[1], commandArray[2], commandArray[3], commandArray[4], commandArray[5]);
               result.add(commandListBean);
            }
        }
        return result;
    }


    @Override
    public String toXml() {
    	String result = "";
    	XStream xstream = new XStream();
    	xstream.processAnnotations(CommandBean.class);
    	StringBuffer sb = new StringBuffer();
    	sb.append(xstream.toXML(this));
    	result = sb.toString();
    	return result;
    }

    /**
     * @return the command
     */
    public String getCommand() {
        return command;
    }

    /**
     * @return the useage
     */
    public String getUseage() {
        return useage;
    }

    /**
     * @return the requiredParameters
     */
    public String getRequiredParameters() {
        return requiredParameters;
    }

    /**
     * @return the opionalParameters
     */
    public String getOptionalParameters() {
        return optionalParameters;
    }

    /**
     * @return the example
     */
    public String getExample() {
        return example;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param command the command to set
     */
    public void setCommand(String command) {
        this.command = command;
    }

    /**
     * @param useage the useage to set
     */
    public void setUseage(String useage) {
        this.useage = useage;
    }

    /**
     * @param requiredParameters the requiredParameters to set
     */
    public void setRequiredParameters(String requiredParameters) {
        this.requiredParameters = requiredParameters;
    }

    /**
     * @param opionalParameters the opionalParameters to set
     */
    public void setOptionalParameters(String optionalParameters) {
        this.optionalParameters = optionalParameters;
    }

    /**
     * @param example the example to set
     */
    public void setExample(String example) {
        this.example = example;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
}
