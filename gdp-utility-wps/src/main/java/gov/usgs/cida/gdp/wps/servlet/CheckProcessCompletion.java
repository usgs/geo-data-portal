package gov.usgs.cida.gdp.wps.servlet;

import gov.usgs.cida.gdp.communication.EmailHandler;
import gov.usgs.cida.gdp.communication.EmailMessage;
import gov.usgs.cida.gdp.constants.AppConstant;
import gov.usgs.cida.gdp.utilities.HTTPUtils;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author jwalker
 */
public class CheckProcessCompletion {
	static org.slf4j.Logger log = LoggerFactory.getLogger(CheckProcessCompletion.class);
    private static final long serialVersionUID = 1L;
	private static CheckProcessCompletion singleton = null;

	private Timer timer;
	private long recheckTime;

	private CheckProcessCompletion() {
		timer = new Timer("ProcessEmailCheck", true);
		recheckTime = Long.parseLong(AppConstant.CHECK_COMPLETE_MILLIS.toString());
	}

	public static CheckProcessCompletion getInstance() {
		if (singleton == null) {
			singleton = new CheckProcessCompletion();
		}
		return singleton;
	}

	public void addProcessToCheck(String wpsCheckPoint, String emailAddr) {
		timer.scheduleAtFixedRate(new EmailCheckTask(wpsCheckPoint, emailAddr), 0l, recheckTime);
		cleanupTimer();
	}

	public void cleanupTimer() {
		int purged = timer.purge();
		log.debug("Purged " + purged + " tasks from timer.");
	}

	public static Document parseDocument(InputStream is) throws IOException, SAXException, ParserConfigurationException {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		documentBuilderFactory.setNamespaceAware(true);
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document document = documentBuilder.parse(is);
		return document;
	}
}

class EmailCheckTask extends TimerTask {
	static org.slf4j.Logger log = LoggerFactory.getLogger(EmailCheckTask.class);
    private static final long serialVersionUID = 1L;

	private String wpsCheckPoint;
	private String addr;
	private final String taskStarted;

	public EmailCheckTask(String wpsCheckPoint, String emailAddr) {
		this.wpsCheckPoint = wpsCheckPoint;
		this.addr = emailAddr;
		Date now = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		this.taskStarted = sdf.format(now);
	}

	@Override
	public void run() {
		try {
			InputStream is = HTTPUtils.sendPacket(new URL(wpsCheckPoint), "GET");
			Document document = CheckProcessCompletion.parseDocument(is);
			checkAndSend(document);
		}
		catch (Exception ex) {
			String error = "Error in process checking/sending email: " + ex.getMessage();
			log.error(error);
			throw new RuntimeException(error);
		}

	}

	public void checkAndSend(Document document) throws XPathExpressionException, AddressException, MessagingException {

		ProcessStatus procStat = new ProcessStatus(document);
		if (procStat.isAccepted()) {
			log.debug("Process accepted at " + taskStarted);
		}
		else if (procStat.isStarted()) {
			log.debug("Process started (" + taskStarted + ") currently running");
		}
		else if (procStat.isPaused()) {
			log.debug("Process paused (started " + taskStarted + ")");
		}
		else if (procStat.isSuccess()) {
			log.debug("Process (started " + taskStarted + ") complete, sending email");
			sendCompleteEmail(procStat.getOutputReference());
			this.cancel();
		}
		else if (procStat.isFailed()) {
			log.debug("Processing (started " + taskStarted + ") failed, sending email");
			sendFailedEmail(procStat.getFailureMessage());
			this.cancel();
		}
		else {
			log.debug("Status not valid, something went wrong");
		}
	}

	private void sendCompleteEmail(String fileLocation) throws AddressException, MessagingException {
		String from = AppConstant.FROM_EMAIL.toString();
		String subject = "Processing Complete";
		String content = "The processing has completed on your request."
				+ " You can retrieve your file at " + fileLocation;
		List<String> bcc = new ArrayList<String>();
		String bccAddr = AppConstant.TRACK_EMAIL.toString();
		if (!"".equals(bccAddr)) {
			bcc.add(bccAddr);
		}

		EmailMessage message = new EmailMessage(from, addr, null, bcc, subject, content);
		EmailHandler.sendMessage(message);
	}

	private void sendFailedEmail(String errorMsg) throws AddressException, MessagingException {
		String from = AppConstant.FROM_EMAIL.toString();
		String subject = "Processing Failed";
		String content = "The processing has failed on your request."
				+ " The following errors occured: " + errorMsg;

		List<String> bcc = new ArrayList<String>();
		String bccAddr = AppConstant.TRACK_EMAIL.toString();
		if (!"".equals(bccAddr)) {
			bcc.add(bccAddr);
		}

		EmailMessage message = new EmailMessage(from, addr, null, bcc, subject, content);
		EmailHandler.sendMessage(message);
	}
}
