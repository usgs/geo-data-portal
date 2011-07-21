Ext.ns("GDP");

GDP.DimensionController = Ext.extend(Ext.util.Observable, {
	constructor : function(config) {
		if (!config) config = {};
		
		config = Ext.apply({
			
		}, config);
		
		GDP.DimensionController.superclass.constructor.call(this, config);
		
		
	}
});