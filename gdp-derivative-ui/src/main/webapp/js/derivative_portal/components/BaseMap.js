Ext.ns("GDP");

GDP.BaseMap = Ext.extend(GeoExt.MapPanel, {
	layerOpacity : 0.4,
	maxNumberOfTimesteps : 100,
	legendWindow : undefined,
	baseLayer : undefined,
	timestepStore : undefined,
	realignLegend : function() {
		var DEFAULT_LEGEND_X = 110;
		var DEFAULT_LEGEND_Y = 264;
		this.legendWindow.alignTo(this.getEl(), "br-br", [-DEFAULT_LEGEND_X,-DEFAULT_LEGEND_Y]); 
	},
	constructor : function(config) {
		if (!config) config = {};
		
		var map = new OpenLayers.Map();
    
		this.baseLayer = new OpenLayers.Layer.WMS(
			"Global Imagery",
			"http://maps.opengeo.org/geowebcache/service/wms",
			{
				layers: "bluemarble"
			}
			);

		map.addLayers([this.baseLayer]);
		
		
		this.timestepStore = new Ext.data.ArrayStore({
			storeId : 'timestepStore',
			idIndex: 0,
			fields: ['time']
		});
		
		var timestepController = new Ext.form.ComboBox({
			mode : 'local',
			triggerAction: 'all',
			anchor : '100%',
			store : this.timestepStore,
			displayField : 'time'
		});
		
		timestepController.on('select', function(combo, record, index) {
							var timeStr = record.get('time');
							this.changeTimestep(timeStr);
						}, this);

		var timestepPanel = new Ext.Panel({
			region : 'north',
			border : false,
			height : 'auto',
			layout : 'fit',
			items : [timestepController]
		});
		
		config = Ext.apply({
			map: map,
			center: new OpenLayers.LonLat(-96, 38),
			zoom : 4,
			items : [timestepPanel]
		}, config);
		
		GDP.BaseMap.superclass.constructor.call(this, config);
		
		var legendPanel = new GeoExt.LegendPanel({
			defaults: {
				style: 'padding: 0 5px 5px 5px'
			},
			border : false,
			layerStore: this.layers,
			filter : function(record) {
				return !(record.getLayer().isBaseLayer);
			},
			autoScroll: true
		});
    
		this.legendWindow = new Ext.Window({
			border : false,
			frame : false,
			closable: false,
			resizable : false,
			items : [legendPanel]
		});
		
		this.on('afterlayout', function() {
			LOG.debug('mapPanel afterlayout hit');
			this.legendWindow.show(null, function() {
				LOG.info('Legend window show callback hit');
				this.realignLegend();
			}, this);
		}, this);
	},
	zoomToExtent : function(record) {
		this.map.zoomToExtent(
			OpenLayers.Bounds.fromArray(record.get("llbbox"))
			);
	},
	changeTimestep : function(timestep) {
		this.replaceLayer(this.getCurrentLayer(), {time : timestep});
	},
	updateAvailableTimesteps : function(record) {
		this.timestepStore.removeAll();
		var times = record.get('dimensions').time;
		
		var timesToLoad = [];
		Ext.each(times.values, function(item, index, allItems){
			if (index > this.maxNumberOfTimesteps) {
				return false;
			} else {
				timesToLoad.push([item.trim()]);
			}
		}, this);
		
		this.timestepStore.loadData(timesToLoad);
	},
	getCurrentLayer : function() {
		return this.layers.getAt(1);
	},
	changeLayer : function(record) {
		var copy = record.clone();
		
		var params = {};
		var dim = copy.get('dimensions');
		if (dim && dim.hasOwnProperty('time')) {
			params['time'] = dim.time['default'];
		}
		
		this.replaceLayer(copy, params);
		
		this.updateAvailableTimesteps(copy);
		this.zoomToExtent(copy);
		this.realignLegend();
	},
	replaceLayer : function(record, params) {
		if (!params) {
			params = {};
		}
		
		params = Ext.apply({
			format: "image/png",
			transparent : true
		}, params);
		
		record.get('layer').mergeNewParams(params);
		record.get('layer')['opacity'] = this.layerOpacity;
		
		this.layers.removeAt(1);
		this.layers.add(record);
	}
});