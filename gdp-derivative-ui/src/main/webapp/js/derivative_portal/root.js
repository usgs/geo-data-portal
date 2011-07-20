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
	
    var capabilitiesStore = new GeoExt.data.WMSCapabilitiesStore({
        url : 'proxy/http://igsarm-cida-thredds1.er.usgs.gov:8080/thredds/wms/gmo/GMO_w_meta.ncml?service=WMS&version=1.1.1&request=GetCapabilities',
        storeId : 'capabilitiesStore'
    });
    capabilitiesStore.load();
    
	
    var mapPanel = new GDP.BaseMap({
        height: 400,
        width: 600,
        region: 'center',
        border: false,
        title: 'Geo Data Portal Derivative UI'
    });
    
    var layerCombo = new Ext.form.ComboBox({
        xtype : 'combo',
        mode : 'local',
        triggerAction: 'all',
        store : capabilitiesStore,
        forceSelection : true,
        lazyInit : false,
        displayField : 'title',
        listeners : {
            'select' : function(combo, record, index) {
                LOG.info("layerCombo select hit");
                mapPanel.replaceLayer(record);
            }
        }
    });
	
    var configPanel = new Ext.Panel({
        width : 'auto',
        region: 'west',
        items : [
        layerCombo
        ]
    });
	
    capabilitiesStore.on('load', function(capStore, records) {
        var firstIndex = 0;
        var firstRecord = capStore.getAt(firstIndex);
        layerCombo.setValue(firstRecord.get("title"));
        layerCombo.fireEvent('select', layerCombo, firstRecord, 0);
    });
	
    //	var timestepStore = new Ext.data.ArrayStore({
    //		
    //	});
    //	
    //	var timestepPanel = new Ext.Panel({
    //		region : 'north',
    //		border : false,
    //		height : 'auto',
    //		items : [
    //			{
    //				xtype : 'combo',
    //				mode : 'local',
    //				triggerAction: 'all',
    //				store : timestepStore,
    //				displayField : 'title',
    //				listeners : {
    //					'select' : function(combo, record, index) {
    //						mapPanel.replaceLayer(layerCombo, {"time" : record.get("time")});
    //					}
    //				}
    //			}
    //		]
    //	});
	
    new Ext.Viewport({
        renderTo : document.body,
        items : [mapPanel, configPanel],
        layout: 'border'
    });
    
    LOG.info('Derivative Portal: Mapping initialized.');
	
    
}

