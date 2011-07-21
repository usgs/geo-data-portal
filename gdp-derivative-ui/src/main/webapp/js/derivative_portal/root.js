var LOG;

Ext.onReady(function () {
    initializeLogging();
    initializeMapping();
});

function initializeLogging() {
    LOG = log4javascript.getLogger();
    var layout = new log4javascript.PatternLayout("%rms - %d{HH:mm:ss.SSS} %-5p - %m%n");
    var appender = new log4javascript.BrowserConsoleAppender();
    appender.setLayout(layout);
    LOG.addAppender(appender);
    LOG.info('Derivative Portal: Logging initialized.');
}

function initializeMapping() {
	
	var configPanel = new GDP.LayerChooser({
        width : 265,
        region: 'west',
        labelWidth: 80,
        defaults: {
            width: 180
        }
    });
	
    var mapPanel = new GDP.BaseMap({
        height: 400,
        width: 600,
		id : 'mapPanel',
        region: 'center',
        border: false,
		layerController : configPanel.controller,
        title: 'USGS Derived Downscaled Climate Portal'
    });
	
	var headerPanel = new Ext.Panel({
        region: 'north',
        height: 'auto',
        autoShow: true,
        contentEl: 'usgs-header-panel'
    });
    
    var footerPanel = new Ext.Panel({
        region: 'south',
        height: 'auto',
        autoShow: true,
        contentEl: 'usgs-footer-panel'
    });
	
    new Ext.Viewport({
        renderTo : document.body,
        items : [headerPanel, mapPanel, configPanel, footerPanel],
        layout: 'border'
    });
    
    LOG.info('Derivative Portal: Mapping initialized.');
	
    
}

