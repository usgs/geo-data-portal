Ext.ns("GDP");

GDP.BaseMap = Ext.extend(GeoExt.MapPanel, {
	layerOpacity : 0.4,
	maxNumberOfTimesteps : 5,
	legendWindow : undefined,
	baseLayer : undefined,
	timestepStore : undefined,
	timestepController : undefined,
	timestepAnimator : undefined,
	realignLegend : function() {
		var DEFAULT_LEGEND_X = 110;
		var DEFAULT_LEGEND_Y = 264;
		this.legendWindow.alignTo(this.getEl(), "br-br", [-DEFAULT_LEGEND_X,-DEFAULT_LEGEND_Y]); 
	},
	constructor : function(config) {
		if (!config) config = {};
		
		var map = new OpenLayers.Map();
    
                this.baseLayer = config.baseLayer.getLayer();

                map.addLayers([this.baseLayer]);

		
		this.timestepStore = new Ext.data.ArrayStore({
			storeId : 'timestepStore',
			idIndex: 0,
			fields: ['time']
		});
		
		this.timestepController = new Ext.form.ComboBox({
			mode : 'local',
			triggerAction: 'all',
			flex : 1,
			anchor : '100%',
			store : this.timestepStore,
			displayField : 'time'
		});
		
		this.timestepController.on('select', function(combo, record, index) {
							var timeStr = record.get('time');
							this.changeTimestep(timeStr);
						}, this);
		
		var timestepPanel = new Ext.Panel({
			region : 'north',
			border : false,
			height : 'auto',
			layout : 'hbox',
			items : [
				this.timestepController,
				new Ext.Button({
					text : 'Play Timesteps',
					handler : function() {
						this.timestepAnimator.startAnimation();
					},
					scope : this
				}),
				new Ext.Button({
					text : 'Stop Timesteps',
					handler : function() {
						this.timestepAnimator.stopAnimation();
					},
					scope : this
				})
			]
		});
		
		this.timestepAnimator = new GDP.MapTimestepAnimator({
			baseMap : this,
			store : this.timestepStore
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
	updateAvailableTimesteps : function(record, currentTimestep) {
		
		this.timestepStore.removeAll();
		var times = record.get('dimensions').time;
		
		var timesToLoad = [];
		Ext.each(times.values, function(item, index, allItems){
			if (index > this.maxNumberOfTimesteps) {
				return false;
			} else {
				timesToLoad.push([item.trim()]);
			}
			return true;
		}, this);
		
		this.timestepStore.loadData(timesToLoad);
		this.timestepController.setValue(currentTimestep);
	},
	getCurrentLayer : function() {
		var storeIndex = this.layers.findBy(function(record, id) {
			return record.get('layer').getVisibility();
		}, this, 1);
		if (-1 < storeIndex) {
			return this.layers.getAt(storeIndex);
		} else {
			return null;
		}
		
	},
	clearLayers : function() {
		if (this.layers.getCount() > 1) {
			this.layers.remove(this.layers.getRange(1));
		}
	},
	changeLayer : function(record) {
		var params = {};
		
		this.zoomToExtent(record);
		
		var dim = record.get('dimensions');
		var timestep = '';
		if (dim && dim.hasOwnProperty('time')) {
			timestep = dim.time['default'];
			params['time'] = timestep;
		}
		
		this.clearLayers();
		this.replaceLayer(record, params);
		
		this.updateAvailableTimesteps(record, timestep);
		this.realignLegend();
	},
        replaceBaseLayer : function(record) {
            this.layers.removeAt(0);
            this.layers.insert(0,[record]);
        },
	replaceLayer : function(record, params) {
		if (!params) {
			params = {};
		}
		
		var requestedTime = params.time;
		var existingLayerIndex = this.layers.findBy(function(record, id) {
					var existingTime = record.get('layer').params['TIME'];
					return (existingTime === requestedTime);
				}, this, 1);
		
		var hideLayer = function(oldLayer) {
			if (oldLayer) {
				oldLayer.getLayer().setVisibility(false);
				oldLayer.getLayer().redraw();
			}
		};
		
		if (-1 < existingLayerIndex) {
			hideLayer(this.getCurrentLayer());
			
			var newLayer = this.layers.getAt(existingLayerIndex).getLayer();
			newLayer.setVisibility(true);
			newLayer.redraw();
		} else {
			//Only the base layer exists on this map, lets get it.
			var copy = record.clone();
			hideLayer(this.getCurrentLayer());
			
			params = Ext.apply({
				format: "image/png",
				transparent : true
			}, params);

			copy.get('layer').mergeNewParams(params);
			copy.get('layer')['opacity'] = this.layerOpacity;

			this.layers.add(copy);
		}
	}
});