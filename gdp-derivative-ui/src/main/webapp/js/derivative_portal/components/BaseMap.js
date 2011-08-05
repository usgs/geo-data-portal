Ext.ns("GDP");

GDP.BaseMap = Ext.extend(GeoExt.MapPanel, {
	layerController : undefined,
	currentLayer : undefined,
	constructor : function(config) {
            LOG.debug('BaseMap: Constructing self.');
		// From GDP (with Zoerb's comments)
		// Got this number from Hollister, and he's not sure where it came from.
		// Without this line, the esri road and relief layers will not display
		// outside of the upper western hemisphere.
		var MAX_RESOLUTION = 1.40625/2;
                
		if (!config) config = {};
		
		var map = new OpenLayers.Map({
			maxResolution: MAX_RESOLUTION
                        ,controls: [
                            new OpenLayers.Control.MousePosition()
                            ,new OpenLayers.Control.ScaleLine()
                            ,new OpenLayers.Control.Navigation()
                            ,new OpenLayers.Control.ArgParser()
                            ,new OpenLayers.Control.Attribution()
                            ,new OpenLayers.Control.PanZoomBar()
                        ]
		});
			
		config = Ext.apply({
			map : map,
			center : new OpenLayers.LonLat(-96, 38),
			zoom : 4
		}, config);
                
		GDP.BaseMap.superclass.constructor.call(this, config);
		LOG.debug('BaseMap: Constructing done.');
                
                // Register listeners
                {
                    this.layerController = config.layerController;
                    this.layerController.on('changebaselayer', function() {
                        this.replaceBaseLayer(this.layerController.getBaseLayer());
                    },this);
                    this.layerController.on('changelayer', function() {
                        this.onChangeLayer();
                        this.currentLayer = this.findCurrentLayer();
                    }, this);
                    this.layerController.on('changedimension', function() {
                        this.onChangeDimension();
                        this.currentLayer = this.findCurrentLayer();
                    }, this);
                    this.layerController.on('changeopacity', function() {
                        this.onChangeOpacity();
                    }, this);
                    this.layerController.on('changelegend', function() {
                        this.onChangeLegend();
                        this.currentLayer = this.findCurrentLayer();
                    }, this);
                }
                
		this.layerController.requestBaseLayer(this.layerController.getBaseLayer());
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
                return (this.layerController.getLayerOpacity() === record.get('layer').opacity);
            }, this, 1);
            if (-1 < storeIndex) {
                    return this.layers.getAt(storeIndex);
            } else {
                    return null;
            }
	},
	clearLayers : function() {
            LOG.debug('BaseMap:clearLayers: Handling request.');
		if (this.layers.getCount() > 1) {
                    LOG.debug('BaseMap:clearLayers: Clearing layer.');
                    Ext.each(this.layers, function(item, index, allItems){
                    },this);
                    this.layers.remove(this.layers.getRange(1));
		}
	},
	onChangeLayer : function() {
            LOG.debug('BaseMap:onChangeLayer: Handling request.')
            
            var layer = this.layerController.getLayer();

            if (!this.currentLayer || this.currentLayer.getLayer() !== layer) {
                    this.zoomToExtent(layer);
                    this.clearLayers();

                    var params = {};
                    Ext.apply(params, this.layerController.getAllDimensions());
                    this.replaceLayer(layer, params);
            }
	},
	onChangeDimension : function() {
            LOG.debug('BaseMap:onChangeDimension: Handling request.');
		var existingLayerIndex = this.layers.findBy(function(record, id) {
                    LOG.debug(' BaseMap:onChangeDimension: Checking existing layer index.');
			var result = true;
			var requestedDimensions = this.layerController.getAllDimensions();
			Ext.iterate(requestedDimensions, function(extentName, value) {
				var existingDimension = record.getLayer().params[extentName.toUpperCase()];
				result = result && (existingDimension === value)
			}, this);
                        LOG.debug(' BaseMap:onChangeDimension: Found existing layer index ' + result);
			return result;
		}, this, 1);
		
		var params = {};
		Ext.apply(params, this.layerController.getAllDimensions());
		
		this.replaceLayer(
			this.layerController.getLayer(), 
			params,
			(-1 < existingLayerIndex) ? existingLayerIndex : undefined
		);
	},
        onChangeLegend : function() {
            LOG.debug('BaseMap:onChangeLegend: Handling Request.');
            var record = this.layerController.getLegendRecord();
            this.clearLayers();
            this.replaceLayer(
                this.layerController.getLayer(),
                {
                    styles: record.id
                }
            );
            
        },
	onChangeOpacity : function() {
            LOG.debug('BaseMap:onChangeOpacity: Handling Request.');
		if (this.currentLayer) {
			this.currentLayer.getLayer().setOpacity(this.layerController.getLayerOpacity());
		}
            },
        replaceBaseLayer : function(record) {
            if (!record) return;
            LOG.debug('BaseMap:replaceBaseLayer: Handling Request.');
            if (!this.layers.getAt(0)) {
                this.layers.add([record]);//map.addLayer(record);
            } else {
                if (this.layers.getCount() > 0) {
                    var baseLayerIndex = this.layers.findBy(function(r, id){
                        return r.data.layer.isBaseLayer
                    });
                    if (baseLayerIndex > -1 ) {
                        this.layers.removeAt(baseLayerIndex);
                        this.layers.insert(baseLayerIndex, [record]);
                    } else {
                         this.layers.add([record]);
                    }
                } else { 
                    this.layers.add([record]);
                }
            }
        },
	replaceLayer : function(record, params, existingIndex) {
            LOG.debug('BaseMap:replaceLayer: Handling request.');
		if (!record) return;
		if (!params) {
			params = {};
		}
		
		if (existingIndex) {
			var newLayer = this.layers.getAt(existingIndex).getLayer();
			newLayer.setOpacity(this.layerController.getLayerOpacity());
		} else {
			var copy = record.clone();
			
			params = Ext.apply({
				format: "image/png"
				,transparent : true
//                                ,styles : (params.styles) ? params.styles : this.layerController.getLegendRecord().id
			}, params);

			copy.get('layer').mergeNewParams(params);
			copy.get('layer')['opacity'] = this.layerController.getLayerOpacity();
			copy.get('layer')['url'] = 'proxy/' + copy.get('layer')['url'];

			copy.getLayer().events.register('loadend', this, function() {
                            if (LOADMASK) LOADMASK.hide();
			});
			this.layers.add(copy);
		}
		
		if (this.currentLayer) {
                    LOG.debug('BaseMap:replaceLayer: currentLayer exists. Setting opacity to 0');
                    this.currentLayer.getLayer().setOpacity(0);
		}
	}
});