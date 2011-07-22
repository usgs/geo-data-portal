var MAXIMUM_STEPS = 2; 
var AJAX_TIMEOUT = 5 * 60 * 1000;
var CURRENT_STEP = 0;
var MINIMUM_MAP_HEIGHT = 400; //px

var BOTTOM_DIV = '#bottom-div'
var FOOTER = '#footer';
var HEADER = '#header';
var MAP_DIV = '#map-div';
var NEXT_BUTTON = '#next-button';
var OVERLAY = '#overlay';
var OVERLAY_CONTENT = '#overlay-content';
var PREV_BUTTON = '#prev-button';
var STEPS_HEADER = '#steps-header';
var STEP_HEADER_CLASS = '.step-header';
var STEPS_CONTENT = '#steps-content';
var STEP_CONTENT_CLASS = '.step-content';
var STEPS_TOP = '#steps-top';
var THROBBER = '#throbber';

var logger;
var steps;
var AOI;
var Dataset;
var Submit;
var Constant;
var Algorithm;
var CSWClient;
var WFS;
var WPS;

$(document).ready(function() {
    try {
        initializeLogging();

        if (!init()) logger.error("GDP: A non-fatal error occured while loading the application.");
        else logger.debug("GDP: Application initialized successfully.");
    } catch(err) {
        handleException(err);
    }
})



function init() {
    logger.info("GDP: Beginning application initialization.");
    
    $(window).load(function() {
        // initMap() has to be done here instead of in $(document).ready due to IE8 bug
        initMap();
        removeOverlay();
    })
    
    // If we wish to modify when this gets called based on the event (reload? link click?),
    // check http://geekswithblogs.net/renso/archive/2009/09/21/how-to-stop-browser-closing.aspx
    // Ref: http://internal.cida.usgs.gov/jira/browse/GDP-358
    window.onbeforeunload = function() {
        return "Leaving the Geo Data Portal will cause any unsaved configuration to be lost.";
    }
    
    // Add htmlDecode function to the JS String object
    String.prototype.htmlDecode = function() {
        var e = document.createElement('div');
        e.innerHTML = this;
        return e.childNodes[0].nodeValue;
    }
    
    return initializeOverlay() && 
        initializeAjax() &&
        initializeSteps() &&
        initializeView();
}

function initializeLogging() {
    logger = log4javascript.getLogger();
    var layout = new log4javascript.PatternLayout("%rms - %d{HH:mm:ss.SSS} %-5p - %m%n");
    var appender = new log4javascript.BrowserConsoleAppender();
    appender.setLayout(layout);
    logger.addAppender(appender);
    logger.info('GDP: Logging initialized.');
}

function initializeOverlay() {
    logger.info("GDP: Initializing overlay.");
    
    logger.trace("GDP: Adding CSS rules to the overlay content");
    $(OVERLAY_CONTENT).css({
        "left": ($(window).width() / 2.5) + "px",
        "top": ($(window).height() / 2) + "px"
    });
    
    logger.trace("GDP: Adding text and image to overlay.");
    $(OVERLAY_CONTENT).append(
        "Loading...&nbsp;&nbsp;&nbsp;&nbsp;",
        $('<img></img>').attr('src','images/ajax-loader.gif')
    );
        
    return true;
}

function removeOverlay() {
    $(OVERLAY).fadeOut(Constant.ui.fadespeed, function() {
        logger.info('GDP: Application initialization has completed. Removing overlay.');
        $(OVERLAY).remove();
        
        if (parseInt(Constant.ui.view_popup_info) && Constant.ui.view_popup_info_txt.length > 0) {
            logger.trace('GDP: Showing popup to user');
            createPopupView(Constant.ui.view_popup_info_txt);
        }
    });
    return true;
}

function initializeSteps() {
    logger.info('GDP: Initializing steps (creating global JS objects)');
    Constant = new Constant(); // important that this gets initialized first
    WPS = WPS();
    WFS = WFS();
    Algorithm = new Algorithm();
    CSWClient = new CSWClient();
    AOI = new AOI();
    Dataset = new Dataset();
    
    Constant.init();
    Algorithm.init();
    CSWClient.init();
    
    steps = [AOI, Dataset];

    logger.debug('GDP: Moving all steps content into respective page content sections');
    for (var stepsCounter = 0; stepsCounter < steps.length; stepsCounter++) {
        var htmlID = '#' + steps[stepsCounter].htmlID;
        
        $(STEPS_HEADER).append($(htmlID + ' ' + STEP_HEADER_CLASS));
        $(STEPS_CONTENT).append($(htmlID + ' ' + STEP_CONTENT_CLASS));

        steps[stepsCounter].init();
    }
    
    return true;
}

/**
 * Brings the entire view together and initializes the elements throughout the
 * steps
 */
function initializeView() {
    logger.info('GDP: Initializing view.');
    $(window).resize(function() {resizeElements()});
    
    loadStep(0);
    
    // Hide all elements with class 'hidden'. Do this here instead of in css
    // so that each element's display attribute is correctly restored to what
    // it was before being hidden (handled by jQuery's hide and show methods).
    $('.hidden').each(function(index, element) {$(element).hide()});

    // This sets all 'button' elements to JQuery UI Buttons
    $('.directionArrow').button();
    $(PREV_BUTTON).button({'label' : 'Back'});
    $(NEXT_BUTTON).button({
        'label' : 'Next',
        'disabled' : true
    });

    initializeTips();

    initializeThrobbers();
    
    resizeElements();

    initializePrevNextButtons();
        
    $('#show-info-link').click(function() { createPopupView(Constant.ui.view_popup_info_txt, true) })
    return true;
}

function sortListbox(listbox) {
    var $r = $(listbox + ' option');
    $r.sort(function(a, b) {
        var _a = a.text;
        var _b = b.text;
        if (!isNaN(_a)) _a = +_a;
        if (!isNaN(_b)) _b = +_b;
        if (_a < _b) return -1;
        if (_a == _b) return 0;
        return 1;
    });
    $($r).remove();
    $(listbox).append($($r));

}

function initializeAjax() {
    logger.info("GDP: Initializing AJAX timeout functionality. Setting to: " + AJAX_TIMEOUT + "ms");
    $.ajaxSetup({
        timeout: AJAX_TIMEOUT,
        error: function(jqXHR, textStatus, errorThrown) {
            logger.error('A communication error has occured.'
                + '\nStatus Text: ' + jqXHR.statusText 
                + '\nStatus Code: ' + textStatus
                + '\nError Thrown: ' + errorThrown
                + '\nURL Attempted: ' + this.url
                + '\nData: ' + this.data
                + '\nMethod: ' + this.type);
            
            showErrorNotification('A communication error has occured.'
                + '<br />Status Text: <span style="color:#feff88;">' + jqXHR.statusText +'</span>'
                + '<br />Status Code: <span style="color:#feff88;">' + textStatus +'</span>'
                + '<br />Error Thrown: <span style="color:#feff88;">' + errorThrown +'</span>'
                + '<br /><br />Check your browser console for more information.');
            
            hideThrobber();
        }
    });
    return true;
}

/**
 * We have a wizard-type UI. This function sets up how the forward and previous
 * buttons operate
 */
function initializePrevNextButtons() {
    $(PREV_BUTTON).click(function() {
        logger.trace("GDP: User is moving back to step " + (CURRENT_STEP - 1))
        if (CURRENT_STEP - 1 >= 0) loadStep(CURRENT_STEP - 1);
    });

    $(NEXT_BUTTON).click(function() {
        logger.trace("GDP: User is moving forward to step " + (CURRENT_STEP + 1))
        if (CURRENT_STEP + 1 < steps.length) loadStep(CURRENT_STEP + 1);
    });
}

/**
 * Set up the way the throbbers behave throughout the UI 
 */
function initializeThrobbers() {
    // Show THROBBER when doing ajax request
    $(THROBBER).ajaxSend(function(){
        showThrobber();
    });
    // Hide when all ajax requests are finished
    $(THROBBER).ajaxStop(function() {
        hideThrobber();
    });
    
    $(THROBBER).ajaxError(function(event, XMLHttpRequest, ajaxOptions, thrownError) {
        showErrorNotification('Error communicating with ' + ajaxOptions.url);
        hideThrobber();
    });
}

/**
 * Set up the tooltip functionality throughout the UI
 */
function initializeTips() {
    logger.info("GDP: Initializing Tips.");
    $(".tooltip img").hover(
        function() {$(this).attr('src', 'images/question-mark-hover.png')},
        function() {$(this).attr('src', 'images/question-mark.png')}
    );
    $(".tooltip").tipTip({
        'maxWidth': '50%',
        'delay': 100,
        'fadeIn' : Constant.ui.fadeSpeed,
        'fadeOut' : Constant.ui.fadeSpeed,
        'fadeOutDelay' : Constant.ui.tip.fadeout.timeout,
        'keepAlive' : true,
        'activation' : 'click',        
        'enter': function() { // keep alive
            logger.trace('Showing tip');
            $("#tiptip_content").css('text-align', 'left');
        }
    });
}


function loadStep(stepNum) {
    // Steps are 0-indexed
    // Step headers and content are put into main html by their step order.
    // So to load a step, get the step HEADER and content at the step's index.
    
    // Initialize the step's loading procedure if there is any. 
    // If we get into an issue, we stop here
    if (!steps[stepNum].stepLoading()) return false;
    logger.debug('GDP: Loading step ' + stepNum);
    // Hide current step
    $(STEPS_HEADER + ' ' + STEP_HEADER_CLASS).eq(CURRENT_STEP).fadeOut(Constant.ui.fadespeed);
    $(STEPS_CONTENT + ' ' + STEP_CONTENT_CLASS).eq(CURRENT_STEP).fadeOut(Constant.ui.fadespeed);

    // Show next step
    $(STEPS_HEADER + ' ' + STEP_HEADER_CLASS).eq(CURRENT_STEP).queue(function() {
        $(STEPS_HEADER + ' ' + STEP_HEADER_CLASS).eq(stepNum).fadeIn(Constant.ui.fadespeed);
        $(this).dequeue(); // Any function put on the queue must call this
    });

    $(STEPS_CONTENT + ' ' + STEP_CONTENT_CLASS).eq(CURRENT_STEP).queue(function() {
        $(STEPS_CONTENT + ' ' + STEP_CONTENT_CLASS).eq(stepNum).fadeIn(Constant.ui.fadespeed);
        $(this).dequeue();
    });

    CURRENT_STEP = stepNum;
    
    if (CURRENT_STEP == 0) $(PREV_BUTTON).fadeOut(500);
    else $(PREV_BUTTON).fadeIn(500);
    
    if (CURRENT_STEP == steps.length - 1) $(NEXT_BUTTON).fadeOut(500);
    else $(NEXT_BUTTON).fadeIn(500);
    
    return true;
}

function showIntro() {
    $.colorbox({
        href: 'jsp/intro.jsp',
        width: '600px',
        transition: 'none'
    });
}
function resizeElements() {
    logger.trace("The application window is resizing. Resizing all elements to fit.");
    resizeMapDiv()
}

function resizeMapDiv() {
    var windowHeight = $(window).height();
    var bottomDivHeight = $(BOTTOM_DIV).outerHeight(true);
    var headerHeight = $(HEADER).outerHeight(true);
    var footerHeight = $(FOOTER).outerHeight(true);
    
    logger.trace('GDP: Calculating map height.');
    var mapDivHeight = (windowHeight - (bottomDivHeight + headerHeight + footerHeight) - 2 < MINIMUM_MAP_HEIGHT) ? 
        MINIMUM_MAP_HEIGHT : 
        windowHeight - (bottomDivHeight + headerHeight + footerHeight) - 2;
    logger.trace('GDP: Setting map height to ' + mapDivHeight + 'px');
    $(MAP_DIV).height(mapDivHeight);
}

function showThrobber() {$(THROBBER).fadeIn(Constant.ui.fadespeed);}

function hideThrobber() {$(THROBBER).fadeOut(Constant.ui.fadespeed);}

function showWarningNotification(message, sticky) {
    showNotification(message, sticky, 'theme-warning');
}

function showErrorNotification(message, sticky) {
    showNotification(message, sticky, 'theme-error');
}

function showNotification(message, sticky, themeStr) {
    $.jGrowl(message, {
        theme: (themeStr) ? themeStr : 'default',
        life: '10000',
        sticky: (sticky) ? sticky : false
    } );
    hideThrobber();
}

function showInformationalNotification(message, sticky)  {
    showNotification(message, sticky, 'theme-informational');
}

/**
 * @See http://internal.cida.usgs.gov/jira/browse/GDP-188
 */
function createPopupView(popupText, overrideCookie) {
    // Create the div
    var popupDiv = $(Constant.divString).attr('id', 'info_popup_modal_window').append(popupText);
    
    if (!overrideCookie) {
        if (cookie.get('gdp-hide-popup')) return true;
        $(popupDiv).append(
            $(Constant.divString).attr('id', 'info_popup_modal_window_hide_popup_option').append(
                "Do not show this again?",
                $(Constant.inputString).attr({
                    'type': 'checkbox',
                    'id' : 'dont-show-again-check'
                })
            )
        )
        $('#dont-show-again-check').live('change', function(action) {
            if (action.target.checked) {
                cookie.set('gdp-hide-popup', 'true', '');
            } else {
                cookie.del('gdp-hide-popup');
            }
        });
    }
    
    $('body').append(popupDiv);
    $('#info_popup_modal_window').dialog({
        buttons: {
            'OK' : function() {
                $(this).dialog("close");
                // Take the div out of the dom.
                // This is a one-time-only event
                $('#info_popup_modal_window').remove();
                return true;
            }
        },
        title: 'Geo Data Portal Information',
        width: 'auto',
        height: 'auto',
        modal: true,
        resizable: false,
        draggable: false,
        zIndex: 9999
    });
    return true;
}

function ajaxNoErrorNotification(options) {
    $(THROBBER).unbind('ajaxError');

    // complete is called after both error and success
    options['complete'] = initializeThrobbers; // rebind error notification
    
    $.ajax(options);
}

function handleException(err) {
    var name = (err.name) ? err.name : 'No Name';
    var message = (err.message) ? err.message : 'No Message';
    logger.fatal("An error ("+name+")was occured during application run. Presenting error dialog to user. Error follows: " + message);
    $('body').append(
        $('<div></div>').
            attr('id', 'fatal_error_window').
            html('An application error has occured: <br/>' + message + '\
                   <br /><br/>You may try reloading the application, continue using the application or \
                   <a href="mailto:gdp@usgs.gov?subject=Geo Data Portal Error Encountered Feedback&body=Error Encountered: %0A'+message+'%0A%0A"> \
                        contact the system administrator \
                   </a>. \
                   <br /><br />Warning: Continuing to use the application without reloading may cause instability and unexpected results.').
            addClass('hidden')
    );
        
    $('#fatal_error_window').dialog({
        buttons: {
            'RELOAD' : function() {
                $('#fatal_error_window').remove();
                window.location.reload();
            },
            'CONTINUE' : function() {
                $('#fatal_error_window').dialog('destroy');
                $('#fatal_error_window').remove();
            }
        },
        height: 'auto',
        hide: 'fade',
        modal: true,
        resizable: false,
        show: 'fade',
        title: 'Geo Data Portal Error Encountered',
        width: 'auto',
        zIndex: 9999
    });
}