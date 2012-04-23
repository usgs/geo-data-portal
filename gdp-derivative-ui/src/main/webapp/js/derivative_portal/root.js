var LOG;
var NOTIFY;
var LOADMASK;
// TODO- We may want to begin preloading images here. At least over a VPN connection
// it seems to take ages to load action/toolbar icons. This happens due to map tiles
// loading at the same time.

if (Ext.isIE) { // http://www.mail-archive.com/users@openlayers.org/msg01838.html
    document.namespaces;
} 

Ext.onReady(function () {
    initializeLogging();
    initializeNotification();
    initializeMapping();
    initializeQuickTips();
});

/**
 * http://log4javascript.org/
 */
function initializeLogging() {
    LOG = log4javascript.getLogger();
    
    var layout = new log4javascript.PatternLayout(GDP.LOG4JS_PATTERN_LAYOUT);
    
    var appender = new log4javascript.BrowserConsoleAppender();
    
    appender.setLayout(layout);
    //    Thresholds:
    //    log4javascript.Level.ALL
    //    log4javascript.Level.TRACE
    //    log4javascript.Level.DEBUG
    //    log4javascript.Level.INFO
    //    log4javascript.Level.WARN
    //    log4javascript.Level.ERROR
    //    log4javascript.Level.FATAL
    //    log4javascript.Level.OFF
    appender.setThreshold(log4javascript.Level.ALL);
    
    LOG.addAppender(appender);
    
    LOG.info('Derivative Portal: Log4javascript v.'+log4javascript.version+' initialized.');
}

/**
 * Usage example : 
 *      NOTIFY.error({
 *          msg : 'Message here'
 *      });
 */
function initializeNotification() {
    LOG.info('Derivative Portal: Initializing Notification.');
    
    var defaultConfig = {
        msgWidth: 400,
        hideDelay: 8000
    };
    
    /**
     * ERROR
     */
    var _notifyError = function(args) {
        LOG.trace('Derivative Portal: Showing error popup');
        var config = args || {};
	
        var moreInfo = new Ext.Button({
            text: 'More Info...'
        });
        
        var buttons = [];
        if (config.moreInfoAction) {
            buttons.push(moreInfo);
        }
        
        var notifyError = new Ext.ux.Notify(Ext.applyIf({
            title: 'Error!',
            titleIconCls: 'titleicon-error',
            msgIconCls: 'msgicon-error',
            msg: config.msg || 'An error has occured.',
            buttons: buttons
        }, defaultConfig));
		
        if (config.moreInfoAction) {
            moreInfo.on('click', function() {
                notifyError.hide();
                config.moreInfoAction();
            });
        }
		
        notifyError.show(document);
    }
	
    /**
     * SUCCESS
     */
    var _notifySuccess = function(msg) {
        LOG.trace('Derivative Portal: Showing success popup');
        new Ext.ux.Notify(Ext.applyIf({
            title: 'Success!',
            titleIconCls: 'titleicon-success',
            msg: msg.msg || 'Data saved successfully.'
        }, defaultConfig)).show(document);
    }
	
    /**
     * DEBUG NOTIFY
     */    
    var _notifyDebug = function(msg) {
        LOG.debug('Derivative Portal: Showing debug popup');
        new Ext.ux.Notify(Ext.applyIf({
            title: 'DEBUG',
            titleIconCls: 'titleicon-debug',
            msg: msg.msg || ''
        }, defaultConfig)).show(document);
    }
    
    /**
     * WARNING
     */
    var _notifyWarning = function(msg) {
        LOG.debug('Derivative Portal: Showing warning popup');
        new Ext.ux.Notify(Ext.applyIf({
            title: 'WARNING',
            titleIconCls: 'titleicon-warning',
            msg: msg.msg || ''
        }, defaultConfig)).show(document);
    }
    
    /**
     * INFO
     */
    var _notifyInfo = function(msg) {
        LOG.debug('Derivative Portal: Showing info popup');
        new Ext.ux.Notify(Ext.applyIf({
            title: 'INFO',
            titleIconCls: 'titleicon-info',
            msg: msg.msg || ''
        }, defaultConfig)).show(document);
    }    
    
    NOTIFY = {
        debug : _notifyDebug,
        success : _notifySuccess,
        error : _notifyError,
        warn : _notifyWarning,
        info : _notifyInfo
    };
    
    LOG.info('Derivative Portal: Notification Initialized.');
}

function initializeMapping() {
    LOG.info('Derivative Portal: Initializing Mapping.');
	
    LOG.debug('Derivative Portal:initializeMapping: Constructing endpoint panel.');
    var legendStore = new Ext.data.JsonStore({
        idProperty: 'name',
        root: 'styles',
        fields: [
        {
            name: 'name', 
            mapping: 'name'
        },
        {
            name: 'title', 
            mapping: 'title'
        },
        {
            name: 'abstrakt', 
            mapping: 'abstract'
        },
        {
            name: 'width', 
            mapping: 'legend.width'
        },
        {
            name: 'height', 
            mapping: 'legend.height'
        },
        {
            name: 'format', 
            mapping: 'legend.format'
        },
        {
            name: 'href', 
            mapping: 'legend.href'
        }
        ]
    });
    
    var baseLayerStore = new GeoExt.data.LayerStore({
        layers : [
        new OpenLayers.Layer.XYZ(
            "Shaded Relief",
            "http://server.arcgisonline.com/ArcGIS/rest/services/NGS_Topo_US_2D/MapServer/tile/${z}/${y}/${x}",
            {
                layers : "0",
                isBaseLayer : true,
                numZoomLevels : 7
            }
            ),
        new OpenLayers.Layer.WMS(
            "Blue Marble",
            "http://maps.opengeo.org/geowebcache/service/wms",
            {
                layers: "bluemarble",
                isBaseLayer : true,
                numZoomLevels : 7
            }
            ),
        new OpenLayers.Layer.WMS(
            "NAIP",
            "http://isse.cr.usgs.gov/ArcGIS/services/Combined/SDDS_Imagery/MapServer/WMSServer",
            {
                layers: "0",
                isBaseLayer : true,
                numZoomLevels : 7
            }
            ),
        new OpenLayers.Layer.XYZ(
            "Street Map",
            "http://server.arcgisonline.com/ArcGIS/rest/services/ESRI_StreetMap_World_2D/MapServer/tile/${z}/${y}/${x}",
            {
                layers : "0",
                isBaseLayer : true,
                numZoomLevels : 7
            }
            )
        ]
    });

    var layerController = new GDP.LayerController({
        id : 'layerController',
        baseLayer : baseLayerStore.getAt(0),
        legendStore : legendStore,
        dimensions : ['time', 'elevation']
    });
    
    var mapPanel = new GDP.BaseMap({
        id : 'mapPanel',
        region: 'center',
        layout : 'fit',
        border: false,
        layerController : layerController,
        baseLayerStore : baseLayerStore
    });

    layerController.requestBaseLayer(layerController.getBaseLayer());
    
    var endpointUrls = [
        ['http://cida-wiwsc-gdp1qa.er.usgs.gov:8080/ncWMS/wms?SERVICE=WMS&REQUEST=GetCapabilities&VERSION=1.1.1'],
        ['http://igsarmewmaccave:8081/ncWMS/wms?SERVICE=WMS&REQUEST=GetCapabilities&VERSION=1.1.1'],
        ['http://igsarm-cida-thredds1.er.usgs.gov:8080/thredds/wms/gmo/GMO_w_meta.ncml?service=WMS&version=1.1.1&request=GetCapabilities'],
        ['http://igsarm-cida-gdp2.er.usgs.gov:8081/geonetwork/srv/en/csw']
    ];
    var proxyUrl = GDP.PROXY_PREFIX + endpointUrls[0];
    var capabilitiesStore = new GeoExt.data.WMSCapabilitiesStore({
        url : proxyUrl,
        storeId : 'capabilitiesStore'
    });
    
    /*
     * Documenting this, because this is very much a convention, close to a hack
     * Parent record is a starting point, the id is all we need to enter a 
     * "CSW tree".
     * 
     * The "CSW tree" needs to have the following dynamics
     * - Parent record enumerates keywords of all children, broken into categories
     * - Children only contain the keywords that describe data in their branch
     * - The leaf record contains both a data endpoint and wms endpoint (sos?)
     * - The WMS layer name for the GCM (you might change gcm to something else)
     *   must be the same as the keyword for that type
     * - Keywords are used to build the UI, which then are used to get to the
     *   map layer or data
     */
    var getRecordsStore = new GDP.CSWGetRecordsStore({
        url : "geonetwork/csw",
        storeId : 'cswStore',
        opts : {
            resultType : 'results',
            outputSchema : 'http://www.isotc211.org/2005/gmd',
            Query : {
                ElementSetName : {
                    value: 'full'
                },
                Constraint : {
                    Filter : {
                        type : '==',
                        property : 'identifier',
                        value : GDP.CSW_QUERY_CONSTRAINT_FILTER_VALUE
                    },
                    version : '1.1.0'
                }
            }
        }
    });
    
    var configPanel = new GDP.DatasetConfigPanel({
        controller : layerController,
        collapsible : true,
        region: 'west',
        width : 265,
        minWidth : 265,
        map : mapPanel.map,
        foiGetCapsURL : GDP.FOI_GETCAPS_URL,
        capabilitiesStore : capabilitiesStore,
        getRecordsStore : getRecordsStore
    });

    var plotterPanel = new GDP.Plotter({
        id : 'plotterPanel',
        plotterDiv : 'dygraph-content',
        legendDiv : 'dygraph-legend',
        region : 'south',
        border : true,
        height : 200,
        controller : layerController
    });
    
    var centerPanel = new Ext.Panel({
        id : 'center-panel',
        region : 'center',
        layout : 'border',
        items : [ mapPanel, plotterPanel]
    });
    
    LOG.info('Derivative Portal: Mapping initialized.');
    LOADMASK = new Ext.LoadMask(Ext.getBody(), {
        msg: '<div id="cida-load-msg">Loading...</div><img src="images/cida-anim.gif" />', 
        msgCls: 'cida-load'
    });
    LOADMASK.show();

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
    
    var viewPort = new Ext.Viewport({
        renderTo : document.body,
        items : [headerPanel, centerPanel, configPanel, footerPanel], 
        layout: 'border'
    });
    
    layerController.on('application-resize', function(collapse){
        LOG.debug('Root:Observed "application-resize": Expand: ' + collapse);
        if (collapse) {
            headerPanel.hide();
            footerPanel.hide();
        } else {
            headerPanel.show();
            footerPanel.show();
        }
        viewPort.doLayout();
    }, this)
    
}

function initializeQuickTips() {
    Ext.QuickTips.init();

    Ext.apply(Ext.QuickTips.getQuickTip(), {
        maxWidth: 200,
        minWidth: 100,
        showDelay: 50,      // Show 50ms after entering target
        dismissDelay: 0,
        trackMouse: true
    });
}

// This is here just for shortcutting some processes in order to test stuff like XML parsing
function test() { 
    var testArray = [2, 3, 4, 5];
    LOG.debug(Array.max(testArray, 2));
    LOG.debug(Array.max(testArray, 11));
    LOG.debug(Array.min(testArray, 0));
    LOG.debug(Array.min(testArray, 22));
}
