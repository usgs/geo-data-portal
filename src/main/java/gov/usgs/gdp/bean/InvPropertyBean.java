package gov.usgs.gdp.bean;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("invproperty")
public class InvPropertyBean implements XmlBean {

	private String name;
	private String value;
	
	@Override
	public String toXml() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

}
