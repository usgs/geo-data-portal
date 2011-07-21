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
    
    var baseLayerStore = new GeoExt.data.LayerStore({
        layers : [
        new OpenLayers.Layer.WMS(
            "Blue Marble",
            "http://maps.opengeo.org/geowebcache/service/wms",
            {
                layers: "bluemarble"
            }
            ),
        new OpenLayers.Layer.WMS(
            "NAIP",
            "http://isse.cr.usgs.gov/ArcGIS/services/Combined/SDDS_Imagery/MapServer/WMSServer",
            {
                layers: "0"
            }
            )
        ]
    });
	
    var mapPanel = new GDP.BaseMap({
        height: 400,
        width: 600,
		id : 'mapPanel',
        region: 'center',
        border: false,
        title: 'Geo Data Portal Derivative UI',
        baseLayer: baseLayerStore.getAt(1)
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
                mapPanel.changeLayer(record);
            }
        }
    });
	
    var baseLayerCombo = new Ext.form.ComboBox({
        xtype : 'combo',
        mode : 'local',
        triggerAction: 'all',
        store : baseLayerStore,
        fieldLabel : 'Base Layer',
        forceSelection : true,
        lazyInit : false,
        displayField : 'title',
        listeners : {
            'afterrender' : function() {
                baseLayerCombo.setValue(baseLayerStore.getAt(1).getLayer().name);
            },
            'select' : function(combo, record, index) {
                mapPanel.replaceBaseLayer(record);
            }
        }
    });        
        
    var configPanel = new Ext.Panel({
        width : 'auto',
        region: 'west',
        items : [
            baseLayerCombo,
            layerCombo
        ]
    });
	
    capabilitiesStore.on('load', function(capStore, records) {
        var firstIndex = 0;
        var firstRecord = capStore.getAt(firstIndex);
        layerCombo.setValue(firstRecord.get("title"));
        layerCombo.fireEvent('select', layerCombo, firstRecord, 0);
    });
	
    new Ext.Viewport({
        renderTo : document.body,
        items : [mapPanel, configPanel],
        layout: 'border'
    });
    
    LOG.info('Derivative Portal: Mapping initialized.');
	
    
}

