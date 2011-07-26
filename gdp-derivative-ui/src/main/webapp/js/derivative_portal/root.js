var LOG;

var LOADMASK;

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
	
	var coolUrls = {
		testCave : 'http://igsarmewmaccave:8081/ncWMS/wms?SERVICE=WMS&REQUEST=GetCapabilities&VERSION=1.1.1',
		initialSample : 'http://igsarm-cida-thredds1.er.usgs.gov:8080/thredds/wms/gmo/GMO_w_meta.ncml?service=WMS&version=1.1.1&request=GetCapabilities'
	};
	
	var endpointTextField = new Ext.form.TextField({
				flex : 1,
				value : coolUrls.testCave
			});
	
	var endpointApplyButton = new Ext.Button({
				text : 'Go'
			});
	
	var endpointContainer = new Ext.form.CompositeField({
		region : 'center',
		fieldLabel : 'Endpoint',
		items : [
			endpointTextField,
			endpointApplyButton
		]
	});
	
	var capabilitiesStore = new GeoExt.data.WMSCapabilitiesStore({
			url : 'proxy/' + endpointTextField.getValue(),
			storeId : 'capabilitiesStore'
		});
		
	endpointApplyButton.on('click', function() {
		capabilitiesStore.proxy.setApi(Ext.data.Api.actions.read, 'proxy/' + endpointTextField.getValue());
		capabilitiesStore.load();
	}, this);
	
	//UI Components
	var configPanel = new GDP.LayerChooser({
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
//	LOADMASK.show();
	
    capabilitiesStore.load();
}

