Ext.ns("GDP");

GDP.LayerController = Ext.extend(Ext.util.Observable, {
	baseLayer : undefined,
	getBaseLayer : function() {
		return this.baseLayer;
	},
        layer : undefined,
	getLayer : function() {
		return this.layer;
	},
	dimensions : undefined,
	getDimension : function(extentName) {
		return this.dimensions[extentName];
	},
	getAllDimensions : function() {
		return this.dimensions;
	},
	zaxisName : undefined,
	getZAxisName : function() {
		return this.zaxisName;
	},
	layerOpacity : 0.4,
	getLayerOpacity : function() {
		return this.layerOpacity;
	},
        legendStore : undefined,
        getLegendStore : function() {
            return this.legendStore;
        },
        legendRecord : undefined,
        getLegendRecord : function() {
            return this.legendRecord;
        },
        constructor : function(config) {
            LOG.debug('LayerController: Constructing self.');
            
            if (!config) config = {};

            this.layerOpacity = config.layerOpacity || this.layerOpacity;
            this.legendStore = config.legendStore || this.legendStore;
            
            
            // TODO- Ask Sipps what he was doing here
            var filledDims = {'time' : ''}; 
            Ext.each(config.dimensions, function(item) {
                filledDims[item] = '';
            }, this);
            this.dimensions = filledDims;

            GDP.LayerController.superclass.constructor.call(this, config);
            LOG.debug('LayerController: Construction complete.');
            
            this.addEvents(
                "changebaselayer",
                "changelayer",
                "changelegend",
                "changedimension",
                "changeopacity"
            );
            
            // There shouldn't be anything listening at this point
            this.requestBaseLayer(config.baseLayer);

        },
	requestBaseLayer : function(baseLayer) {
            LOG.debug('LayerController:requestBaseLayer');
            if (!baseLayer) return;
            this.baseLayer = baseLayer;
            LOG.debug('LayerController:requestBaseLayer: Added new base layer to LayerController. Firing "changebaselayer".');
            this.fireEvent('changebaselayer');
	},
	requestLayer : function(layerRecord) {
		if (!layerRecord) return;
		this.layer = layerRecord;
		
		var dims = layerRecord.get('dimensions');
		Ext.iterate(dims, function(key, value) {
			this.modifyDimensions(key, value['default']);
		}, this);
		
		var layerName = layerRecord.get('name');
		this.zaxisName = layerName.slice(0, layerName.indexOf('/'));
		
		this.fireEvent('changelayer');
	},
        requestLegendStore : function(legendStore) {
            if (!legendStore) return;
            LOG.debug('LayerController:requestLegendStore: Handling request.');
            this.legendStore = legendStore;
            this.fireEvent('changelegend');
        },
        modifyLegendStore : function(jsonObject) {
            if (!jsonObject) return;
            if (!this.legendStore) return;
            LOG.debug('LayerController:modifyLegendStore: Handling request.');
            this.legendStore.loadData(jsonObject);
            this.fireEvent('changelegend');
        },
        requestLegendRecord : function(legendRecord) {
            if (!legendRecord) return;
            LOG.debug('LayerController:requestLegendRecord: Handling request.');
            this.legendRecord = legendRecord;
            this.fireEvent('changelegend');
        },
	requestOpacity : function(opacity) {
		if (!opacity) return;
                LOG.debug('LayerController:requestOpacity: Handling request.');
		if (0 <= opacity && 1 >= opacity) {
			this.layerOpacity = opacity;
			this.fireEvent('changeopacity');
		}
	},
	requestDimension : function(extentName, value) {
		if (!extentName) return;
                LOG.debug('LayerController:requestDimension: Handling request.');
		if (this.modifyDimensions(extentName, value)) {
			this.fireEvent('changedimension');
		} else {
			LOG.info('Requested dimension (' + extentName + ') does not exist');
		}
	},
	modifyDimensions : function(extentName, value) {
            LOG.debug('LayerController:modifyDimensions: Handling request.');
		if (this.dimensions.hasOwnProperty(extentName)) {
			var val = value || '';
			this.dimensions[extentName] = val;
			return true;
		} else {
			return false;
		}
	},
	loadDimensionStore : function(record, store, extentName, maxCount) {
		if (!record || !store || !extentName) return null;
		LOG.debug('LayerController:loadDimensionStore: Handling request.');
		var maxNum = maxCount || 101;
		
		store.removeAll();
		
		var extents = record.get('dimensions')[extentName];
		if (extents) {
			var currentExtent = extents['default'];
			var timesToLoad = [];
			Ext.each(extents.values, function(item, index, allItems){
				if (index > maxNum) {
					return false;
				} else {
					timesToLoad.push([item.trim()]);
				}
				return true;
			}, this);

			store.loadData(timesToLoad);
			return {
				currentExtent : currentExtent,
				loadedData : timesToLoad
			}
		} else {
			return null;
		}
	}
});