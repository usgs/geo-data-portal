var LOG;
var capabilitiesStore;

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
    capabilitiesStore = new GeoExt.data.WMSCapabilitiesStore({
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
            layers: "Tmax"
        },{
            opacity : 0.5
        }
        );
            
    map.addLayers([baseLayer, threddsLayer]);
    map.setBaseLayer(baseLayer);
    map.setLayerIndex(threddsLayer, 1);
    

    var mapPanel = new GeoExt.MapPanel({
        //            renderTo: 'gxmap',
        height: 400,
        width: 600,
        map: map,
        title: 'Geo Data Portal Derivative UI',
        center: new OpenLayers.LonLat(-96, 38),
        zoom : 4,
        getState: function() {
            var state = GeoExt.MapPanel.prototype.getState.apply(this);
            state.width = this.getSize().width;
            state.height = this.getSize().height;
            return state;
        },
        applyState: function(state) {
            GeoExt.MapPanel.prototype.applyState.apply(this, arguments);
            this.width = state.width;
            this.height = state.height;
        }
    });
        
    new Ext.Viewport({
        renderTo : document.body,
        items : [mapPanel],
        layout: 'fit'
            
    });
    LOG.info('Derivative Portal: Mapping initialized.');
}

