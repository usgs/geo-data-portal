package gov.usgs.cida.gdp.outputprocessing.bean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import gov.usgs.cida.gdp.utilities.bean.XmlResponse;

@XStreamAlias("outputfiletypes")
public class OutputFileType extends XmlResponse {

    @XStreamImplicit(itemFieldName = "type")
    private List<String> types;

    public OutputFileType() {
        this.types = new ArrayList<String>();
    }

    public OutputFileType(List<String> types) {
        this.types = types;
    }

    public OutputFileType(String... types) {
        this.types = new ArrayList<String>();
        this.types.addAll(Arrays.asList(types));
    }

    public void setTypes(List<String> types) {
        this.types = types;
    }

    public List<String> getTypes() {
        return types;
    }
}
