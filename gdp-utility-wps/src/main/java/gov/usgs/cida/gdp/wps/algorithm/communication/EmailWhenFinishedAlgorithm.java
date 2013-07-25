package gov.usgs.cida.gdp.wps.algorithm.communication;

import gov.usgs.cida.gdp.constants.AppConstant;
import gov.usgs.cida.gdp.wps.completion.CheckProcessCompletion;
import org.n52.wps.algorithm.annotation.Algorithm;
import org.n52.wps.algorithm.annotation.Execute;
import org.n52.wps.algorithm.annotation.LiteralDataInput;
import org.n52.wps.algorithm.annotation.LiteralDataOutput;
import org.n52.wps.server.AbstractAnnotatedAlgorithm;

@Algorithm(version="1.0.0")
public class EmailWhenFinishedAlgorithm extends AbstractAnnotatedAlgorithm {
    private static final long serialVersionUID = 1L;
    
    public final static String INPUT_EMAIL = "email";
    public final static String INPUT_WPS_STATUS = "wps-checkpoint";
    public final static String INPUT_CALLBACK_BASE_URL = "callback-base-url";
    public final static String OUTPUT_RESULT = "result";
	public final static Boolean BREAK_ON_SYSERR = Boolean.parseBoolean(AppConstant.BREAK_ON_SYSERR.getValue());
	public final static Boolean EMAIL_ON_SYSERR = Boolean.parseBoolean(AppConstant.EMAIL_ON_SYSERR.getValue());
	public final static Integer CHECK_PROC_ERR_LIMIT = Integer.parseInt(AppConstant.CHECK_PROC_ERR_LIMIT.getValue());
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
            CheckProcessCompletion.getInstance().addProcessToCheck(wpsStatusURL, email, callbackBaseURL, BREAK_ON_SYSERR, EMAIL_ON_SYSERR, CHECK_PROC_ERR_LIMIT);
            result = "OK: when " + wpsStatusURL + " is complete, an email will be sent to  '" + email + "'";
        } catch (Exception e) {
			throw new RuntimeException("Error: Unable to add process check timer");
        }
    }
}