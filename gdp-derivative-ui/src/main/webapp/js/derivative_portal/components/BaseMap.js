Ext.ns("GDP");

GDP.BaseMap = Ext.extend(GeoExt.MapPanel, {
	layerOpacity : 0.4,
	legendWindow : undefined,
	layerController : undefined,
	currentLayer : undefined,
	realignLegend : function(isAlreadyRendered) {
		if (this.legendWindow) {
			var DEFAULT_LEGEND_X = 110;
			var DEFAULT_LEGEND_Y = 264;
			if (isAlreadyRendered) {
				this.legendWindow.alignTo(this.getEl(), "br-br"); 
			} else {
				this.legendWindow.alignTo(this.getEl(), "br-br", [-DEFAULT_LEGEND_X,-DEFAULT_LEGEND_Y]); 
			}
		}
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
			this.onChangeLayer();
			this.currentLayer = this.findCurrentLayer();
		}, this);
		this.layerController.on('changedimension', function() {
			this.onChangeDimension();
			this.currentLayer = this.findCurrentLayer();
		}, this);
		
		config = Ext.apply({
			map: map,
			center: new OpenLayers.LonLat(-96, 38),
			zoom : 4
		}, config);
		
		GDP.BaseMap.superclass.constructor.call(this, config);
		
		this.layerController.requestBaseLayer(this.layerController.getBaseLayer());
		
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
		
		this.on('resize', function() {
			this.realignLegend(true);
		}, this);
	},
	zoomToExtent : function(record) {
		if (!record) return;
		this.map.zoomToExtent(
			OpenLayers.Bounds.fromArray(record.get("llbbox"))
			);
	},
	/**
	 * Completely accurate (tho expensive) way to find which layer
	 * is the visible one.
	 */
	findCurrentLayer : function() {
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
	onChangeLayer : function() {			
		this.replaceBaseLayer(this.layerController.getBaseLayer());
		
		var layer = this.layerController.getLayer();

		this.zoomToExtent(layer);
		this.clearLayers();
			
		var params = {};
		Ext.apply(params, this.layerController.getAllDimensions());
			
		this.replaceLayer(layer, params);
			
		this.realignLegend();
	},
	onChangeDimension : function() {
		var existingLayerIndex = this.layers.findBy(function(record, id) {
			var result = true;
			var requestedDimensions = this.layerController.getAllDimensions();
			Ext.iterate(requestedDimensions, function(extentName, value) {
				var existingDimension = record.getLayer().params[extentName.toUpperCase()];
				result = result && (existingDimension === value)
			}, this);
			return result;
		}, this, 1);
		
		var params = {};
		Ext.apply(params, this.layerController.getAllDimensions());
		
		this.replaceLayer(
			this.layerController.getLayer(), 
			params,
			(-1 < existingLayerIndex)?existingLayerIndex : undefined
		);
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
	replaceLayer : function(record, params, existingIndex) {
		if (!record) return;
		if (!params) {
			params = {};
		}

		if (this.currentLayer) {
			this.currentLayer.getLayer().setVisibility(false);
		}
		
		if (existingIndex) {
			var newLayer = this.layers.getAt(existingIndex).getLayer();
			newLayer.setVisibility(true);
		} else {
			var copy = record.clone();
			
			params = Ext.apply({
				format: "image/png",
				transparent : true,
				styles : 'boxfill/redblue'
			}, params);

			copy.get('layer').mergeNewParams(params);
			copy.get('layer')['opacity'] = this.layerOpacity;
			
			copy.getLayer().events.register('loadend', this, function() {
				LOADMASK.hide();
			});
			this.layers.add(copy);
		}
	}
});