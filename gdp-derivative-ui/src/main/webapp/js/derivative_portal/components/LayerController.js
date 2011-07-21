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
			
			"requestbaselayer"
		);
		
		this.on('requestbaselayer', function(baseLayerRecord) {
			this.baseLayer = baseLayerRecord;
			this.onRequest();
		}, this);
		
		this.on('requestlayer', function(layerRecord) {
			this.layer = layerRecord;
			this.onRequest();
		}, this);
		
	},
	onRequest : function() {
		this.fireEvent('changelayer');
	}
});