var LOG;
var NOTIFY;
var LOADMASK;

Ext.onReady(function () {
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
}

function initializeMapping() {
	
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
	
	var layerController = new GDP.LayerController({
		baseLayer : baseLayerStore.getAt(3),
		dimensions : [
			'time', 'elevation'
		]
	});
	
	var endpointStore = new Ext.data.ArrayStore({
		storeId : 'endpointStore',
		idIndex: 0,
		fields: ['url']
	});
	
	var urls = [
		['http://cida-wiwsc-gdp1qa.er.usgs.gov:8080/ncWMS/wms?SERVICE=WMS&REQUEST=GetCapabilities&VERSION=1.1.1'],
		['http://igsarmewmaccave:8081/ncWMS/wms?SERVICE=WMS&REQUEST=GetCapabilities&VERSION=1.1.1'],
		['http://igsarm-cida-thredds1.er.usgs.gov:8080/thredds/wms/gmo/GMO_w_meta.ncml?service=WMS&version=1.1.1&request=GetCapabilities']
	];
	
	endpointStore.loadData(urls);
	
	var endpointCombo = new Ext.form.ComboBox({
			mode : 'local',
			triggerAction: 'all',
			flex : 1,
			store : endpointStore,
			value : urls[0][0],
			lazyInit : false,
			displayField : 'url'
		});
	
//	var endpointTextField = new Ext.form.TextField({
//				flex : 1,
//				value : coolUrls.qa
//			});
	
	var endpointApplyButton = new Ext.Button({
				text : 'Go',
				handler : function() {
					if (LOADMASK) LOADMASK.show();
				}
			});
	
	var endpointContainer = new Ext.form.CompositeField({
		region : 'center',
		fieldLabel : 'Endpoint',
		items : [
			endpointCombo,
			endpointApplyButton
		]
	});
	
	var proxyUrl = '';
	
	if (endpointCombo.getRawValue() && '' !== endpointCombo.getRawValue()) {
		proxyUrl = 'proxy/' + endpointCombo.getRawValue();
	}
	
	var capabilitiesStore = new GeoExt.data.WMSCapabilitiesStore({
			url : proxyUrl,
			storeId : 'capabilitiesStore'
		});
	
	capabilitiesStore.on('exception', function() {
		if (LOADMASK) LOADMASK.hide();
		NOTIFY.error();
	}, this);
		
	endpointApplyButton.on('click', function() {
		var proxyUrl = '';
	
		if (endpointCombo.getRawValue() && '' !== endpointCombo.getRawValue()) {
			proxyUrl = 'proxy/' + endpointCombo.getRawValue();
			capabilitiesStore.proxy.setApi(Ext.data.Api.actions.read, proxyUrl);
			capabilitiesStore.load();
		}
	}, this);
	
	//UI Components
	var configPanel = new GDP.LayerChooser({
                id: 'control-panel',
		title : 'Controls',
		width : 265,
		region: 'west',
		labelWidth: 80,
		border : false,
		collapsible : true,
		floatable : false,
		hideCollapseTool : true,
		collapseMode : 'mini',
		split : true,
		minSize : 265,
		maxSize : 265,
		capabilitiesStore : capabilitiesStore,
		baseLayerStore : baseLayerStore,
		controller : layerController,
		defaults: {
			width: 180
		}
	});
	
	var endpointPanel = new Ext.Panel({
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
	
	var timestepPanel = new GDP.TimestepChooser({
				region : 'south',
				border : false,
				height : 30,
				layerController : layerController
			});
	
	var mapPanel = new GDP.BaseMap({
		id : 'mapPanel',
		region: 'center',
		layout : 'fit',
		border: false,
		layerController : layerController,
		title: 'USGS Derived Downscaled Climate Portal'
	});
	
	var centerPanel = new Ext.Panel({
		region : 'center',
		layout : 'border',
		items : [ endpointPanel, mapPanel, timestepPanel]
	})
	
	var headerPanel = new Ext.Panel({
		region: 'north',
		height: 'auto',
		border : false,
		autoShow: true,
		contentEl: 'usgs-header-panel'
	});
    
	var footerPanel = new Ext.Panel({
		region: 'south',
		height: 'auto',
		border : false,
		autoShow: true,
		contentEl: 'usgs-footer-panel'
	});
	
	new Ext.Viewport({
		renderTo : document.body,
		items : [headerPanel, centerPanel, configPanel,footerPanel], 
		layout: 'border'
	});
    
	LOG.info('Derivative Portal: Mapping initialized.');
	
	LOADMASK = new Ext.LoadMask(Ext.getBody());
	if (LOADMASK) LOADMASK.show();
	
    capabilitiesStore.load();
}

