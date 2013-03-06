package gov.usgs.cida.gdp.wps.algorithm.communication;

import gov.usgs.cida.gdp.wps.completion.CheckProcessCompletion;
import org.n52.wps.algorithm.annotation.Algorithm;
import org.n52.wps.algorithm.annotation.Execute;
import org.n52.wps.algorithm.annotation.LiteralDataInput;
import org.n52.wps.algorithm.annotation.LiteralDataOutput;
import org.n52.wps.server.AbstractAnnotatedAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Algorithm(version="1.0.0")
public class EmailWhenFinishedAlgorithm extends AbstractAnnotatedAlgorithm {

    private final static Logger LOGGER = LoggerFactory.getLogger(EmailWhenFinishedAlgorithm.class);
    
    private static final long serialVersionUID = 1L;
    
    public final static String INPUT_EMAIL = "email";
    public final static String INPUT_WPS_STATUS = "wps-checkpoint";
    public final static String INPUT_CALLBACK_BASE_URL = "callback-base-url";
    public final static String OUTPUT_RESULT = "result";

    private String email;
    private String wpsStatusURL;
    private String callbackBaseURL;
    private String result;
    
    @LiteralDataInput(identifier="email")
    public void setEmail(String email) {
        this.email = email;
    }
    
    @LiteralDataInput(identifier="wps-checkpoint")
    public void setWPSStatusURL(String wpsStatusURL) {
        this.wpsStatusURL = wpsStatusURL;
    }
    
    @LiteralDataInput(identifier="callback-base-url")
    public void setCallbackBaseURL(String callbackBaseURL) {
        this.callbackBaseURL = callbackBaseURL;
    }
    
    @LiteralDataOutput(identifier="result")
    public String getResult() {
        return result;
    }
    
    @Execute
    public void runEmailChecker() {
        try {
            CheckProcessCompletion.getInstance().addProcessToCheck(wpsStatusURL, email, callbackBaseURL);
            result = "OK: when " + wpsStatusURL + " is complete, an email will be sent to  '" + email + "'";
        } catch (Exception e) {
			throw new RuntimeException("Error: Unable to add process check timer");
        }
    }
}