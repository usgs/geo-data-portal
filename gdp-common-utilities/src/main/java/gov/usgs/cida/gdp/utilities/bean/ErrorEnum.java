/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.usgs.cida.gdp.utilities.bean;

import gov.usgs.cida.gdp.utilities.bean.XmlBean;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import gov.usgs.cida.gdp.utilities.PropertyFactory;
import java.util.Date;

/**
 *
 * @author isuftin
 */
@XStreamAlias("error")
public enum ErrorEnum  implements XmlBean {
   // ERR_CUSTOM_MESSAGE(-1),
    ERR_NO_COMMAND(0),
    ERR_USER_DIR_CREATE(1),
    ERR_FILE_UPLOAD(2),
    ERR_FILE_LIST(3),
    ERR_FILE_NOT_FOUND(4),
    ERR_ATTRIBUTES_NOT_FOUND(5),
    ERR_FEATURES_NOT_FOUND(6),
    ERR_ERROR_WHILE_CONNECTING(7),
    ERR_MISSING_PARAM(8),
    ERR_MISSING_DATASET(9),
    ERR_PORT_INCORRECT(10),
    ERR_MISSING_THREDDS(11),
    ERR_MISSING_TIMERANGE(12),
    ERR_PARSE_TIMERANGE(13),
    ERR_INVALID_URL(14),
    ERR_PROTOCOL_VIOLATION(15),
    ERR_TRANSPORT_ERROR(16),
    ERR_OUTFILES_UNAVAILABLE(17),
    ERR_BOX_NO_INTERSECT_GRID(18),
    ERR_EMAIL_ERROR(19),
    ERR_EMAIL_ERROR_INCORRECT_ADDRESS(20);

    @XStreamAlias("code")
    @XStreamAsAttribute
    private Integer errorNumber;
    @XStreamAlias("message")
    private String errorMessage;
    @XStreamAlias("exception")
    private Exception exception;
    @XStreamAlias("errorCreated")
    private Date errorCreated;
    @XStreamAlias("errorClass")
    private String errorClass;

    ErrorEnum() {
    String localError = PropertyFactory.getProperty("error.message." + this.ordinal());
    this.errorMessage = localError;
    }

    ErrorEnum(int errorNumber) {
        this.errorNumber = errorNumber;
        String localError = PropertyFactory.getProperty("error.message." + this.ordinal());
        this.errorMessage = localError;
    }

@Override
    public String toXml() {
        XStream xstream = new XStream();
        xstream.processAnnotations(ErrorEnum.class);
        StringBuffer sb = new StringBuffer();
        String result = "";
        sb.append(xstream.toXML(this));
        result = sb.toString();
        return result;
    }

    public void setErrorMessage(String errorMessageParam) {
        this.errorMessage = errorMessageParam;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }

    public void setException(Exception stackTrace) {
        this.exception = stackTrace;
    }

    public Exception getException() {
        return this.exception;
    }

    public void setErrorCreated(Date errorCreatedParam) {
        this.errorCreated = errorCreatedParam;
    }

    public Date getErrorCreated() {
        return this.errorCreated;
    }

    public void setErrorClass(String errorClassParam) {
        this.errorClass = errorClassParam;
    }

    @Override
    public String toString() {
        return "ErrorBean [errorClass=" + this.errorClass + ", errorCreated="
                + this.errorCreated + ", errorMessage=" + this.errorMessage
                + ", errorNumber=" + this.errorNumber + ", exception=" + this.exception
                + "]";
    }

    public String getErrorClass() {
        return this.errorClass;
    }

    public void setErrorNumber(Integer errorNumberParam) {
        this.errorNumber = errorNumberParam;
    }

    public Integer getErrorNumber() {
        return this.errorNumber;
    }


}
