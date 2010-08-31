package gov.usgs.cida.gdp.dataaccess.bean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ucar.nc2.VariableSimpleIF;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import gov.usgs.cida.gdp.utilities.bean.XmlResponse;

@XStreamAlias("datatypecollection")
public class DataTypeCollectionBean implements XmlResponse {

    @XStreamAlias("datatype")
	@XStreamAsAttribute
	private String dataType;
        	
    @XStreamAlias("types")
	private List<DataTypeBean> dataTypeCollection;
	
	public DataTypeCollectionBean() {}
	
	public DataTypeCollectionBean(String type, DataTypeBean... dataTypeArray) {
		this.dataType = type;
		this.dataTypeCollection = Arrays.asList(dataTypeArray);
	}
	
	public DataTypeCollectionBean(String type, VariableSimpleIF... variableSimpleIFArray) {
		this.dataType = type;
		List<DataTypeBean> dtbList = new ArrayList<DataTypeBean>(variableSimpleIFArray.length);
		for (VariableSimpleIF vsif : variableSimpleIFArray) {
			DataTypeBean dtb = new DataTypeBean(vsif);
			dtbList.add(dtb);
		}
		this.dataTypeCollection = dtbList;
	}

    /**
     * @return the dataType
     */
    public String getDataType() {
        return dataType;
    }

    /**
     * @param dataType the dataType to set
     */
    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    /**
     * @return the dataTypeCollection
     */
    public List<DataTypeBean> getDataTypeCollection() {
        return dataTypeCollection;
    }

    /**
     * @param dataTypeCollection the dataTypeCollection to set
     */
    public void setDataTypeCollection(List<DataTypeBean> dataTypeCollection) {
        this.dataTypeCollection = dataTypeCollection;
    }
	
    @XStreamAlias("type")
	static public class DataTypeBean implements XmlResponse {

		private String description;
		private String name;
		private int rank;
		private int[] shape;
		private String shortname;
		private String unitsstring;
		
		public DataTypeBean() {	}
		public DataTypeBean(VariableSimpleIF variableSimpleIF) {
			this.description = variableSimpleIF.getDescription();
			this.name = variableSimpleIF.getName();
			this.rank = variableSimpleIF.getRank();
			this.shape = variableSimpleIF.getShape();
			this.shortname = variableSimpleIF.getShortName();
			this.unitsstring = variableSimpleIF.getUnitsString();
		}
		
		public String toXml() {
			XStream xstream = new XStream();
			xstream.processAnnotations(DataTypeBean.class);
			StringBuilder sb = new StringBuilder();
			String result = "";
			sb.append(xstream.toXML(this));
			result = sb.toString();
			return result;
		}
		public String getDescription() {
			return description;
		}
		public void setDescription(String description) {
			this.description = description;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public int getRank() {
			return rank;
		}
		public void setRank(int rank) {
			this.rank = rank;
		}
		public int[] getShape() {
			return shape;
		}
		public void setShape(int[] shape) {
			this.shape = shape;
		}
		public String getShortname() {
			return shortname;
		}
		public void setShortname(String shortname) {
			this.shortname = shortname;
		}
		public String getUnitsstring() {
			return unitsstring;
		}
		public void setUnitsstring(String unitsstring) {
			this.unitsstring = unitsstring;
		}
	}
}