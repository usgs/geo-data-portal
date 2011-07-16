<%-- 
    Document   : feedback
    Created on : Jul 14, 2011, 9:31:39 AM
    Author     : Ivan Suftin <isuftin@usgs.gov>
--%>
<script type="text/javascript">
    var FEEDBACK = new function() {
        var _contactPopupContainer;
        var _commentField;
        var _captchaImageDiv;
        var _captchaImage;
        var _submitButton;
        var _captchaInput;
        var _resetSecurityImageButton;
        
        var _captchaValidationUrl = '${param["securityimageDir"]}/validatecaptcha/';
        var _commentSubmittalUrl = '${param["serviceDir"]}/contact/';
        var _serverErrorMessage = '${param["serverErrorMessage"]}';
        var _defaultComment = 'Please enter your comment here.';
        var _captchaFailureMessage = 'Either the text did not match the security image or it has expired.<br /> Please try again with a new security image.';
        
        return {
            init : function() {
                logger.info('GDP: Initializing Contact Form');
                _submitButton = $('#submit');
                _resetSecurityImageButton = $('#resetSecurityTextButton');
                _contactPopupContainer = $('#contact-popup-container');
                _commentField = $('#comment');
                _captchaImageDiv = $('#captchaImageDiv');
                _captchaImage = _captchaImageDiv.find('img');
                _captchaInput = $('#captcha');
                // Create button objects 
                _submitButton.button({'label' : 'Submit', 'disabled' : true});
                _resetSecurityImageButton.button({'label':'Reset Security Image'});
                
                // Set default text
                _commentField.val(_defaultComment);
                
                // Binds
                _resetSecurityImageButton.click(function() {
                    FEEDBACK.updateCaptchaImage();
                });
                
                _commentField.focus(function() {
                    if ($(this).val() == _defaultComment) {
                        $(this).val('');
                    }
                });
                
                _commentField.blur(function() {
                    if ($(this).val() == '') {
                        $(this).val(_defaultComment);
                    }
                });
                
                _commentField.keyup(function() {
                    if ($(this).val().trim()) {
                        _submitButton.button('option', 'disabled', false);
                    } else {
                        _submitButton.button('option', 'disabled', true);
                    }
                });
                
                _submitButton.click(function() {
                    FEEDBACK.submitComment();
                });
                
            },
            getCaptchaFailureMessage : function() {
                return _captchaFailureMessage;
            },
            getCaptchaValidationUrl : function() {
                return _captchaValidationUrl;
            },
            getCommentSubmittalUrl : function() {
                return _commentSubmittalUrl;
            },
            getServerErrorMessage : function() {
                return _serverErrorMessage;
            },
            popEmailPanel : function() {
                _contactPopupContainer.dialog({
                    title: 'User Feedback',
                    width: 'auto',
                    height: 'auto',
                    modal: true,
                    resizable: false,
                    draggable: true,
                    zIndex: 9999
                });
            },
            closeEmailPanel : function() {
                _contactPopupContainer.dialog('close');
            },
            updateCaptchaImage : function() {
                var cacheBreak =  new Date().getMilliseconds();
                _captchaImage.prop('src', '${param["securityimageDir"]}/getImage/?width=300&height=50&charsToPrint=6&circlesToDraw=30&cacheBreak=' + cacheBreak);
            },
            keyPressed : function(charCode) {
                if (charCode == 0) {
                    return false;
                }
            },
            submitComment : function() {
                var errorColor = '#FF0000';
                var emailAddress= $('#email').val();
                var emailError  = $('#emailError');
                var commentError= $('#commentError');
                var captchaError =$('#captchaError'); 
                var replyReq	= $('#emailResponseRequired').prop('checked') ? true : false;
                var filter 	= /^.+@.+\..{2,3}$/;
                var result 	= true;
                var verified 	= false;
                
                emailError.val('');
                
                // Check EMail address if anything is entered or needs to be
                if (replyReq) {
                    if (filter.test(emailAddress)) {
                        emailError.html('');	
                    } else if (!filter.test(emailAddress) || emailAddress == '') {
                        emailError.html('* Please enter a valid E-Mail address or<br/>uncheck the reply required box.');	
                        emailError.css('color', errorColor);	
                        result = false;	
                    } 
                }

                // Check that a comment is entered
                if (!_commentField.val()) {
                    emailError.html('');
                    commentError.html('* Please enter a comment');	
                    commentError.css('color', errorColor);	
                    result = false;	
                }

                if (result) {
                    $.ajax( {
                        url : FEEDBACK.getCaptchaValidationUrl(),
                        type : 'get',
                        data : { "userCaptcha" :  _captchaInput.val()},
                        dataType : 'json',
                        cache : false,
                        success : function(data, textStatus, XMLHttpRequest) {
                            verified = (data["captcha"] == "true") ? true : false;
                            if (verified) {
                                logger.debug("GDP: Captcha verified");
                                captchaError.html('');
                                
                                $.ajax({
                                    url : FEEDBACK.getCommentSubmittalUrl(),
                                    method: 'post',
                                    dataType : 'json',
                                    data : { 
                                        "comments" : _commentField.val(),
                                        "email" : emailAddress,
                                        "replyrequired" : replyReq 
                                    },
                                    success: function(data, textStatus, XMLHttpRequest) {
                                        if (data['status'] == 'success'){
                                            logger.debug("GDP: Feedback e-mail sent");
                                            _commentField.val('');
                                            _captchaInput.val('');
                                            FEEDBACK.updateCaptchaImage();
                                            FEEDBACK.closeEmailPanel();
                                            showNotification("Feedback has been sent. If you entered an e-mail address, you will receive a copy.", false, null);
                                        } else {
                                            logger.debug("GDP: Feedback e-mail not sent");
                                            showWarningNotification("Sorry, submission failed. Please retype security text and try again. " + _serverErrorMessage);
                                            FEEDBACK.updateCaptchaImage();
                                            return false;
                                        }
                                    },
                                    error :  function(jqXHR, textStatus, errorThrown) {
                                        showWarningNotification(FEEDBACK.getCaptchaFailureMessage());
                                        FEEDBACK.updateCaptchaImage();
                                        return false;
                                    }
                                })
                                
                            } else {
                                logger.debug("GDP: Captcha verification failed");
                                captchaError.html(_captchaFailureMessage);
                            }
                            
                        },
                        error : function(jqXHR, textStatus, errorThrown) {
                            alert(FFEDBACK.getCaptchaFailureMessage());
                            FEEDBACK.updateCaptchaImage();
                            return false;
                        }
                    });
                } else {
                    return result;
                }
			
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
                                  rows="10" 
                                  cols="40" 
                                  name="comment">
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
                               type="button" 
                               value="Submit" />
                    </td>	   
                    <td>
                        <input id="resetSecurityTextButton"
                               type="button" 
                               value="Reset Security Image" />
                    </td>
                </tr>
            </table>
        </form>
    </div>

</div>
