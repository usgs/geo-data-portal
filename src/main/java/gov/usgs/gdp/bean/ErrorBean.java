package gov.usgs.gdp.bean;

import java.util.ArrayList;
import java.util.List;

public class ErrorBean {
	private List<String> errors;

	public List<String> getErrors() {
		if (this.errors == null) this.errors = new ArrayList<String>();
		return errors;
	}

	public void setErrors(List<String> errors) {
		this.errors = errors;
	}
}
