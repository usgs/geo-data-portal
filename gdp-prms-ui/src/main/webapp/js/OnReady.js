if (Ext.isIE) { // http://www.mail-archive.com/users@openlayers.org/msg01838.html
    document.namespaces;
}

Ext.onReady(function() {
    initializeLogging();
    initializeAjax();
	
    var bodyPanel = new Ext.Panel({
        id: 'body-panel',
        region: 'center',
        layout: 'fit',
        border: false,
        items : [
        new Ext.Panel({
            items : [
            new PRMS.WPSConfigPanel({
                title : 'Launch Process',
                region : 'center',
                width : '30%',
                'wfs-url' : CONFIG.GEOSERVER_URL ,
                'wps-processing-url' : CONFIG.WPS_PROCESS_URL
            }),
            new PRMS.FileUploadPanel({
                title : 'Upload Shapefile',
                region : 'south',
                width : '30%'
            })
            ]
        })
        ]
    })
        
    var headerPanel = new Ext.Panel({
        id: 'header-panel',
        region: 'north',
        height: 'auto',
        border : false,
        autoShow: true,
        contentEl: 'usgs-header-panel'
    });
    var footerPanel = new Ext.Panel({
        id: 'footer-panel',
        region: 'south',
        height: 'auto',
        border : false,
        autoShow: true,
        contentEl: 'usgs-footer-panel'
    });
	
    VIEWPORT = new Ext.Viewport({
        renderTo : document.body,
        layout : 'border',
        items : [
        headerPanel,
        bodyPanel,
        footerPanel
        ]
    });
});

function initializeAjax() {
    
    Ext.Ajax.addEvents(
        "ajax-request-firing",
        "ajax-requests-complete",
        "ajax-request-exception"
        );
        
    Ext.Ajax.on('beforerequest', function(connection, options) {
        if (!Ext.Ajax.isLoading()) {
            Ext.Ajax.fireEvent('ajax-request-firing', 
            {
                connection : connection, 
                options : options
            });
        }
    }, this);
    Ext.Ajax.on('requestcomplete', function(connection, response, options) {
        if (!Ext.Ajax.isLoading()) {
            Ext.Ajax.fireEvent('ajax-requests-complete', 
            {
                connection : connection, 
                response : response, 
                options : options
            });
        }
    }, this);
    Ext.Ajax.on('requestexception', function(connection, response, options) {
        LOG.error(response);
        if (!Ext.Ajax.isLoading()) {
            Ext.Ajax.fireEvent('ajax-request-exception', 
            {
                connection : connection, 
                response : response, 
                options : options
            });
        }
    }, this);
}