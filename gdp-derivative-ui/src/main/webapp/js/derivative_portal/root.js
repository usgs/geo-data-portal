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
            ),
        new OpenLayers.Layer.XYZ(
            "Shaded Relief",
            "http://server.arcgisonline.com/ArcGIS/rest/services/ESRI_ShadedRelief_World_2D/MapServer/tile/${z}/${y}/${x}",
            {
                layers : "0"
            }
        ),
       new OpenLayers.Layer.XYZ(
            "Street Map",
            "http://server.arcgisonline.com/ArcGIS/rest/services/ESRI_StreetMap_World_2D/MapServer/tile/${z}/${y}/${x}",
            {
                layers : "0"
            }
        ), 
        ]
    });
	
    var mapPanel = new GDP.BaseMap({
        height: 400,
        width: 600,
		id : 'mapPanel',
        region: 'center',
        border: false,
        title: 'USGS Derived Downscaled Climate Portal',
        baseLayer: baseLayerStore.getAt(1)
    });
    
    var layerCombo = new Ext.form.ComboBox({
        xtype : 'combo',
        mode : 'local',
        triggerAction: 'all',
        store : capabilitiesStore,
        fieldLabel : 'Layer',
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
        
    var configPanel = new Ext.form.FormPanel({
        width : 265,
        region: 'west',
        labelWidth: 80,
        defaults: {
            width: 180
        },
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
    
    var headerPanel = new Ext.Panel({
        region: 'north',
        height: 'auto',
        autoShow: true,
        contentEl: 'usgs-header-panel'
    })
    
    var footerPanel = new Ext.Panel({
        region: 'south',
        height: 'auto',
        autoShow: true,
        contentEl: 'usgs-footer-panel'
    })
    
    new Ext.Viewport({
        renderTo : document.body,
        items : [headerPanel,mapPanel, configPanel,footerPanel],
        layout: 'border'
    });
    
    LOG.info('Derivative Portal: Mapping initialized.');
	
    
}

