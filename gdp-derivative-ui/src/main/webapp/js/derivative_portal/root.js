//TODO- Way too much coniguration going on in this file. A lot of the elements can (should) be made into their own classes and self-configured in there
//TODO- Make sure that the element initializations are coming in at the right order -- With this much initialization from one place, lots of room for issues to come up
var LOG;
var NOTIFY;
var LOADMASK;

Ext.onReady(function () {
        GDP.PROXY_PREFIX = 'proxy/';
	initializeLogging();
	initializeNotification();
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

function initializeNotification() {
    LOG.info('Derivative Portal: Initializing Notification.');
    LOG.debug('root:initializeNotification');
	var _notifyError = function(config) {
		if (!config) config = {};
		
		var moreInfo = new Ext.Button({text: 'More Info...'});
		var buttons = [];
		
		if (config.moreInfoAction) {
			buttons.push(moreInfo);
		}
		
		var notify = new Ext.ux.Notify({
			msgWidth: 200,
			hideDelay: 8000,
			title: 'Error!',
			titleIconCls: 'titleicon-error',
			msgIconCls: 'msgicon-error',
			msg: 'An error has occured.',
			buttons: buttons
		});
		
		if (config.moreInfoAction) {
			moreInfo.on('click', function() {
					notify.hide();
					config.moreInfoAction();
				});
		}
		
            notify.show(document);
	}
	
	var _notifySuccess = function() {
		new Ext.ux.Notify({
			msgWidth: 200,
			hideDelay: 1000,
			title: 'Success!',
			titleIconCls: 'titleicon-success',
			msg: 'Data saved successfully.'
		}).show(document);
	}
	
	var _notifyDebug = function(msg) {
		new Ext.ux.Notify({
			msgWidth: 200,
			hideDelay: 3000,
			title: 'DEBUG',
			titleIconCls: 'titleicon-debug',
			msg: msg
		}).show(document);
	}
	NOTIFY = {
		debug : _notifyDebug,
		success : _notifySuccess,
		error : _notifyError
	};
        LOG.info('Derivative Portal: Notification Initialized.');
}

function initializeMapping() {
    LOG.info('Derivative Portal: Initializing Mapping.');
    LOG.debug('root:initializeMapping');
    LOADMASK = new Ext.LoadMask(Ext.getBody());
	
    var proxyUrl, urls, endpointStore, endpointCombo, endpointApplyButton, endpointContainer, endpointPanel;
    {
        proxyUrl = '';
        urls = [
                ['http://cida-wiwsc-gdp1qa.er.usgs.gov:8080/ncWMS/wms?SERVICE=WMS&REQUEST=GetCapabilities&VERSION=1.1.1'],
                ['http://igsarmewmaccave:8081/ncWMS/wms?SERVICE=WMS&REQUEST=GetCapabilities&VERSION=1.1.1'],
                ['http://igsarm-cida-thredds1.er.usgs.gov:8080/thredds/wms/gmo/GMO_w_meta.ncml?service=WMS&version=1.1.1&request=GetCapabilities']
        ];
        endpointStore = new Ext.data.ArrayStore({
                storeId : 'endpointStore',
                idIndex: 0,
                fields: ['url']
        });
        endpointStore.loadData(urls);
        endpointCombo = new Ext.form.ComboBox({
            mode : 'local'
            ,triggerAction: 'all'
            ,flex : 1
            ,store : endpointStore
            ,value : urls[0][0]
            ,lazyInit : false
            ,displayField : 'url'
            
        });
        endpointApplyButton = new Ext.Button({
            text : 'Go'
        });
        endpointCombo.on('select', function(){
            endpointApplyButton.fireEvent('click');
        });
        endpointContainer = new Ext.form.CompositeField({
            region : 'center'
            ,fieldLabel : 'Endpoint'
            ,items : [
                endpointCombo,
                endpointApplyButton
            ]
        });
        if (endpointCombo.getRawValue()) {
            proxyUrl = GDP.PROXY_PREFIX + endpointCombo.getRawValue();
        }
        endpointPanel = new Ext.Panel({
            region : 'north',
            border : false,
            layout : 'border',
            collapsed : true,
            collapsible : true,
            floatable : false,
            hideCollapseTool : true,
            collapseMode : 'mini',
            split : true,
            height : 25,
            minSize : 25,
            maxSize : 25,
            items : [endpointContainer]
        });
    }

    var capabilitiesStore = new GeoExt.data.WMSCapabilitiesStore({
        url : proxyUrl,
        storeId : 'capabilitiesStore'
    });
    
    var legendStore = new Ext.data.JsonStore({
        idProperty: 'name'
        ,root: 'styles'
        ,fields: [
            {name: 'name', mapping: 'name'}
            ,{name: 'title', mapping: 'title'}
            ,{name: 'abstrakt', mapping: 'abstract'}
            ,{name: 'width', mapping: 'legend.width'}
            ,{name: 'height', mapping: 'legend.height'}
            ,{name: 'format', mapping: 'legend.format'}
            ,{name: 'href', mapping: 'legend.href'}
        ]
    });
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
            )
        ]
    });
    
    // Central controller for the application.
    var layerController = new GDP.LayerController({
        baseLayer : baseLayerStore.getAt(3) 
        ,legendStore : legendStore
        ,dimensions : ['time', 'elevation']
    });

    //UI Components
    var configPanel, mapPanel, timestepPanel, centerPanel;
    
    mapPanel = new GDP.BaseMap({
            id : 'mapPanel',
            region: 'center',
            layout : 'fit',
            border: false,
            layerController : layerController,
            title: 'USGS Derived Downscaled Climate Portal'
    });
    
    configPanel = new GDP.LayerChooser({
        id: 'control-panel',
        title : 'Controls',
        region: 'west',
        labelWidth: 80,
        border : false,
        collapsible : true,
        floatable : false,
        hideCollapseTool : true,
        collapseMode : 'mini',
        split : true,
        width : 265,
        minSize : 265,
        maxSize : 265,
        map : mapPanel.map,
        capabilitiesStore : capabilitiesStore,
        baseLayerStore : baseLayerStore,
        controller : layerController,
        defaults: {
            width: 180
        }
    });
    
    timestepPanel = new GDP.TimestepChooser({
        region : 'south',
        border : false,
        height : 30,
        layerController : layerController
    });
    centerPanel = new Ext.Panel({
            region : 'center',
            layout : 'border',
            items : [ endpointPanel, mapPanel, timestepPanel]
    });

    var headerPanel, footerPanel;
    headerPanel = new Ext.Panel({
        region: 'north',
        height: 'auto',
        border : false,
        autoShow: true,
        contentEl: 'usgs-header-panel'
    });
    footerPanel = new Ext.Panel({
        region: 'south',
        height: 'auto',
        border : false,
        autoShow: true,
        contentEl: 'usgs-footer-panel'
    });

    LOG.debug('root:initializeMapping: Registering Event Handlers.');
    {
        capabilitiesStore.on('exception', function() {
            if (LOADMASK) LOADMASK.hide();
            NOTIFY.error();
        }, this);
        capabilitiesStore.on('load', function() {
            LOG.debug('root: Capabilities store has finished loading.');
            if (LOADMASK) LOADMASK.hide();
        }, this);
        endpointApplyButton.on('click', function() {
            LOG.debug('EVENT: User has clicked on the endpoint apply button');
            var endpoint = endpointCombo.getRawValue();
            if (endpoint) {
                LOG.debug('EVENT: Adding ' + endpoint + ' to the capabilities store');
                if (LOADMASK) LOADMASK.show();
                var proxyUrl = '';
                proxyUrl = GDP.PROXY_PREFIX + endpoint;
                capabilitiesStore.proxy.setApi(Ext.data.Api.actions.read, proxyUrl);
                capabilitiesStore.load();
            }
        }, this);
    }

    LOG.info('Derivative Portal: Mapping initialized.');
    if (LOADMASK) LOADMASK.show();
    
    LOG.debug('root: Loading capabilities store.');
    capabilitiesStore.load();
    LOG.debug('root: Triggering LayerController:requestBaseLayer.');
    layerController.requestBaseLayer(layerController.getBaseLayer());
    
    new Ext.Viewport({
            renderTo : document.body,
            items : [headerPanel, centerPanel, configPanel,footerPanel], 
            layout: 'border'
    });
}