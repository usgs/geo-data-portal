package gov.usgs.cida.gdp.utilities.bean;

import java.util.List;

import org.slf4j.LoggerFactory;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("reply")
public class XmlReplyBean implements XmlBean {

	private static org.slf4j.Logger log = LoggerFactory.getLogger(XmlReplyBean.class);

	private AckBean acknowledgment;
	@SuppressWarnings("unused")
	private ErrorBean errorBean;
        private ErrorEnum errorEnum;
	private MessageBean message;
	private XmlBean[] content;

	public XmlReplyBean() {
		this.acknowledgment = new AckBean(AckBean.ACK_OK);
	}

	public XmlReplyBean(XmlBean... contentArray) {
		this.acknowledgment = new AckBean(AckBean.ACK_OK);
		this.content = contentArray;
	}

	public XmlReplyBean(int result, List<XmlBean> xmlBeanList) {
		this.acknowledgment = new AckBean(result);
		XmlBean[] xmlBeanArray = new XmlBean[0];
		xmlBeanArray = xmlBeanList.toArray(xmlBeanArray);
		this.content = xmlBeanArray;

	}

	public XmlReplyBean(MessageBean messageBean, XmlBean... contentArray) {
		this.acknowledgment = new AckBean(AckBean.ACK_OK);
		this.message = messageBean;
		this.content = contentArray;
	}

	public XmlReplyBean(int status) throws IllegalArgumentException {
		try {
			this.acknowledgment = new AckBean(status);
		} catch (IllegalArgumentException e) {
			log.debug(e.getMessage());
			throw e;
		}
	}

	public XmlReplyBean(int status, ErrorBean error) throws IllegalArgumentException {
		try {
			this.acknowledgment = new AckBean(status);
			this.errorBean = error;
		} catch (IllegalArgumentException e) {
			log.debug(e.getMessage());
			throw e;
		}
	}

	public XmlReplyBean(int status, ErrorEnum errorEnum) throws IllegalArgumentException {
		try {
			this.acknowledgment = new AckBean(status);
			this.errorEnum = errorEnum;
		} catch (IllegalArgumentException e) {
			log.debug(e.getMessage());
			throw e;
		}
	}

	public XmlReplyBean(int status, ErrorBean error, MessageBean messageBean) throws IllegalArgumentException {
		try {
			this.acknowledgment = new AckBean(status);
			this.errorBean = error;
			this.message = messageBean;
		} catch (IllegalArgumentException e) {
			log.debug(e.getMessage());
			throw e;
		}
	}

        public XmlReplyBean(int status, ErrorEnum errorEnum, MessageBean messageBean) throws IllegalArgumentException {
		try {
			this.acknowledgment = new AckBean(status);
			this.errorEnum = errorEnum;
			this.message = messageBean;
		} catch (IllegalArgumentException e) {
			log.debug(e.getMessage());
			throw e;
		}
	}

	public XmlReplyBean(int status, MessageBean messageBean) throws IllegalArgumentException {
		try {
			this.acknowledgment = new AckBean(status);
			this.message = messageBean;
		} catch (IllegalArgumentException e) {
			log.debug(e.getMessage());
			throw e;
		}
	}

	public XmlReplyBean(int status, XmlBean... contentArray) throws IllegalArgumentException {
		try {
			this.acknowledgment = new AckBean(status);
			this.content = contentArray;
		} catch (IllegalArgumentException e) {
			log.debug(e.getMessage());
			throw e;
		}
	}

	@Override
	public String toXml() {
		XStream xstream = new XStream();
		xstream.autodetectAnnotations(true);

		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		sb.append(xstream.toXML(this));
		return sb.toString();
	}


	public void setAcknowledgment(AckBean acknowledgment) {
		this.acknowledgment = acknowledgment;
	}


	public AckBean getAcknowledgment() {
		return acknowledgment;
	}


	public void setContent(XmlBean[] content) {
		this.content = content;
	}


	public Object[] getContent() {
		return content;
	}


	public void setMessage(MessageBean message) {
		this.message = message;
	}


	public MessageBean getMessage() {
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

}
