package gov.usgs.cida.gdp.communication;

import gov.usgs.cida.gdp.constants.AppConstant;
import java.util.TimerTask;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

/**
 *
 * @author jwalker
 */
public class EmailCheckTask extends TimerTask {
        private String wpsCheckPoint;
        private String addr;

        @Override
		// might need to be moved to know about wps specific stuff
        public void run() {
			try {
				sendCompleteEmail("test");
				this.cancel();
			}
			catch (Exception e) {
				throw new RuntimeException("Something went wrong with your email");
			}
            /*log.info("Checking for Processing Completion... ");
			if (WPSJob == processing) {
				log.info("Still processing job");
			}
			else if (WPSJob == done) {
				sendCompleteEmail(resultLocation);
				this.cancel();

			}
			else if (WPSJob == failed) {
				sendFailedEmail(errorMsg);
				this.cancel();
			}*/

        }

        public EmailCheckTask(String wpsCheckPoint, String emailAddr) {
			this.wpsCheckPoint = wpsCheckPoint;
			this.addr = emailAddr;
        }

		private void sendCompleteEmail(String fileLocation) throws AddressException, MessagingException {
			String from = AppConstant.FROM_EMAIL.toString();
			String subject = "Processing Complete";
			String content = "The processing has completed on your request." +
					" You can retrieve your file at " + fileLocation;

			EmailMessage message = new EmailMessage(from, addr, null, subject, content);
			EmailHandler.sendMessage(message);
		}

		private void sendFailedEmail(String errorMsg) throws AddressException, MessagingException {
			String from = AppConstant.FROM_EMAIL.toString();
			String subject = "Processing Failed";
			String content = "The processing has failed on your request." +
					" The following errors occured: " + errorMsg;

			EmailMessage message = new EmailMessage(from, addr, null, subject, content);
			EmailHandler.sendMessage(message);
		}

    }

