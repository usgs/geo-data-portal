Ext.ns("GDP");

GDP.BaseMap = Ext.extend(GeoExt.MapPanel, {
	layerOpacity : 0.4,
	maxNumberOfTimesteps : 5,
	legendWindow : undefined,
	//	baseLayer : undefined,
	layerController : undefined,
	timestepStore : undefined,
	timestepController : undefined,
	timestepAnimator : undefined,
	realignLegend : function() {
		var DEFAULT_LEGEND_X = 110;
		var DEFAULT_LEGEND_Y = 264;
		this.legendWindow.alignTo(this.getEl(), "br-br", [-DEFAULT_LEGEND_X,-DEFAULT_LEGEND_Y]); 
	},
	constructor : function(config) {
		// From GDP (with Zoerb's comments)
		// Got this number from Hollister, and he's not sure where it came from.
		// Without this line, the esri road and relief layers will not display
		// outside of the upper western hemisphere.
		var MAX_RESOLUTION = 1.40625/2;
                
		if (!config) config = {};
		
		var map = new OpenLayers.Map({
			maxResolution: MAX_RESOLUTION
		});
				
		this.layerController = config.layerController;
		
		this.layerController.on('changelayer', function() {
			LOG.info('BaseMap changelayer hit');
			this.replaceBaseLayer(this.layerController.getBaseLayer());
			this.changeLayer(this.layerController.getLayer());
		}, this);
		
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
		
		this.timestepAnimator.on('timestepchange', function(time) {
			this.changeTimestep(time);
		}, this);
		
		config = Ext.apply({
			map: map,
			center: new OpenLayers.LonLat(-96, 38),
			zoom : 4,
			items : [timestepPanel]
		}, config);
		
		GDP.BaseMap.superclass.constructor.call(this, config);
		
		this.layerController.fireEvent('requestbaselayer', this.layerController.getBaseLayer());
		
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
			this.legendWindow.show(null, function() {
				this.realignLegend();
			}, this);
		}, this);
	},
	zoomToExtent : function(record) {
		if (!record) return;
		this.map.zoomToExtent(
			OpenLayers.Bounds.fromArray(record.get("llbbox"))
			);
	},
	changeTimestep : function(timestep) {
		this.replaceLayer(this.getCurrentLayer(), {
			time : timestep
		});
	},
	updateAvailableTimesteps : function(record, currentTimestep) {
		if (!record) return;
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
		if (!record) return;
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
		if (!record) return;
		if (!this.layers) {
			this.map.addLayer(record.getLayer());
		} else {
			if (0 < this.layers.getCount()) {
				if (this.layers.getAt(0) !== record) {  //If it's a different baseLayer, lets swap it out
					this.layers.removeAt(0);
					this.layers.insert(0, record);
				}
			} else {
				this.layers.add(record);
			}
			
		}
			
	},
	replaceLayer : function(record, params) {
		if (!record) return;
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