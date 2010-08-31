package gov.usgs.cida.gdp.utilities.bean;

import com.thoughtworks.xstream.XStream;
import java.util.List;

import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("reply")
public class XmlReply implements XmlResponse {

    private static org.slf4j.Logger log = LoggerFactory.getLogger(XmlReply.class);
    private Acknowledgement acknowledgment;
    private Error errorBean;
    private ErrorEnum errorEnum;
    private Message message;
    private XmlResponse[] content;

    public XmlReply() {
        this.acknowledgment = new Acknowledgement(Acknowledgement.ACK_OK);
    }

    public XmlReply(XmlResponse... contentArray) {
        this.acknowledgment = new Acknowledgement(Acknowledgement.ACK_OK);
        this.content = contentArray;
    }

    public XmlReply(int result, List<XmlResponse> xmlBeanList) {
        this.acknowledgment = new Acknowledgement(result);
        XmlResponse[] xmlBeanArray = new XmlResponse[0];
        xmlBeanArray = xmlBeanList.toArray(xmlBeanArray);
        this.content = xmlBeanArray;

    }

    public XmlReply(Message messageBean, XmlResponse... contentArray) {
        this.acknowledgment = new Acknowledgement(Acknowledgement.ACK_OK);
        this.message = messageBean;
        this.content = contentArray;
    }

    public XmlReply(int status) throws IllegalArgumentException {
        try {
            this.acknowledgment = new Acknowledgement(status);
        } catch (IllegalArgumentException e) {
            log.debug(e.getMessage());
            throw e;
        }
    }

    public XmlReply(int status, Error error) throws IllegalArgumentException {
        try {
            this.acknowledgment = new Acknowledgement(status);
            this.errorBean = error;
        } catch (IllegalArgumentException e) {
            log.debug(e.getMessage());
            throw e;
        }
    }

    public XmlReply(int status, ErrorEnum errorEnum) throws IllegalArgumentException {
        try {
            this.acknowledgment = new Acknowledgement(status);
            this.errorEnum = errorEnum;
        } catch (IllegalArgumentException e) {
            log.debug(e.getMessage());
            throw e;
        }
    }

    public XmlReply(int status, Error error, Message messageBean) throws IllegalArgumentException {
        try {
            this.acknowledgment = new Acknowledgement(status);
            this.errorBean = error;
            this.message = messageBean;
        } catch (IllegalArgumentException e) {
            log.debug(e.getMessage());
            throw e;
        }
    }

    public XmlReply(int status, ErrorEnum errorEnum, Message messageBean) throws IllegalArgumentException {
        try {
            this.acknowledgment = new Acknowledgement(status);
            this.errorEnum = errorEnum;
            this.message = messageBean;
        } catch (IllegalArgumentException e) {
            log.debug(e.getMessage());
            throw e;
        }
    }

    public XmlReply(int status, Message messageBean) throws IllegalArgumentException {
        try {
            this.acknowledgment = new Acknowledgement(status);
            this.message = messageBean;
        } catch (IllegalArgumentException e) {
            log.debug(e.getMessage());
            throw e;
        }
    }

    public XmlReply(int status, XmlResponse... contentArray) throws IllegalArgumentException {
        try {
            this.acknowledgment = new Acknowledgement(status);
            this.content = contentArray;
        } catch (IllegalArgumentException e) {
            log.debug(e.getMessage());
            throw e;
        }
    }

    public void setAcknowledgment(Acknowledgement acknowledgment) {
        this.acknowledgment = acknowledgment;
    }

    public Acknowledgement getAcknowledgment() {
        return acknowledgment;
    }

    public void setContent(XmlResponse[] content) {
        this.content = content;
    }

    public Object[] getContent() {
        return content;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public Message getMessage() {
        return message;
    }

    /**
     * @return the errorEnum
     */
    public ErrorEnum getErrorEnum() {
        return errorEnum;
    }

    /**
     * @param errorEnum the errorEnum to set
     */
    public void setErrorEnum(ErrorEnum errorEnum) {
        this.errorEnum = errorEnum;
    }

    public String toXML() {
        XStream stream = new XStream();
        return stream.toXML(this);
    }
}
