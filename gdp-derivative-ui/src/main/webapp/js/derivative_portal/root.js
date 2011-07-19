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
        url : 'proxy/http://igsarm-cida-thredds1.er.usgs.gov:8080/thredds/wms/gmo/GMO_w_meta.ncml?service=WMS&version=1.3.0&request=GetCapabilities',
        storeId : 'whatever'
    });
    capabilitiesStore.load();
    
    var map = new OpenLayers.Map();
    
    var baseLayer = new OpenLayers.Layer.WMS(
        "Global Imagery",
        "http://maps.opengeo.org/geowebcache/service/wms",
        {
            layers: "bluemarble"
        }
        );
            
    var threddsLayer = new OpenLayers.Layer.WMS(
        "...",
        "http://igsarm-cida-thredds1.er.usgs.gov:8080/thredds/wms/gmo/GMO_w_meta.ncml",
        {
            transparent: true,
            layers: "Tavg"
        },{
            opacity : 0.5
        }
        );
            
    map.addLayers([baseLayer, threddsLayer]);
    
    var legendPanel = new GeoExt.LegendPanel({
        defaults: {
            preferredTypes : 'simple',
            style: 'padding: 5px'
        },
        width: 'auto',
        autoScroll: true,
        region: 'east'
    });
    
    var mapPanel = new GeoExt.MapPanel({
        height: 400,
        width: 600,
        region: 'center',
        border: false,
        map: map,
        title: 'Geo Data Portal Derivative UI',
        center: new OpenLayers.LonLat(-96, 38),
        zoom : 4
    });
    
    var cbConfigPanel = new Ext.Panel({
        width : 'auto',
        region: 'west',
        items : [{
            xtype : 'combo',
            mode : 'remote',
            triggerAction: 'all',
            store : capabilitiesStore,
            displayField : 'title',
            listeners : {
                'select' : function(combo, record, index) {
                    var copy = record.clone();
                    
                    copy.get('layer').mergeNewParams({
                        format: "image/png",
                        transparent : true
                    });
                    mapPanel.layers.removeAt(1);
                    mapPanel.layers.add(copy);
                    mapPanel.map.zoomToExtent(
                        OpenLayers.Bounds.fromArray(copy.get("llbbox"))
                    );
                }
            }
        }]
        
        
    });

    new Ext.Viewport({
        renderTo : document.body,
        items : [mapPanel, cbConfigPanel,legendPanel],
        layout: 'border'
            
    });
    
    LOG.info('Derivative Portal: Mapping initialized.');
}

