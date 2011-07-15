<%-- 
    Document   : feedback
    Created on : Jul 14, 2011, 9:31:39 AM
    Author     : Ivan Suftin <isuftin@usgs.gov>
--%>
<script type="text/javascript">
    var FEEDBACK = new function() {
        var _contactForm = '#contact-form';
        var _captchaImageDiv = '#captchaImageDiv';
        var _contactPopupContainer = '#contact-popup-container';
        
        var _captchaValidationUrl = '${param["securityimageDir"]}/validatecaptcha/';
        var _commentSubmittalUrl = '${param["serviceDir"]}/contact/';
        var _serverErrorMessage = '${param["serverErrorMessage"]}';
        var defaultComment = 'Please enter your comment here.';
        var captchaFailureMessage = 'Either the text did not match the security image or it has expired. Please try again with a new security image.';
        
        return {
            init : function() {
                logger.info('GDP: Initializing Contact Form');
                
            },
            popEmailPanel : function() {
                $(_contactPopupContainer).dialog({
                    title: 'User Feedback',
                    width: 'auto',
                    height: 'auto',
                    modal: true,
                    resizable: false,
                    draggable: true,
                    zIndex: 9999
                });
            }
        }
    }();
    
    $(document).ready(function() {
        FEEDBACK.init();
    })
</script>

<div id="contact-popup-container" class="hidden">

    <div id="contact-popup">

        <form id="contact-form" action="" method="post" name="commentForm">
            <table>
                <tr>
                    <td>
                        <label for="emailResponseRequired">Require response:</label>
                    </td>
                    <td>			
                        <input id="emailResponseRequired" type="checkbox" alt="Response Required Checkbox" name="emailResponseRequired" checked="checked" />
                    </td>
                </tr>
                <tr>
                    <td>
                        <label for="email">E-Mail (optional):</label>
                    </td>
                    <td>			
                        <input id="email" 
                               onkeypress="FEEDBACK.keyPressed(event.charCode);" 
                               type="text" 
                               name="email" 
                               size="30"/>
                    </td>
                </tr>
                <tr>
                    <td>&nbsp;</td>
                    <td>			
                        <span id="emailError"></span>
                    </td>
                </tr>
                <tr>
                    <td>
                        <label for="comment">Comment:</label>
                    </td>
                    <td>
                        <textarea id="comment"
                                  onkeypress="FEEDBACK.keyPressed(event.charCode);" 
                                  onfocus="FEEDBACK.commentFocus();"
                                  onblur="FEEDBACK.commentLostFocus();"
                                  onkeyup="FEEDBACK.commentEntry();" 
                                  rows="10" 
                                  cols="40" 
                                  name="comment">Please enter your comment here.
                        </textarea>
                    </td>
                </tr>
                <tr>
                    <td>&nbsp;</td>
                    <td>			
                        <span id="commentError"></span>
                    </td>
                </tr>
                <tr>
                    <td>
                        <label for="captcha">Security Text:</label>
                    </td>
                    <td>
                        <input id="captcha" 
                               onkeypress="FEEDBACK.keyPressed(event.charCode);"  
                               type="text" 
                               name="captcha" 
                               size="6" 
                               maxlength="6" />
                    </td>
                </tr>
                <tr>
                    <td>&nbsp;</td>
                    <td>
                        <div id="captchaImageDiv" >
                            <img id="captchaImage" alt="Security Image" src='${param["securityimageDir"]}/getImage/?width=300&height=50&charsToPrint=6&circlesToDraw=30' />
                        </div>
                    </td>
                </tr>
                <tr>
                    <td>&nbsp;</td>
                    <td>			
                        <span id="captchaError"></span>
                    </td>
                </tr>
                <tr>
                    <td>
                        <input id="submit"
                               onkeypress="function(key) { if (key.which == 13) FEEDBACK.submitComment(); }"
                               onclick="FEEDBACK.submitComment();" 
                               type="button" 
                               value="Submit" />
                    </td>	   
                    <td>
                        <input id="resetSecurityTextButton"
                               onclick="FEEDBACK.retrieveSecurityImage();" 
                               type="button" 
                               value="Reset Security Image" />
                    </td>
                </tr>
            </table>
        </form>
    </div>

</div>
