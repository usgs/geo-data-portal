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
	timestep : undefined,
	getTimestep : function() {
		return this.timestep;
	},
	constructor : function(config) {
		if (!config) config = {};
		
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
			"changedimension"
		);
		
	},
	requestBaseLayer : function(baseLayerRecord) {
		this.baseLayer = baseLayerRecord;
		this.fireEvent('changelayer');
	},
	requestLayer : function(layerRecord) {
		this.layer = layerRecord;
		this.fireEvent('changelayer');
	},
	requestTimestep : function(timestep) {
		this.timestep = timestep;
		this.fireEvent('changedimension');
	}
});