if (Ext.isIE) { // http://www.mail-archive.com/users@openlayers.org/msg01838.html
    document.namespaces;
}

Ext.onReady(function() {
    initializeLogging();
    initializeAjax();
	
    var bodyPanel = new Ext.Panel({
        id: 'body-panel',
        region: 'center',
        border: false,
        items : [
        new PRMS.FileUploadPanel({
            region : 'center',
            width : 'auto'
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