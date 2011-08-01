Ext.ns("GDP");

GDP.LayerController = Ext.extend(Ext.util.Observable, {
	baseLayer : undefined,
	layer : undefined,
	getBaseLayer : function() {
		return this.baseLayer;
	},
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
	constructor : function(config) {
		if (!config) config = {};
		
		this.layerOpacity = config.layerOpacity || this.layerOpacity;
		
		var baseLayer = config.baseLayer;
		
		var configDimensions = config.dimensions;
		var filledDims = {'time' : ''};
		
		Ext.each(configDimensions, function(item) {
			filledDims[item] = '';
		}, this);
		
		this.dimensions = filledDims;
		
		config = Ext.apply({
			
		}, config);
		
		GDP.LayerController.superclass.constructor.call(this, config);
		
		this.addEvents(
		/**
		 * @event changelayer
		 * Fired after all configuration changes has been made and are ready
		 * to switch to a new layer.
		 */
			"changelayer",
			"changedimension",
			"changeopacity"
		);
		
		this.requestBaseLayer(baseLayer);
		
	},
	requestBaseLayer : function(baseLayerRecord) {
		if (!baseLayerRecord) return;
		this.baseLayer = baseLayerRecord;
		this.fireEvent('changelayer');
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
	requestOpacity : function(opacity) {
		if (!opacity) return;
		if (0 <= opacity && 1 >= opacity) {
			this.layerOpacity = opacity;
			this.fireEvent('changeopacity');
		}
	},
	requestDimension : function(extentName, value) {
		if (!extentName) return;
		if (this.modifyDimensions(extentName, value)) {
			this.fireEvent('changedimension');
		} else {
			LOG.info('Requested dimension (' + extentName + ') does not exist');
		}
	},
	modifyDimensions : function(extentName, value) {
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
