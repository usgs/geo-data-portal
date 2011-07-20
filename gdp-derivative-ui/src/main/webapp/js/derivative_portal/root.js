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
    
    var map = new OpenLayers.Map();
    
    var baseLayer = new OpenLayers.Layer.WMS(
        "Global Imagery",
        "http://maps.opengeo.org/geowebcache/service/wms",
        {
            layers: "bluemarble"
        }
        );
            
    var threddsLayer = new OpenLayers.Layer.WMS(
        "tavg",
        "http://igsarm-cida-thredds1.er.usgs.gov:8080/thredds/wms/gmo/GMO_w_meta.ncml",
        {
            transparent: true,
            layers: "Tmax"
        },{
            opacity : 0.5
        }
        );
            
    map.addLayers([baseLayer, threddsLayer]);
	
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
    
    capabilitiesStore.on('load', function(capStore, records) {
        mapPanel.map.zoomToExtent(
            OpenLayers.Bounds.fromArray(capStore.getAt(0).get("llbbox"))
            );
    });
    
    var configPanel = new Ext.Panel({
        width : 'auto',
        region: 'west',
        items : [{
            xtype : 'combo',
            mode : 'local',
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
                    copy.get('layer')['opacity'] = 0.2;
                    mapPanel.layers.removeAt(1);
                    mapPanel.layers.add(copy);
                    mapPanel.map.zoomToExtent(
                        OpenLayers.Bounds.fromArray(copy.get("llbbox"))
                        );
                }
            }
        }]
    });
    

    var legendPanel = new GeoExt.LegendPanel({
        defaults: {
            //            preferredTypes : 'simple',
            style: 'padding: 0 5px 5px 5px'
        },
        border : false,
        layerStore: mapPanel.layers,
        filter : function(record) {
            return !(record.getLayer().isBaseLayer);
        },
        autoScroll: true
    });
    
    var legendWindow = new Ext.Window({
        border : false,
        frame : false,
        closable: false,
        resizable : false,
        items : [legendPanel]
    });
	
    mapPanel.on('afterlayout', function() {
        LOG.debug('mapPanel afterlayout hit');
        legendWindow.show(null, function() {
            LOG.info('Legend window show callback hit');
            //TODO, I hate myself for this, but here are some magic numbers
            // legendpanel's afterlayout gets called before it's actually done rendering everything
            // so this is a hack to make at least some of it visible
            legendWindow.alignTo(mapPanel.getEl(), "br-br", [-110,-264]); 
        });
    });
	
    var viewport = new Ext.Viewport({
        renderTo : document.body,
        items : [mapPanel, configPanel],
        layout: 'border'
            
    });
    
    viewport.on('afterlayout', function() {
        LOG.debug('viewport afterlayout hit');
        legendWindow.alignTo(mapPanel.getEl(), "br-br");
    });
    
    LOG.info('Derivative Portal: Mapping initialized.');
	
    
}

