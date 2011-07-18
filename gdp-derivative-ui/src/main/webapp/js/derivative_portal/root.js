var LOG;

$(document).ready(function() {
    initializeLogging();
});

function initializeLogging() {
    LOG = log4javascript.getLogger();
    var layout = new log4javascript.PatternLayout("%rms - %d{HH:mm:ss.SSS} %-5p - %m%n");
    var appender = new log4javascript.BrowserConsoleAppender();
    appender.setLayout(layout);
    LOG.addAppender(appender);
    LOG.info('Derivative Portal: Logging initialized.');
}

Ext.onReady(function () {
	
	new Ext.Viewport({
		renderTo : document.body
	});
	
});