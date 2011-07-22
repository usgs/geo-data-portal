Ext.ns("GDP");

GDP.LayerChooser = Ext.extend(Ext.form.FormPanel, {
	controller : undefined,
	constructor : function(config) {
		if (!config) config = {};
		
		this.controller = config.controller || new GDP.LayerController({});
		
		var capabilitiesStore = new GeoExt.data.WMSCapabilitiesStore({
			url : 'proxy/http://igsarm-cida-thredds1.er.usgs.gov:8080/thredds/wms/gmo/GMO_w_meta.ncml?service=WMS&version=1.1.1&request=GetCapabilities',
			storeId : 'capabilitiesStore'
		});
		capabilitiesStore.load();
		
		capabilitiesStore.on('load', function(capStore, records) {
			var firstIndex = 0;
			var firstRecord = capStore.getAt(firstIndex);
			layerCombo.setValue(firstRecord.get("title"));
			layerCombo.fireEvent('select', layerCombo, firstRecord, 0);
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
				), 
			]
		});
		
		this.controller.requestBaseLayer(baseLayerStore.getAt(3));
		
		var layerCombo = new Ext.form.ComboBox({
			xtype : 'combo',
			mode : 'local',
			triggerAction: 'all',
			store : capabilitiesStore,
			fieldLabel : 'Layer',
			forceSelection : true,
			lazyInit : false,
			displayField : 'title'
		});
		
		layerCombo.on('select', function(combo, record, index) {
			LOG.debug("layerCombo select hit");
			this.controller.requestLayer(record);
		}, this);
	
		var baseLayerCombo = new Ext.form.ComboBox({
			xtype : 'combo',
			mode : 'local',
			triggerAction: 'all',
			store : baseLayerStore,
			fieldLabel : 'Base Layer',
			forceSelection : true,
			lazyInit : false,
			displayField : 'title'
		});
		
		baseLayerCombo.on('select', function(combo, record, index) {
			this.controller.requestBaseLayer(record);
		}, this);
		
		config = Ext.apply({
			items : [
			baseLayerCombo,
			layerCombo
			]
		}, config);
		
		this.controller.on('changelayer', function() {
			var baseLayer = this.controller.getBaseLayer();
			if (baseLayer) {
				baseLayerCombo.setValue(baseLayer.getLayer().name);
			}
			
			var layer = this.controller.getLayer();
			if (layer) {
				layerCombo.setValue(layer.getLayer().name);
			}
			
		}, this);
		
		GDP.LayerChooser.superclass.constructor.call(this, config);
	}
});