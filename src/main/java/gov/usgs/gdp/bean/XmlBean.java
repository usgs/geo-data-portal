package gov.usgs.gdp.bean;


public interface XmlBean {
	
	// TODO: implement toXml in here
	/*public String toXml() {
		XStream xstream = new XStream();
		xstream.processAnnotations(this.getClass());
		StringBuffer sb = new StringBuffer();
		String result = "";
		sb.append(xstream.toXML(this));
		result = sb.toString();
		return result;
	}*/
	
	String toXml();

}
