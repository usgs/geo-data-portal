var LOG;

function initializeLogging() {
    LOG = log4javascript.getLogger();
    var layout = new log4javascript.PatternLayout("%rms - %d{HH:mm:ss.SSS} %-5p - %m%n");
    var appender = new log4javascript.BrowserConsoleAppender();
    appender.setLayout(layout);
    LOG.addAppender(appender);
    LOG.info('Derivative Portal: Logging initialized.');
}

Ext.onReady(function () {
	
    initializeLogging();
        
    var map = new OpenLayers.Map();
    var baseLayer = new OpenLayers.Layer.WMS(
        "Global Imagery",
        "http://isse.cr.usgs.gov/ArcGIS/services/Combined/SDDS_Imagery/MapServer/WMSServer",
               
        {
            layers: "0"
        }
        );
    var threddsLayer = new OpenLayers.Layer.WMS(
        "...",
        "http://igsarm-cida-thredds1.er.usgs.gov:8080/thredds/wms/gmo/GMO_w_meta.ncml",
        {
            transparent: 'true',
            isBaseLayer: false,
            layers: "Prcp"
        },{
            opacity : 0.5
        }
        );
            
    map.addLayers([baseLayer, threddsLayer]);
    map.setBaseLayer(baseLayer);
    map.setLayerIndex(threddsLayer, 1);
//    map.raiseLayer(threddsLayer, 1);
//    map.addLayers([threddsLayer,layer]);
//    map.addLayer(threddsLayer);

    var mapPanel = new GeoExt.MapPanel({
        //            renderTo: 'gxmap',
        height: 400,
        width: 600,
        map: map,
        title: 'A Simple GeoExt Map'
    });
        
    new Ext.Viewport({
        renderTo : document.body,
        items : [mapPanel],
        layout: 'fit'
                
    });
	
});