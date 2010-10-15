package gov.usgs.cida.gdp.utilities.bean;

import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Represents an acknowledgement to the client
 *
 * @author isuftin
 */
@XStreamAlias("acknowledgment")
public class Acknowledgement extends XmlResponse {

    public static final int ACK_OK = 1;
    public static final int ACK_FAIL = 2;
    private String status;

    public Acknowledgement(int status) throws IllegalArgumentException {
        switch (status) {
            case (1):
                this.status = "OK";
                break;
            case (2):
                this.status = "FAIL";
                break;
            default:
                throw new IllegalArgumentException("AckBean does not have a status of: " + status);
        }
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
