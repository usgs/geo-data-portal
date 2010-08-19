package gov.usgs.gdp.bean;

import gov.usgs.cida.gdp.utilities.bean.XmlBean;
import thredds.catalog.InvDataset;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("DataSet")
public class DataSetBean implements XmlBean {

	private String fullname;
	private String authority;
	private String catalogurl;
	private String history;
	private String id;
	private String name;
	private String processing;
	private String restrictaccess;
	private String rights;
	private String subseturl;
	private String summary;
	private String uniqueid;
	private boolean hasnesteddatasets;
	private boolean isharvest;
	private boolean hasaccess;
	
	public DataSetBean(){}
	
	public DataSetBean(String fullName) {
		this.fullname = fullName;
	}
	
	public DataSetBean(InvDataset invDataSet) {
		if (invDataSet != null) {
			this.fullname = invDataSet.getFullName();
			this.authority = invDataSet.getAuthority();
			this.catalogurl = invDataSet.getCatalogUrl();
			this.history = invDataSet.getHistory();
			this.id = invDataSet.getID();
			this.name = invDataSet.getName();
			this.processing = invDataSet.getProcessing();
			this.restrictaccess = invDataSet.getRestrictAccess();
			this.rights = invDataSet.getRights();
			this.subseturl = invDataSet.getSubsetUrl();
			this.summary = invDataSet.getSummary();
			this.uniqueid = invDataSet.getUniqueID();
			this.hasnesteddatasets = invDataSet.hasNestedDatasets();
			this.isharvest = invDataSet.isHarvest();
			this.hasaccess = invDataSet.hasAccess();	
		} else {
			throw new IllegalArgumentException("InvDataSet passed in was null");
		}
		
	}
	
	public String getCatalogurl() {
		return catalogurl;
	}

	public void setCatalogurl(String catalogurl) {
		this.catalogurl = catalogurl;
	}



	public String getHistory() {
		return history;
	}

	public void setHistory(String history) {
		this.history = history;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getProcessing() {
		return processing;
	}

	public void setProcessing(String processing) {
		this.processing = processing;
	}

	public String getRestrictaccess() {
		return restrictaccess;
	}

	public void setRestrictaccess(String restrictaccess) {
		this.restrictaccess = restrictaccess;
	}

	public String getRights() {
		return rights;
	}

	public void setRights(String rights) {
		this.rights = rights;
	}

	public String getSubseturl() {
		return subseturl;
	}

	public void setSubseturl(String subseturl) {
		this.subseturl = subseturl;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getUniqueid() {
		return uniqueid;
	}

	public void setUniqueid(String uniqueid) {
		this.uniqueid = uniqueid;
	}

	public boolean isHasnesteddatasets() {
		return hasnesteddatasets;
	}

	public void setHasnesteddatasets(boolean hasnesteddatasets) {
		this.hasnesteddatasets = hasnesteddatasets;
	}

	public boolean isIsharvest() {
		return isharvest;
	}

	public void setIsharvest(boolean isharvest) {
		this.isharvest = isharvest;
	}

	public boolean isHasaccess() {
		return hasaccess;
	}

	public void setHasaccess(boolean hasaccess) {
		this.hasaccess = hasaccess;
	}

	@Override
	public String toXml() {
		XStream xstream = new XStream();
		xstream.processAnnotations(DataSetBean.class);
		StringBuffer sb = new StringBuffer();
		String result = "";
		sb.append(xstream.toXML(this));
		result = sb.toString();
		return result;
	}


	public void setFullname(String fullname) {
		this.fullname = fullname;
	}


	public String getFullname() {
		return fullname;
	}

	public void setAuthority(String authority) {
		this.authority = authority;
	}

	public String getAuthority() {
		return authority;
	}

	public void setCatalogUrl(String catalogUrl) {
		this.catalogurl = catalogUrl;
	}

	public String getCatalogUrl() {
		return catalogurl;
	}
}
